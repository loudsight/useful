package com.loudsight.utilities.web.proxy;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.KeyStore;
import java.util.Map;

public class UrlProxyFactory {
    private final SSLContext sslContext;
    private final Map<String, UrlProxyRoutes.UrlRoute> urlRouteMap;

    public UrlProxyFactory(Map<String, UrlProxyRoutes.UrlRoute> urlRouteMap) {
        this(urlRouteMap, null);
    }

    private UrlProxyFactory(Map<String, UrlProxyRoutes.UrlRoute> urlRouteMap,
                            SSLContext sslContext) {
        this.sslContext = sslContext;
        this.urlRouteMap = urlRouteMap;
    }

    public UrlProxyFactory(
            Map<String, UrlProxyRoutes.UrlRoute> urlRouteMap,
            String keyStoreType,
            InputStream keyStoreInputStream,
            String keyStorePassword) throws Exception {
        this(urlRouteMap,
                getSSLContext(keyStoreType, keyStoreInputStream, keyStorePassword)
        );

    }

    private static SSLContext getSSLContext(String keyStoreType,
                                            InputStream keyStoreInputStream,
                                            String keyStorePassword) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(getKeyManagers(keyStoreType, keyStoreInputStream, keyStorePassword).getKeyManagers(), null, null); // Use default trust manager
        return sslContext;
    }

    private static KeyManagerFactory getKeyManagers(String keyStoreType, InputStream keyStoreFile, String keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        return kmf;
    }

    public <T> T proxy(URI url, Class<T> klass, ProxiedRequestFilter... filters) {
        RestClient.Builder builder = RestClient.builder();

        if (sslContext != null) {
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
            ClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            builder.requestFactory(requestFactory);
        }
//        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
//                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build();

        RestClient webClient = builder
//                .exchangeStrategies(exchangeStrategies)
                .baseUrl(url.toString())
                .build();

        return proxy(webClient, klass, filters);
    }

    public <T> T proxy(RestClient webClient, Class<T> klass, ProxiedRequestFilter... filters) {
        return (T) Proxy.newProxyInstance(klass.getClassLoader(),
                new Class[]{klass},
                new UrlProxyHandler(klass, webClient, urlRouteMap, filters)
        );
    }
}
