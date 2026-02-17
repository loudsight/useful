package com.loudsight.utilities.web.proxy;

import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.meta.serialization.EntityTransform;
import com.loudsight.useful.helper.JvmClassHelper;
import com.loudsight.useful.helper.ExceptionHelper;
import com.loudsight.utilities.json.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UrlProxyHandler<T> implements InvocationHandler {
    private static final LoggingHelper logger = LoggingHelper.wrap(MethodHandles.lookup().lookupClass());

    private final RestClient client;
    private final ProxiedRequestFilter[] filters;
    Class<T> klass;
    private final Map<String, UrlProxyRoutes.UrlRoute> urlRouteMap;

    public UrlProxyHandler(Class<T> klass,
                           RestClient client,
                           Map<String, UrlProxyRoutes.UrlRoute> urlRouteMap,
                           ProxiedRequestFilter... filters) {
        this.klass = klass;
        this.client = client;
        this.filters = Stream.of(filters).toList().toArray(new ProxiedRequestFilter[0]);
        this.urlRouteMap = urlRouteMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if ("equals".equals(method.getName())) {
            return proxy == args[0];
        }
        var urlRoute = urlRouteMap.get(method.getName());

        try {
            ProxiedRequest request = new ProxiedRequest();

            for (ProxiedRequestFilter filter : filters) {
                filter.filter(request);
            }
            RestClient.RequestHeadersUriSpec<?> reqUriSpec = switch (urlRoute.httpMethod()) {
                case GET -> client.get();
                case POST -> client.post();
                case PUT -> client.put();
                case DELETE -> client.delete();
                default -> throw new RuntimeException("Unsupported HTTP method");
            };

            var parameterAnnotations = method.getParameterAnnotations();
            var uriVariables = new ArrayList<>();
            var requestParams = new ArrayList<RequestParam>();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (var parameterAnnotation : parameterAnnotations[i]) {
                    if (parameterAnnotation.annotationType() == RequestParam.class) {
                        var requestParam = (RequestParam) parameterAnnotation;
                        requestParams.add(requestParam);
                    } else if (parameterAnnotation.annotationType() == PathVariable.class) {
                        uriVariables.add(args[i]);
                    }
                }
            }

            var bodyUriSpec = reqUriSpec.uri(builder -> {
                builder.replacePath(builder.build().getPath() + urlRoute.pathPattern().pattern());
                IntStream.of(0, requestParams.size())
                        .filter(i -> i > 0)
                        .map(i -> i - 1)
                        .forEachOrdered(i -> {
                    var requestParam = requestParams.get(i);
                    builder.queryParam(requestParam.value(), args[i]);
                });
                return builder.build(uriVariables.toArray());
            });

            request.getHeaders().getAll().forEach(bodyUriSpec::header);
            if (bodyUriSpec instanceof RestClient.RequestBodyUriSpec) {
//                if (requestMapping.consumes().length > 0) {
//                    ((WebClient.RequestBodyUriSpec)bodyUriSpec).contentType(MediaType.parseMediaType(requestMapping.consumes()[0]));
//                }
            }

//            if (requestMapping.produces().length > 0) {
//                bodyUriSpec.accept(MediaType.parseMediaType(requestMapping.produces()[0]));
//            }
            var argList = new ArrayList<>(Arrays.asList(args == null? new Object[0] : args));

            for (Annotation[] parameterAnnotation1 : parameterAnnotations) {
                var parameterAnnotationList = new ArrayList<>();

                for (var parameterAnnotation : parameterAnnotation1) {
                    parameterAnnotationList.add(parameterAnnotation.annotationType());

                    if (parameterAnnotationList.contains(PathVariable.class)) {
                        argList.removeFirst();
                        break;
                    }
                }
            }

            if (!argList.isEmpty()) {
                if (bodyUriSpec instanceof RestClient.RequestBodyUriSpec) {
                    byte[] result = EntityTransform.serialize(argList.get(0));
                    ((RestClient.RequestBodyUriSpec)bodyUriSpec).body(result);
                }
            }
            var response = bodyUriSpec
                    .retrieve()
                    .toEntity(byte[].class)
                    ;

            var responseContent = response.getBody();

            if (responseContent.length == 1 && responseContent[0] == 0) {
                return proxy;
            }

            Class<?> targetClass = method.getReturnType();
            if (targetClass != String.class) {
                targetClass = JvmClassHelper.classForName(
                        ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0].getTypeName()
                );
            }
            return inflate(responseContent, targetClass);
        } catch (Exception e) {
            logger.logError("Unexpected exception: ", e);
            ExceptionHelper.uncheckedThrow(e);
            return null;
        }
    }

    private Object inflate(byte[] result, Class<?> targetClass) {
        var str = new String(result, Charset.defaultCharset()).trim();
        if (str.startsWith("["))
            return JsonHelper.fromJson(str, targetClass, JsonHelper.TypeToken.of());
        else if (str.startsWith("{"))
            return JsonHelper.fromJson(str, targetClass);
        else
            return str;
    }
}