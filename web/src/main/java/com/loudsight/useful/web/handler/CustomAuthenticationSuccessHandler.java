package com.loudsight.useful.web.handler;

import com.loudsight.useful.web.AuthenticationEvent;
import com.loudsight.useful.web.AuthenticationListener;
import com.loudsight.useful.web.AuthenticationProvider;
import com.loudsight.useful.web.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

public class CustomAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final AuthenticationListener authenticationListener;

    private URI location = URI.create("/");
    private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
    private ServerRequestCache requestCache = new WebSessionServerRequestCache();

    public CustomAuthenticationSuccessHandler(AuthenticationListener authenticationListener, String location) {
        this.location = URI.create(location);
        this.authenticationListener = authenticationListener;
    }

    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        this.authenticationListener.accept(new AuthenticationEvent() {
            @Override
            public String username() {
                return SecurityUtils.getUsername(authentication);
            }

            @Override
            public AuthenticationProvider provider() {
                return getProvider(exchange);
            }
        });
        return this.requestCache.getRedirectUri(exchange).defaultIfEmpty(this.location).flatMap((location) -> {
            return this.redirectStrategy.sendRedirect(exchange, location);
        });
    }

    public void setRequestCache(ServerRequestCache requestCache) {
        Assert.notNull(requestCache, "requestCache cannot be null");
        this.requestCache = requestCache;
    }

    public void setLocation(URI location) {
        Assert.notNull(location, "location cannot be null");
        this.location = location;
    }

    public void setRedirectStrategy(ServerRedirectStrategy redirectStrategy) {
        Assert.notNull(redirectStrategy, "redirectStrategy cannot be null");
        this.redirectStrategy = redirectStrategy;
    }

    private AuthenticationProvider getProvider(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        if(path.contains("facebook"))
            return AuthenticationProvider.FACEBOOK;

        if(path.contains("google"))
            return AuthenticationProvider.GOOGLE;

        if(path.contains("github"))
            return AuthenticationProvider.GITHUB;

        if(path.contains("linkedin"))
            return AuthenticationProvider.LINKEDIN;

        return AuthenticationProvider.FORM;
    }
}