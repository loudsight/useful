package com.loudsight.utilities.web.proxy;

import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.meta.Meta;
import com.loudsight.meta.EntityHelper;
import com.loudsight.meta.entity.EntityMethod;
import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.helper.ClassHelper;
import com.loudsight.useful.helper.ExceptionHelper;
import com.loudsight.utilities.json.JsonHelper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RequestPredicates.POST;
import static org.springframework.web.servlet.function.RequestPredicates.PUT;
import static org.springframework.web.servlet.function.RouterFunctions.route;

public class UrlProxyRoutes <T, I extends T> {

    private static final LoggingHelper logger = LoggingHelper.wrap(MethodHandles.lookup().lookupClass());

    public record UrlRoute(HttpMethod httpMethod, Pattern pathPattern, EntityMethod<?,?> entityMethod, String mediaType) {

        Object handle(ServerRequest request, Object handlingInstance) {
            var result = entityMethod.invoke(ClassHelper.uncheckedCast(handlingInstance));

            if (result instanceof String resultStr && MediaType.TEXT_PLAIN_VALUE.equals(mediaType)) {
                return resultStr;
            }

            return JsonHelper.toJson(result);
        }
    }

    private final List<UrlRoute> urlRoutes;

    public UrlProxyRoutes(List<UrlRoute> routes) {
        this.urlRoutes = routes;
    }

    public RouterFunction<ServerResponse> getRoutes(String base, I instance) {
        return urlRoutes.stream()
                .map(urlRoute -> {
                    HttpMethod requestMethod = urlRoute.httpMethod();
                    var pathPattern = urlRoute.pathPattern();
                    var path = pathPattern.pattern();

                    if (!path.startsWith("/")) {
                        throw new RuntimeException("UrlRoute path must start with '/'");
                    }

                    if (base.endsWith("/")) {
                        path = base.substring(0, base.length() - 1) + path;
                    } else {
                        path = base + path;
                    }

                    var requestPredicate = switch (requestMethod) {
                        case GET -> GET(path);
                        case POST -> POST(path);
                        case PUT -> PUT(path);
                        default -> null;
                    };
                    if (requestPredicate == null) return null;

                    return route(requestPredicate, logAndExecute(request -> {
                        Object result = urlRoute.handle(request, instance);

                        return ServerResponse.ok()
                                .contentType(MediaType.parseMediaType(urlRoute.mediaType))
                                .body(result)
//                                .onErrorResume(Exception.class, e -> ServerResponse.badRequest().build())
                                ;
                    }));
                })
                .filter(Objects::nonNull)
                .reduce(RouterFunction::and)
                .orElseThrow();
    }

    public Mono<Object[]> getParams(ServerRequest request, EntityMethod<T, ?> method) {

        return Mono.just(
                method.parameters().stream().map(parameter -> {
                    var pathVariable = parameter.annotations()
                            .stream()
                            .filter(it -> it.getName().equals(PathVariable.class.getName()))
                            .findFirst();

                    if (pathVariable.isPresent()) {
                        var pathVariableName = pathVariable.get().getProperties().get("value").getValue().toString();
                        var pathVariableValue = request.pathVariable(pathVariableName);

                        return EntityHelper.convert(pathVariableValue, parameter.parameterType());
                    }
//            return request.body(BodyExtractors.toDataBuffers())
//                    .map(DataBuffer::asByteBuffer)
//                    .map(it -> map(request.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM), it.array()))
//                    .singleOrEmpty();
////                .map(inflatingMapper);

                    return new Object();
                }).toArray()
        );
    }

    byte[] deflate(List<MediaType> accept, Object payload) {
        if (payload instanceof byte[]) {
            return (byte[]) payload;
        }
        if (payload instanceof Exception e) {
            ExceptionHelper.uncheckedThrow(e);
        }

        if (!accept.isEmpty()) {
            if (accept.stream().anyMatch(it -> it.equals(ALL)) ||
                    accept.stream().anyMatch(it -> it.equals(APPLICATION_JSON))) {
                if (payload instanceof Collection<?> collection) {
                    return JsonHelper.toJson(collection).getBytes(StandardCharsets.UTF_8);
                } else {
                    return JsonHelper.toJson(payload).getBytes(StandardCharsets.UTF_8);
                }
            }
            if (payload instanceof String str) {
                return str.getBytes(StandardCharsets.UTF_8);
            }
        }
        throw new RuntimeException("fix it");
    }

