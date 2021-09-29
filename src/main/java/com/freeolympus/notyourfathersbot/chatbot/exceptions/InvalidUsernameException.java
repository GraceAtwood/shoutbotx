package com.freeolympus.notyourfathersbot.chatbot.exceptions;

import lombok.Getter;

import static java.lang.String.format;

public class InvalidUsernameException extends Exception {

    @Getter
    private String username;

    public InvalidUsernameException(String username) {
        super(format("The text '%s' is an invalid username!", username));

        this.username = username;
    }
}
