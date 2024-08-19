package org.devexperts.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.devexperts.model.InstrumentData;
import org.devexperts.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class InstrumentDataService {
    private final RestTemplate restTemplate;
    private final Map<String, Observable<InstrumentData>> instrumentObservables;
    private final Map<String, Map<String, Disposable>> userSubscriptions;
    private final Map<String, WebSocketSession> userSessions;
    private final Map<String, InstrumentData> instrumentCache;
    @Autowired
    private UserService userService;
    @Value("${api.mockdata.url}")
    private String apiUrl;

    public InstrumentDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.instrumentObservables = new ConcurrentHashMap<>();
        this.userSessions = new ConcurrentHashMap<>();
        this.userSubscriptions = new ConcurrentHashMap<>();
        this.instrumentCache = new ConcurrentHashMap<>();
    }

    public void subscribeToInstrument(String username, String symbol) {
        userSubscriptions.computeIfAbsent(username, k -> new ConcurrentHashMap<>());

        InstrumentData cachedData = instrumentCache.get(symbol);
        if (cachedData != null) {
            sendCachedUpdate(username, symbol, cachedData);
        }

        Observable<InstrumentData> observable = instrumentObservables.computeIfAbsent(symbol, this::createInstrumentObservable);

        WebSocketSession session = userSessions.get(username);
        if (session != null) {
            subscribeSessionToSymbol(username, session, symbol, observable);
        }
    }

    private void sendCachedUpdate(String username, String symbol, InstrumentData data) {
        WebSocketSession session = userSessions.get(username);
        if (session != null) {
            sendUpdate(session, data);
        }
    }

    private Observable<InstrumentData> createInstrumentObservable(String symbol) {
        return Observable.interval(0, 5, TimeUnit.SECONDS)
                .flatMap(k -> Observable.fromCallable(() -> fetchInstrumentData(symbol))
                        .subscribeOn(Schedulers.io()))
                .doOnNext(data -> {
                    if (isPopularInstrument(symbol)) {
                        instrumentCache.put(symbol, data);
                    }
                })
                .share();
    }

    private void subscribeSessionToSymbol(String username, WebSocketSession session, String symbol, Observable<InstrumentData> observable) {
        Disposable disposable = observable.subscribe(
                data -> sendUpdate(session, data),
                error -> handleError(session, error)
        );
        userSubscriptions.computeIfAbsent(username, k -> new ConcurrentHashMap<>())
                .put(symbol, disposable);
    }

    private synchronized void sendUpdate(WebSocketSession session, InstrumentData data) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(data.toString()));
            } catch (IOException e) {

            }
        }
    }

    private InstrumentData fetchInstrumentData(String symbol) {
        InstrumentData cachedData = instrumentCache.get(symbol);
        if (cachedData != null) {
            return cachedData;
        }

        String url = apiUrl + "/" + symbol;
        InstrumentData data = restTemplate.getForObject(url, InstrumentData.class);

        if (isPopularInstrument(symbol)) {
            instrumentCache.put(symbol, data);
        }

        return data;
    }

    private boolean isPopularInstrument(String symbol) {
        int subscriberCount = (int) userSubscriptions.values().stream()
                .filter(symbols -> symbols.containsKey(symbol))
                .count();
        return subscriberCount >= 5; // More than 5 client subscribes this instrument could be changed
    }

    private void handleError(WebSocketSession session, Throwable error) {
        try {
            session.sendMessage(new TextMessage("Error: " + error.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeFromInstrument(String username, String symbol) {
        Map<String, Disposable> subscriptions = userSubscriptions.get(username);
        if (subscriptions != null) {
            Disposable disposable = subscriptions.remove(symbol);
            if (disposable != null) {
                disposable.dispose();
            }
            if (subscriptions.isEmpty()) {
                userSubscriptions.remove(username);
            }
        }
    }

    public void startSendingUpdates(String username, WebSocketSession session) {
        User user = userService.getUserByUsername(username);
        List<String> subscribedSymbols = user.getSubscribedSymbols();

        userSessions.put(username, session);

        for (String symbol : subscribedSymbols) {
            // Fetch and send the current data immediately
            InstrumentData currentData = fetchInstrumentData(symbol);
            sendUpdate(session, currentData);

            // Then set up the subscription for future updates
            Observable<InstrumentData> observable = instrumentObservables.computeIfAbsent(symbol, this::createInstrumentObservable);
            subscribeSessionToSymbol(username, session, symbol, observable);
        }
    }

    public void stopSendingUpdates(String username) {
        WebSocketSession session = userSessions.remove(username);
        Map<String, Disposable> subscriptions = userSubscriptions.remove(username);
        if (subscriptions != null) {
            subscriptions.values().forEach(disposable -> {
                if (disposable != null) {
                    disposable.dispose();
                }
            });
        }
    }
}