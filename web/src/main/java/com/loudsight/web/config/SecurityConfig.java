package com.loudsight.web.config;

import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.web.AuthenticationEvent;
import com.loudsight.web.AuthenticationListener;
import com.loudsight.web.handler.CustomAuthenticationSuccessHandler;
import com.loudsight.web.handler.CustomOAuth2AccessTokenResponseBodyExtractor;
import com.loudsight.web.handler.CustomServerLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

import java.util.List;

@EnableWebSecurity
public class SecurityConfig {
    private static final LoggingHelper logger = LoggingHelper.wrap(SecurityConfig.class);

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user =  User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    AuthenticationListener authenticationListener() {
        return new AuthenticationListener() {

            @Override
            public void accept(AuthenticationEvent event) {
                logger.logInfo("Received provider " + event.provider() + "Received user " + event.username());
            }
        };
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http,
                                         SecurityContextRepository serverSecurityContextRepository,
                                         @Qualifier("unsecuredPaths") List<String> unsecuredPaths/*,
                                         ,ReactiveClientRegistrationRepository clientRegistrationRepository
                                            X forwardedHeaderFilter*/
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
//                .addFilterAt(forwardedHeaderFilter::filter,
//                        SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(unsecuredPaths.toArray(new String[]{})).permitAll()
                        .anyRequest().authenticated()
                )
//                .oauth2Login(oauth2LoginCustomizer(serverSecurityContextRepository))
//                .formLogin().authenticationSuccessHandler(customAuthenticationSuccessHandler())
//                .and().logout().logoutSuccessHandler(customServerLogoutSuccessHandler(clientRegistrationRepository))
//                .and().oauth2Client(Customizer.withDefaults())
                .build();
    }


//    @Bean
//    public WebClientReactiveAuthorizationCodeTokenResponseClient webClientReactiveAuthorizationCodeTokenResponseClient() {
//        WebClientReactiveAuthorizationCodeTokenResponseClient webClientReactiveAuthorizationCodeTokenResponseClient =
//                new WebClientReactiveAuthorizationCodeTokenResponseClient();
//        webClientReactiveAuthorizationCodeTokenResponseClient.setBodyExtractor(new CustomOAuth2AccessTokenResponseBodyExtractor());
//        return webClientReactiveAuthorizationCodeTokenResponseClient;
//    }

    @Bean
    public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(authenticationListener());
    }

//    @Bean
//    public CustomServerLogoutSuccessHandler customServerLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
//        return new CustomServerLogoutSuccessHandler(authenticationListener(), clientRegistrationRepository);
//    }

    Customizer<ServerHttpSecurity.OAuth2LoginSpec> oauth2LoginCustomizer(
            ServerSecurityContextRepository serverSecurityContextRepository) {
        return (oauth2Login) -> {
            oauth2Login.securityContextRepository(serverSecurityContextRepository)
                    .authenticationSuccessHandler(customAuthenticationSuccessHandler());
        };
    }
}
