package com.loudsight.utilities.web.proxy;

public class ProxiedRequest {
    private final Headers headers = new Headers();

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "Headers is a mutable sub-component that ProxiedRequestFilter "
                    + "implementations populate in place before the request is sent; handing "
                    + "back the live instance is the intended contract of this builder-style type.")
    public Headers getHeaders() {
        return headers;
    }
}
