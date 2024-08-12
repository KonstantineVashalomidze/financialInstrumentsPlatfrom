package org.devexperts.repository;

import org.devexperts.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

    @Query("{ 'subscribedSymbols': { $in: [?0] } }")
    List<User> findBySubscribedSymbol(String symbol);

    @Query("{ 'subscribedSymbols': { $all: ?0 } }")
    List<User> findByAllSubscribedSymbols(List<String> symbols);
}