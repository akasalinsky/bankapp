package com.example.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenRelayFilter implements GlobalFilter {

    @Autowired
    private ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(authentication -> {
                    String clientRegistrationId = authentication.getAuthorizedClientRegistrationId();
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId(clientRegistrationId)
                            .principal(authentication)
                            .build();

                    return authorizedClientManager.authorize(authorizeRequest);
                })
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(accessToken -> exchange.getRequest().mutate()
                        .header("Authorization", "Bearer " + accessToken.getTokenValue())
                        .build())
                .defaultIfEmpty(exchange.getRequest())
                .flatMap(request -> chain.filter(exchange.mutate().request(request).build()));
    }
}