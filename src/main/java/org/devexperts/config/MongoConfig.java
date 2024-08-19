package org.devexperts.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongoconfig.database.name:Cluster0}")
    private String DATABASE_NAME;
    @Value("${mongoconfig.connection.string:mongodb+srv://vashalomidzekonstantine:QuC5BLoX59mn137U@cluster0.tvzxq.mongodb.net/}")
    private String CONNECTION_STRING;

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());

        // Create indexes
        IndexOperations userIndexOps = mongoTemplate.indexOps("users");
        IndexOperations messageIndexOps = mongoTemplate.indexOps("messages");

        // User indexes
        userIndexOps.ensureIndex(new Index().on("username", Sort.Direction.ASC).unique());
        userIndexOps.ensureIndex(new Index().on("subscribedSymbols", Sort.Direction.ASC));
        userIndexOps.ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));
        userIndexOps.ensureIndex(new Index().on("updatedAt", Sort.Direction.DESC));

        // Message indexes
        messageIndexOps.ensureIndex(new Index().on("senderId", Sort.Direction.ASC));
        messageIndexOps.ensureIndex(new Index().on("receiverId", Sort.Direction.ASC));
        messageIndexOps.ensureIndex(new Index().on("timestamp", Sort.Direction.DESC));

        return mongoTemplate;
    }

    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    protected String getDatabaseName() {
        return DATABASE_NAME;
    }
}