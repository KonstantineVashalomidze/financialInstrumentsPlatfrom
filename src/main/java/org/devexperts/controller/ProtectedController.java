package org.devexperts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.devexperts.model.Message;
import org.devexperts.model.User;
import org.devexperts.service.MessageService;
import org.devexperts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ProtectedController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;


    @Operation(summary = "Retrieve subscribed symbols", description = "Returns a list of subscribed symbols for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/subscriptions")
    public List<String> getUserSubscriptions(
            HttpServletRequest request
    ) {
        String username = (String) request.getAttribute("username");
        List<String> subscriptions = userService.getUserByUsername(username).getSubscribedSymbols();
        return subscriptions;
    }

    @Operation(summary = "Retrieve all usernames", description = "Returns a list of all usernames except the authenticated user's.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/users")
    public List<String> getUsernames(
            HttpServletRequest request
    ) {
        String username = (String) request.getAttribute("username");

        return userService
                .getAllUsernames()
                .stream()
                .map(User::getUsername)
                .filter(e -> !e.equals(username))
                .toList();
    }

    @Operation(summary = "Retrieve message history", description = "Returns the message history between the authenticated user and a specified recipient.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Message.class))))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/history")
    public List<Message> getMessageHistory(
            @RequestParam("recipient") String recipient,
            HttpServletRequest request
    ) {
        String sender = (String) request.getAttribute("username");

        List<Message> messages = messageService.getMessagesBetweenUsers(sender, recipient);


        return messages;
    }


}