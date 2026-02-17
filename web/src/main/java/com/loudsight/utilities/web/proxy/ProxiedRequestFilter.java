package com.loudsight.utilities.web.proxy;

@FunctionalInterface
public interface ProxiedRequestFilter {
    void filter(ProxiedRequest requestContext);
}