    private <T extends ServerResponse> HandlerFunction<T> logAndExecute(Function<ServerRequest, T> handler) {
        return (request) -> {
            logger.logInfo("Request: {} {}", request.method(), request.path());
            request.headers().asHttpHeaders()
                    .forEach(
                            (name, values) -> values.forEach(value -> logger.logInfo("{}={}", name, value))
                    );

            return handler.apply(request);
        };
    }

    public static class Builder<T, I extends T> {
        public List<UrlRoute> build() {
            return routes;
        }

        @FunctionalInterface
        public interface A<T, I extends T, K> {
            Invocation<T, I, K> consumes(Function<T, K> methodCaller);
        }

        static class InterceptedInvocation {
            EntityMethod<?, ?> method;
            Object[] parameters;

            InterceptedInvocation(EntityMethod<?, ?> method, Object... parameters) {
                this.method = method;
                this.parameters = (parameters == null || parameters.length == 0)?
                        new Object[0] : Stream.of(parameters).toList().toArray();
            }
        }

        public static abstract class Invocation<T, I extends T, K> {
            InterceptedInvocation interceptedInvocation;

            Invocation(InterceptedInvocation interceptedInvocation) {
                this.interceptedInvocation = interceptedInvocation;
            }

            public abstract Builder<T, I> produces(String mediaType);
        }

        List<UrlRoute> routes = new ArrayList<>();
        private final Class<T> aClass;
        private final Meta<T> meta;
//        private final Map<Integer, List<Function<?, ?>>> paramExtractors = new HashMap<>();
//        private final AtomicInteger routeId = new AtomicInteger();

        public Builder(Class<T> aClass, MetaRepository metaRepository) {
            this.aClass = aClass;
            this.meta = metaRepository.getMeta(aClass);
        }

        public String strFromPath() {
//            var x = paramExtractors.putIfAbsent(routeId.get(), new ArrayList<>());
            // x.add();
            meta.toString(); // get rid of unused error in PMD
            return "";
        }

        public A<T, I, Object> path(HttpMethod httpMethod, String path) {
            final Queue<InterceptedInvocation> interceptedInvocations = new ArrayDeque<>();
            return new A<>() {
                final Object proxiedInstance = Proxy.newProxyInstance(aClass.getClassLoader(),
                        new Class[]{aClass},
                        (a, method, parameters) -> {
                    throw new RuntimeException("Fixme when meta supports methods");
                            // var entityMethod = meta.getMethod(method.getName());
                            // interceptedInvocations.add(new InterceptedInvocation(entityMethod, parameters));

//                            if ("equals".equals(method.getName())) {
//                                return this == parameters[0];
//                            }
//                            return null;
                        }
                );

                @Override
                public Invocation<T, I, Object> consumes(Function<T, Object> methodCaller) {
                    final T invocationInterceptor = ClassHelper.uncheckedCast(proxiedInstance);
                    methodCaller.apply(invocationInterceptor);
                    InterceptedInvocation interceptedInvocation = interceptedInvocations.poll();
                    return new Invocation<>(interceptedInvocation) {

                        @Override
                        public Builder<T, I> produces(String mediaType) {
                            UrlRoute urlRoute = new UrlRoute(
                                    httpMethod, compile(path), interceptedInvocation.method, mediaType);
                            routes.add(urlRoute);

                            return Builder.this;
                        }

                        private Pattern compile(String path) {
                            String newPath = path;
                            if (!newPath.startsWith("/")) {
                                newPath = "/" + newPath;
                            }
                            return Pattern.compile(newPath);
                        }
                    };
                }
            };
        }
    }
}
