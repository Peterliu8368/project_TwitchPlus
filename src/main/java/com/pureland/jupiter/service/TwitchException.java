package com.pureland.jupiter.service;

// use to help us identify errors
public class TwitchException extends RuntimeException {
    public TwitchException(String errorMessage) {
        super(errorMessage);
    }
}
