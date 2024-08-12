package org.devexpers.mockDataService.dataGenerator;

import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataGenerator
{
    private Map< String, InstrumentData >    instrumentData; // Mapping from instrument name to corresponding data
    private Random                           random;
    private ScheduledExecutorService         scheduler;

    public DataGenerator ( )
    {
        this.instrumentData = new ConcurrentHashMap<>( );
        this.random         = new Random( );
        initializeInstruments( );
        startDataGeneration( );
    }

    /* Populating instrument data with initial data */
    private void initializeInstruments ( )
    {
        long currentTime = System.currentTimeMillis( );
        instrumentData.put(
                "AAPL",
                new InstrumentData(
                        "AAPL",
                        150.0,
                        currentTime
                )
                          );
        instrumentData.put(
                "GOOGL",
                new InstrumentData(
                        "GOOGL",
                        2800.0,
                        currentTime
                )
                          );
        instrumentData.put(
                "AAPL",
                new InstrumentData(
                        "AAPL",
                        150.0,
                        currentTime
                )
                          );
        instrumentData.put(
                "GOOGL",
                new InstrumentData(
                        "GOOGL",
                        2800.0,
                        currentTime
                )
                          );
        instrumentData.put(
                "MSFT",
                new InstrumentData(
                        "MSFT",
                        305.5,
                        currentTime
                )
                          );
        instrumentData.put(
                "AMZN",
                new InstrumentData(
                        "AMZN",
                        3300.75,
                        currentTime
                )
                          );
        instrumentData.put(
                "FB",
                new InstrumentData(
                        "FB",
                        330.25,
                        currentTime
                )
                          );
        instrumentData.put(
                "TSLA",
                new InstrumentData(
                        "TSLA",
                        725.6,
                        currentTime
                )
                          );
        instrumentData.put(
                "NVDA",
                new InstrumentData(
                        "NVDA",
                        220.8,
                        currentTime
                )
                          );
        instrumentData.put(
                "JPM",
                new InstrumentData(
                        "JPM",
                        155.3,
                        currentTime
                )
                          );
        instrumentData.put(
                "V",
                new InstrumentData(
                        "V",
                        230.45,
                        currentTime
                )
                          );
        instrumentData.put(
                "JNJ",
                new InstrumentData(
                        "JNJ",
                        170.2,
                        currentTime
                )
                          );
        instrumentData.put(
                "WMT",
                new InstrumentData(
                        "WMT",
                        140.9,
                        currentTime
                )
                          );
        instrumentData.put(
                "PG",
                new InstrumentData(
                        "PG",
                        135.7,
                        currentTime
                )
                          );
        instrumentData.put(
                "DIS",
                new InstrumentData(
                        "DIS",
                        175.3,
                        currentTime
                )
                          );
        instrumentData.put(
                "NFLX",
                new InstrumentData(
                        "NFLX",
                        530.6,
                        currentTime
                )
                          );
        instrumentData.put(
                "PYPL",
                new InstrumentData(
                        "PYPL",
                        275.4,
                        currentTime
                )
                          );
    }

    /* Starts generating random data */
    private void startDataGeneration ( )
    {
        scheduler = Executors.newSingleThreadScheduledExecutor( );
        scheduler.scheduleAtFixedRate(
                this::generateAllData,
                0,
                ( int ) ( Math.random( ) * 30 + 1 ), // In every random seconds from 1 to 31
                TimeUnit.SECONDS
                                     );
    }

    /* Responsible for updating data randomly */
    private void generateAllData ( )
    {
        instrumentData.forEach( ( symbol, data ) ->
                                {
                                    double priceChange = ( random.nextDouble( ) - 0.5 ) * 2; // -1% to +1%
                                    data.setPrice( data.getPrice( ) * ( 1 + priceChange / 100 ) ); // New price
                                    data.setTimeStamp( System.currentTimeMillis( ) ); // Last updating time
                                } );
    }


    public InstrumentData getData ( String symbol )
    {
        return instrumentData.get( symbol );
    }


}