package org.devexperts.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.devexperts.model.Message;
import org.devexperts.model.User;
import org.devexperts.service.MessageService;
import org.devexperts.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ProtectedController {
    private static final Logger logger = LoggerFactory.getLogger(ProtectedController.class);
    private static final String myLog = "[!!!MY_LOG!!!]";
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @GetMapping("/subscriptions")
    public List<String> getUserSubscriptions(
            HttpServletRequest request
    ) {
        String username = (String) request.getAttribute("username");
        logger.info("{} User {} requested his / her subscriptions", myLog, username);
        List<String> subscriptions = userService.getUserByUsername(username).getSubscribedSymbols();
        logger.info("{} User {} got subscribed symbols {}", myLog, username, subscriptions);
        return subscriptions;
    }


    @GetMapping("/users")
    public List<String> getUsernames(
            HttpServletRequest request
    ) {
        String username = (String) request.getAttribute("username");
        logger.info("{} User {} requested list of available usernames", myLog, username);

        return userService
                .getAllUsernames()
                .stream()
                .map(User::getUsername)
                .filter(e -> !e.equals(username))
                .toList();
    }


    /**
     * Endpoint for retrieving conversation history between the initiator of the request and recipient which is the
     * one at the second end of the conversation
     *
     * @param recipient with who do we have conversation.
     * @param request
     * @return List of message objects indicating sender content and recipient in it.
     */
    /* curl -X GET "http://localhost:8081/api/history?recipient=newuser2" -H "Authorization: Bearer YOUR_JWT_TOKEN" */
    @GetMapping("/history")
    public List<Message> getMessageHistory(
            @RequestParam("recipient") String recipient,
            HttpServletRequest request
    ) {
        String sender = (String) request.getAttribute("username");
        logger.info("{} User {} requested his/her messages with user {}", myLog, sender, recipient);

        List<Message> messages = messageService.getMessagesBetweenUsers(sender, recipient);

        logger.info("{} User {} got messages {} with user {}", myLog, sender, messages, recipient);

        return messages;
    }


}