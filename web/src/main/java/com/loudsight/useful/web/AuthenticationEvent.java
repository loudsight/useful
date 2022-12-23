package com.loudsight.useful.web;

public interface AuthenticationEvent {
    String username();

    AuthenticationProvider provider();
}
