package com.loudsight.web.handler;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import net.minidev.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import reactor.core.publisher.Mono;

import java.util.*;

public class CustomOAuth2AccessTokenResponseBodyExtractor implements BodyExtractor<Mono<OAuth2AccessTokenResponse>, ReactiveHttpInputMessage> {
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
    };

    public CustomOAuth2AccessTokenResponseBodyExtractor() {
    }

    @Override
    public Mono<OAuth2AccessTokenResponse> extract(ReactiveHttpInputMessage inputMessage, Context context) {
        BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate = BodyExtractors
                .toMono(STRING_OBJECT_MAP);
        return delegate.extract(inputMessage, context)
                .onErrorMap((ex) -> new OAuth2AuthorizationException(
                        invalidTokenResponse("An error occurred parsing the Access Token response: " + ex.getMessage()),
                        ex))
                .switchIfEmpty(Mono.error(() -> new OAuth2AuthorizationException(
                        invalidTokenResponse("Empty OAuth 2.0 Access Token Response"))))
                .map(CustomOAuth2AccessTokenResponseBodyExtractor::parse)
                .flatMap(CustomOAuth2AccessTokenResponseBodyExtractor::oauth2AccessTokenResponse)
                .map(CustomOAuth2AccessTokenResponseBodyExtractor::oauth2AccessTokenResponse);
    }


    private static TokenResponse parse(Map<String, Object> json) {
        json.put("token_type", "Bearer");
        try {
            return TokenResponse.parse(new JSONObject(json));
        } catch (ParseException var3) {
            OAuth2Error oauth2Error = invalidTokenResponse("An error occurred parsing the Access Token response: " + var3.getMessage());
            throw new OAuth2AuthorizationException(oauth2Error, var3);
        }
    }

    private static OAuth2Error invalidTokenResponse(String message) {
        return new OAuth2Error("invalid_token_response", message, null);
    }

    private static Mono<AccessTokenResponse> oauth2AccessTokenResponse(TokenResponse tokenResponse) {
        if (tokenResponse.indicatesSuccess()) {
            return Mono.just(tokenResponse).cast(AccessTokenResponse.class);
        } else {
            TokenErrorResponse tokenErrorResponse = (TokenErrorResponse)tokenResponse;
            ErrorObject errorObject = tokenErrorResponse.getErrorObject();
            OAuth2Error oauth2Error = getOAuth2Error(errorObject);
            return Mono.error(new OAuth2AuthorizationException(oauth2Error));
        }
    }

    private static OAuth2Error getOAuth2Error(ErrorObject errorObject) {
        if (errorObject == null) {
            return new OAuth2Error("server_error");
        } else {
            String code = errorObject.getCode() != null ? errorObject.getCode() : "server_error";
            String description = errorObject.getDescription();
            String uri = errorObject.getURI() != null ? errorObject.getURI().toString() : null;
            return new OAuth2Error(code, description, uri);
        }
    }

    private static OAuth2AccessTokenResponse oauth2AccessTokenResponse(AccessTokenResponse accessTokenResponse) {
        AccessToken accessToken = accessTokenResponse.getTokens().getAccessToken();
        OAuth2AccessToken.TokenType accessTokenType = null;
        if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(accessToken.getType().getValue())) {
            accessTokenType = OAuth2AccessToken.TokenType.BEARER;
        }

        long expiresIn = accessToken.getLifetime();
        Set<String> scopes = accessToken.getScope() != null ? new LinkedHashSet<>(accessToken.getScope().toStringList()) : Collections.emptySet();
        String refreshToken = null;
        if (accessTokenResponse.getTokens().getRefreshToken() != null) {
            refreshToken = accessTokenResponse.getTokens().getRefreshToken().getValue();
        }

        Map<String, Object> additionalParameters = new LinkedHashMap<>(accessTokenResponse.getCustomParameters());
        return OAuth2AccessTokenResponse.withToken(accessToken.getValue()).tokenType(accessTokenType).expiresIn(expiresIn).scopes(scopes).refreshToken(refreshToken).additionalParameters(additionalParameters).build();
    }
}