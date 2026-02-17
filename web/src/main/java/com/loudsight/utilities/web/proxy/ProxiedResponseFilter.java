package com.loudsight.utilities.web.proxy;

@FunctionalInterface
public interface ProxiedResponseFilter {
    void filter(ProxiedRequest requestContext, ProxiedResponse responseContext);

    }
