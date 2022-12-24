package com.loudsight.useful.web.handler;

import com.loudsight.useful.web.AuthenticationEvent;
import com.loudsight.useful.web.AuthenticationListener;
import com.loudsight.useful.web.AuthenticationProvider;
import com.loudsight.useful.web.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomServerLogoutSuccessHandler extends OidcClientInitiatedServerLogoutSuccessHandler {

    private final AuthenticationListener authenticationListener;
    public CustomServerLogoutSuccessHandler(AuthenticationListener authenticationListener, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        super(clientRegistrationRepository);
        this.authenticationListener = authenticationListener;
    }


    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
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
        return super.onLogoutSuccess(webFilterExchange,authentication);
    }

    private AuthenticationProvider getProvider(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().contextPath().value();
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