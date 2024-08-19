package org.devexperts.service;

import org.devexperts.model.User;
import org.devexperts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    public User createUser(User user) {
        user.setPassword(user.getPassword());
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsernames() {
        return userRepository.findAll();
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public User addSubscription(String username, String symbol) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.getSubscribedSymbols().add(symbol);
            user.setUpdatedAt(new Date());
            return userRepository.save(user);
        }
        return null;
    }

    public User removeSubscription(String username, String symbol) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.getSubscribedSymbols().remove(symbol);
            user.setUpdatedAt(new Date());
            return userRepository.save(user);
        }
        return null;
    }

    public List<User> getUsersBySubscribedSymbol(String symbol) {
        return userRepository.findBySubscribedSymbol(symbol);
    }
}