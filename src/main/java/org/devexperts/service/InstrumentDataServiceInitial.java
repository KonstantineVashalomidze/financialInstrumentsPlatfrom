package org.devexperts.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.devexperts.model.InstrumentData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class InstrumentDataServiceInitial {
    private final RestTemplate restTemplate;
    // Symbol names alongside with their observables
    private final Map<String, Observable<InstrumentData>> instrumentObservables;
    // Usernames alongside set containing subscribed symbols.
    private final Map<String, Set<String>> userSubscriptions;
    private final Map<String, Set<WebSocketSession>> userSessions;
    private final Map<String, Map<WebSocketSession, Map<String, Disposable>>> userSessionSubscriptions;
    private final Map<String, InstrumentData> instrumentCache;
    // At leas how many users should have subscribed this instrument to be considered popular. will decide if we add
    // this instrument in cache or not
    private static final int POPULARITY_THRESHOLD = 5;
    @Value("${api.mockdata.url}")
    private String apiUrl;

    public InstrumentDataServiceInitial(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.instrumentObservables = new ConcurrentHashMap<>();
        this.userSubscriptions = new ConcurrentHashMap<>();
        this.userSessions = new ConcurrentHashMap<>();
        this.userSessionSubscriptions = new ConcurrentHashMap<>();
        this.instrumentCache = new ConcurrentHashMap<>();
    }

    public void subscribeToInstrument(
            String username,
            String symbol
    ) {
        // Add the symbol to the user's subscription set, create a new set if needed
        userSubscriptions.computeIfAbsent(
                        username,
                        k -> ConcurrentHashMap.newKeySet()
                )
                .add(symbol);

        InstrumentData cachedData = instrumentCache.get(symbol);
        if (cachedData != null)  // If data is available in cache, provide immediate feedback to the user
        {
            sendCachedUpdate(
                    username,
                    symbol,
                    cachedData
            );
        }

        // Create or get the observable for this instrument
        instrumentObservables.computeIfAbsent(
                symbol,
                this::createInstrumentObservable
        );

        // If the user has an active session, start sending updates for the new subscription
        Set<WebSocketSession> sessions = userSessions.get(username);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                subscribeSessionToSymbol(
                        username,
                        session,
                        symbol
                );
            }
        }
    }

    private void sendCachedUpdate(
            String username,
            String symbol,
            InstrumentData data
    ) {
        Set<WebSocketSession> sessions = userSessions.get(username);
        if (sessions != null) {
            // Iterate through each session associated with the user
            for (WebSocketSession session : sessions) {
                // Send the updated instrument data to each active session
                sendUpdate(
                        session,
                        data
                );
            }
        }
    }

    public void unsubscribeFromInstrument(
            String username,
            String symbol
    ) {
        Set<String> subscriptions = userSubscriptions.get(username);
        if (subscriptions != null) // Subscriptions were found associated with user
        {
            subscriptions.remove(symbol); // Unsubscribe specific instrument
            if (subscriptions.isEmpty()) // Haven't subscribed anything yet?
            {
                userSubscriptions.remove(username); // Remove user from subscriptions at all
            }
        }

        // Stop sending updates for the unsubscribed symbol
        Set<WebSocketSession> sessions = userSessions.get(username);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                unsubscribeSessionFromSymbol(
                        username,
                        session,
                        symbol
                );
            }
        }
    }

    // Start sending fetched financial instrument data to specified user
    public void startSendingUpdates(
            String username,
            WebSocketSession session
    ) {
        userSessions.computeIfAbsent(
                        username,
                        k -> ConcurrentHashMap.newKeySet()
                )
                .add(session);
        userSessionSubscriptions.computeIfAbsent(
                username,
                k -> new ConcurrentHashMap<>()
        );

        Set<String> subscriptions = userSubscriptions.get(username);
        if (subscriptions != null) {
            for (String symbol : subscriptions) {
                subscribeSessionToSymbol(
                        username,
                        session,
                        symbol
                );
            }
        }
    }

    // Stop sending fetched financial instrument data to specified user
    public void stopSendingUpdates(
            String username,
            WebSocketSession session
    ) {
        Set<WebSocketSession> sessions = userSessions.get(username);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(username);
            }
        }

        Map<WebSocketSession, Map<String, Disposable>> sessionSubscriptions = userSessionSubscriptions.get(username);
        if (sessionSubscriptions != null) {
            Map<String, Disposable> symbolDisposables = sessionSubscriptions.remove(session);
            if (symbolDisposables != null) {
                symbolDisposables.values()
                        .forEach(Disposable::dispose);
            }
            if (sessionSubscriptions.isEmpty()) {
                userSessionSubscriptions.remove(username);
            }
        }
    }

    // Instrument can be observed, this creates observable for specific type of instrument with corresponding symbol
    private Observable<InstrumentData> createInstrumentObservable(String symbol) {
        return Observable.interval(
                        0,
                        5,
                        // Every 5 seconds
                        TimeUnit.SECONDS
                )
                /*
                 * Example:
                 *   If the interval Observable emits [0, 1, 2], this flatMap might produce:
                 *   0 -> InstrumentData{symbol="AAPL", price=150.25}
                 *   1 -> InstrumentData{symbol="AAPL", price=151.30}
                 *   2 -> InstrumentData{symbol="AAPL", price=149.80}
                 *
                 */
                .flatMap(k -> Observable.fromCallable(() -> fetchInstrumentData(symbol))
                        .subscribeOn(Schedulers.io()))
                .doOnNext(data ->
                {
                    // Update the cache if the instrument is popular
                    if (isPopularInstrument(symbol)) {
                        instrumentCache.put(
                                symbol,
                                data
                        );
                    }
                })
                .share(); // Allow multiple subscribers to share the same subscription
    }


    // Checks if the most user's subscribe this instrument
    private boolean isPopularInstrument(String symbol) {
        int subscriberCount = (int) userSubscriptions.values()
                .stream()
                // List of userSubscriptions containing instruments
                .filter(symbols -> symbols.contains(symbol))
                .count();
        return subscriberCount >= POPULARITY_THRESHOLD;
    }

    private void subscribeSessionToSymbol(
            String username,
            WebSocketSession session,
            String symbol
    ) {
        Observable<InstrumentData> observable = instrumentObservables.get(symbol);
        if (observable != null) {
            Disposable disposable = observable.subscribe( // subscribe the specific instrument
                    data -> sendUpdate(
                            session,
                            data
                    ),
                    error -> handleError(
                            session,
                            error
                    )
            );
            userSessionSubscriptions
                    .computeIfAbsent(
                            username,
                            k -> new ConcurrentHashMap<>()
                    )
                    .computeIfAbsent(
                            session,
                            k -> new ConcurrentHashMap<>()
                    )
                    .put(
                            symbol,
                            disposable
                    );
        }
    }

    private void unsubscribeSessionFromSymbol(
            String username,
            WebSocketSession session,
            String symbol
    ) {
        Map<WebSocketSession, Map<String, Disposable>> sessionSubscriptions = userSessionSubscriptions.get(username);
        if (sessionSubscriptions != null) {
            Map<String, Disposable> symbolDisposables = sessionSubscriptions.get(session);
            if (symbolDisposables != null) {
                Disposable disposable = symbolDisposables.remove(symbol);
                if (disposable != null) {
                    disposable.dispose();
                }
                if (symbolDisposables.isEmpty()) {
                    sessionSubscriptions.remove(session);
                }
            }
            if (sessionSubscriptions.isEmpty()) {
                userSessionSubscriptions.remove(username);
            }
        }
    }

    private void sendUpdate(
            WebSocketSession session,
            InstrumentData data
    ) {
        try {
            session.sendMessage(new TextMessage(data.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleError(
            WebSocketSession session,
            Throwable error
    ) {
        try {
            session.sendMessage(new TextMessage("Error: " + error.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InstrumentData fetchInstrumentData(String symbol) {
        // First, check if the data is in the cache
        InstrumentData cachedData = instrumentCache.get(symbol);
        if (cachedData != null) {
            return cachedData;
        }

        // If not in cache, fetch from the API
        String url = apiUrl + "/" + symbol;
        InstrumentData data = restTemplate.getForObject(
                url,
                InstrumentData.class
        );

        // Update the cache if the instrument is popular
        if (isPopularInstrument(symbol)) {
            instrumentCache.put(
                    symbol,
                    data
            );
        }

        return data;
    }

}