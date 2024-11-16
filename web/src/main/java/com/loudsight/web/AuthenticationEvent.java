package com.loudsight.web;

public interface AuthenticationEvent {
    String username();

    AuthenticationProvider provider();
}
