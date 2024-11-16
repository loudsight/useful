package com.loudsight.web;

import java.util.function.Consumer;

public interface AuthenticationListener extends Consumer<AuthenticationEvent> {

    /**
     * This method is called when a user authentication event occurs
     *
     * @param authenticationEvent login (success and failure), logout event
     */
    @Override
    void accept(AuthenticationEvent authenticationEvent);
}
