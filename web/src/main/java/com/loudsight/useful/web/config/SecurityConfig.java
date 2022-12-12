package com.loudsight.useful.web.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity
public class SecurityConfig {
//    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);



    class X extends ForwardedHeaderTransformer implements WebFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            if (hasForwardedHeaders(request)) {
                ServerWebExchange mutatedExchange = exchange.mutate().request(apply(request)).build();

                return chain.filter(mutatedExchange);
            }
            return chain.filter(exchange);
        }
    }

    @Bean
    X forwardedHeaderTransformer() {
        return new X();
    }

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http,
                                            ServerSecurityContextRepository serverSecurityContextRepository,
                                            @Qualifier("unsecuredPaths") List<String> unsecuredPaths,
                                            X forwardedHeaderFilter
    ) {
        return http
                .csrf().disable()
                .addFilterAt(forwardedHeaderFilter::filter,
                        SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers(unsecuredPaths.toArray(new String[]{})).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2LoginCustomizer(serverSecurityContextRepository))
//                .oauth2Client(oauth2ClientCustomizer())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (exchange, ex) -> {
                                    var loginPath = "/login";
                                    AtomicReference<URI> newLocation = new AtomicReference<>(URI.create(loginPath));
                                    var response = exchange.getResponse();

                                    response.setStatusCode(HttpStatus.FOUND);
                                    response.getHeaders().setLocation(newLocation.get());
                                    return response.writeWith(Mono.just(response.bufferFactory().wrap(
                                            ("Redirecting to " + newLocation.get()).getBytes(StandardCharsets.UTF_8)
                                    )));
                                }
                        )
                )
                .formLogin().disable()
                .oauth2Client(withDefaults())
                .build();
    }

    Customizer<ServerHttpSecurity.OAuth2LoginSpec> oauth2LoginCustomizer(
            ServerSecurityContextRepository serverSecurityContextRepository) {
        return (oauth2Login) -> {
            oauth2Login.securityContextRepository(serverSecurityContextRepository);
        };
    }


//    Customizer<ServerHttpSecurity.OAuth2ClientSpec> oauth2ClientCustomizer() {
//        return oAuth2ClientSpec -> oAuth2ClientSpec.authenticationConverter(new ServerAuthenticationConverter() {
//            @Override
//            public Mono<Authentication> convert(ServerWebExchange exchange) {
//                return null;
//            }
//        });
//    }

//    @Bean
//    SecurityContextRepository securityContextRepository() {
//        @Component
//        class Handler {
//            Mono<ServerResponse> all(ServerRequest request) {
//                return ReactiveSecurityContextHolder.getContext()
//                        .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveSecurityContext is empty")))
//                        .map(SecurityContext::getAuthentication)
//                        .map(Authentication::getName)
//                        .flatMap(s -> Mono.just("Hi " + s))
//                        .doOnNext(System.out::println)
//                        .doOnError(Throwable::printStackTrace)
//                        .doOnSuccess(s -> System.out.println("completed without value: " + s))
//                        .flatMap(s -> ServerResponse.ok().build());
//            }
//        }
//    }
}
