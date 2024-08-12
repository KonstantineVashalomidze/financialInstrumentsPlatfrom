package org.devexpers.mockDataService;

import org.devexpers.mockDataService.dataGenerator.DataGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Konstantine Vashalomidze
 *
 * This application is responsible for providing random generated data for testing purposes.
 */
@SpringBootApplication
public class MockDataServiceApplication
{
    public static void main ( String[] args )
    {
        SpringApplication.run(
                MockDataServiceApplication.class,
                args
                             );
    }

    @Bean
    public DataGenerator dataGenerator ( )
    {
        return new DataGenerator( );
    }






}