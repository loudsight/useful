package com.loudsight.useful.service.dispatcher;

import com.loudsight.helper.ClassHelper;
import com.loudsight.meta.MetaRepository;
import com.loudsight.meta.entity.SimpleEntity;
import com.loudsight.meta.entity.SelfReferencingEntity;
import com.loudsight.meta.serialization.transform.JvmTransforms;
import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.useful.service.Listener;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.loudsight.useful.service.dispatcher.Topic.WILDCARD_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class DispatcherTest {
    private static final LoggingHelper logger = LoggingHelper.wrap(DispatcherTest.class);
    private static final Address SERVER_ADDRESS = new Address("Test.to", "Q");
    private static final Address CLIENT_ADDRESS = new Address("Test.from", "Q");
    private static final AtomicInteger id = new AtomicInteger();
    public static final Subject ANONYMOUS = Subject.getAnonymous();
    private static volatile boolean isRunning = true;

    static {
        JvmTransforms.init(); // FIXME

//        new Thread(() -> {
//            Thread.currentThread().setDaemon(true);
//
//            while (isRunning) {
//                try {
//                    ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
//                    long[] ids = tmx.findDeadlockedThreads();
//                    if (ids != null) {
//                        ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
//                        logger.logInfo("The following threads are deadlocked:");
//                        for (ThreadInfo ti : infos) {
//                            logger.logInfo(String.valueOf(ti));
//                        }
//                    }
//                    Thread.sleep(100);
//                } catch (Exception e) {
//                    logger.logError("Unexpected error", e);
//                }
//            }
//        }).start();
    }

    protected abstract MetaRepository getMetaRepository();

    protected abstract Dispatcher getClientDispatcher();

    protected <P, Q, A> void setupAddresses(Topic<P, Q, A> serverAddress, Address clientAddress) {  };

    protected Dispatcher getServerDispatcher() {
        return getClientDispatcher();
}

    protected void doBeforeEach() { };

    protected void doAfterEach() { };

    @BeforeEach
    public void beforeEach() {
        doBeforeEach();
    }

    @BeforeAll
    static void beforeAll() {
        isRunning = false;
    }

    @AfterAll
    static void afterAll() {
        isRunning = false;
    }

    @Test
    public void  testAsyncRequest() {

        var received = new Listener<SimpleEntity>();
        var replied = new Listener<SimpleEntity>();

        var sent = new SimpleEntity();
        sent.setId(123);
        Dispatcher clientDispatcher = getClientDispatcher();

        Function<SimpleEntity, SimpleEntity> asyncHandler =
                (payload) -> {
                    received.accept(payload);

                    var reply = new SimpleEntity();
                    reply.setName("b");
                    reply.setId(payload.getId() + 123);

                    return reply;
                };

        Dispatcher serverDispatcher = getServerDispatcher();
        var serverAddress = new Topic<>(Object.class,
                SimpleEntity.class,
                SimpleEntity.class);
        setupAddresses(serverAddress, CLIENT_ADDRESS);
        var subscription = serverDispatcher.subscribe(serverAddress, asyncHandler);

        clientDispatcher.publishAsync(serverAddress, sent, replied::accept);

        var result = received.getResult(60, TimeUnit.SECONDS);
        assertEquals(sent.getId(), result.getId());
        assertEquals(sent.getName(), result.getName());

        SimpleEntity reply = replied.getResult(60, TimeUnit.SECONDS);
        assertEquals(sent.getId() + 123, reply.getId());
        assertEquals("b", reply.getName());
        subscription.unsubscribe();
    }

//    @Disabled
//    @Test
//    public void  f()  {
//        int n = 500
//
//        int handler: suspend (Message<String?>) -> Unit = { message ->
//            message.body()!!.toInt()
//        }
//        dispatcher.subscribe(() -> "com.loudsight.utilities.DispatcherTest@hello", handler)
//
//        int actual = (0..n).map {
//            GlobalScope.async {
//                suspendCoroutine<Int> { cont ->
//                    dispatcher.requestAsync(() -> "com.loudsight.utilities.DispatcherTest@hello", "$it",
//                            object : Dispatcher.AsyncHandler<Int?> {
//                                override public void  onSuccess(result: Int?) {
//                                    cont.resume(result!! - it)
//                                }
//
//                                override public void  onFail(e: Exception) {
//                                    fail(e.message)
//                                }
//                            })
//                }
//            }
//        }.awaitAll()
//
//        int expected = (0..n).map {0}
//
//        kotlin.test.assertEquals(expected, actual, "expected = $expected}\nactual = $actual")
//    }

//    @Test
//    @Throws(Exception::class)
//    public void  testSyncRequest()  {
//
//        int received: AtomicReference<Q> = AtomicReference()
//
//        int sent = Q("a")
//        sent.id = 123
//
//        int handler : (Message<Q?>) -> Unit= { message ->
//            received.set(message.body())
//
//            int response = C()
//            response.name = "b"
//            response.id = message.body()!!.id + 123
//            message.reply(response)
//        }
//
//        int subscription = dispatcher.subscribe(() -> "Q::class", handler)
//        int replied = dispatcher.request<C>(() -> "Q::class", sent)
//
//        assertEquals(sent.id, received.get().id)
//        assertEquals(sent.name, received.get().name)
//
//        assertEquals(sent.id + 123, replied!!.id)
//        assertEquals("b", replied.name)
//
//        subscription.unsubscribe()
//    }

    interface Function3<A, B, C, D> {
        D invoke(A a, B b, C c);
    }

    private void testSubscribe(Function3<Dispatcher, String, Consumer<SimpleEntity>, Void> subscribeStrategy) throws Exception {

        CountDownLatch replyLatch = new CountDownLatch(1);
        Dispatcher dispatcher = getClientDispatcher();

        var sent = new SimpleEntity();
        sent.setId(123);

        AtomicReference<SimpleEntity> received = new AtomicReference<>();

        var reply = new SelfReferencingEntity(new SelfReferencingEntity(null));
        reply.setName("b");
        reply.setId(sent.getId() + 123);
        subscribeStrategy.invoke(dispatcher, "Q::class", entity -> {
            received.set(entity);
            replyLatch.countDown();
        });

        dispatcher.publish(new Topic<>(Object.class, SimpleEntity.class, Void.class), sent);

        replyLatch.await(15, TimeUnit.SECONDS);

        assertEquals(sent.getId(), received.get().getId());

        assertEquals(sent.getName(), received.get().getName());
    }

    @Test
    public void testPublishSubscribe() throws Exception {
        Dispatcher clientDispatcher = getClientDispatcher();
        Dispatcher serverDispatcher = getServerDispatcher();
        testPublishSubscribe(getMetaRepository(), clientDispatcher, serverDispatcher);
    }

    void testPublishSubscribe(MetaRepository clientMetaRepository,
                              Dispatcher clientDispatcher,
                              Dispatcher serverDispatcher) {
        var aMeta = clientMetaRepository.getMeta(SelfReferencingEntity.class);

        var sent = new SelfReferencingEntity(new SelfReferencingEntity(null));
        sent.setName("a");
        sent.setId(123);


        var reply = new SelfReferencingEntity(new SelfReferencingEntity(null));
        reply.setName("b");
        reply.setId(sent.getId() + 123);
        var received = new Listener<SelfReferencingEntity>();

        Function<SimpleEntity, SimpleEntity> handler =
                (payload) -> {
            received.accept(ClassHelper.uncheckedCast(payload));
            return null;
        };
        var serverAddress = new Topic<>(
                Object.class,
                SimpleEntity.class,
                SimpleEntity.class,
                Map.of("scope", id.incrementAndGet()));
        setupAddresses(serverAddress, CLIENT_ADDRESS);
        var subscription = serverDispatcher.subscribe(serverAddress, handler);
        delay(500);
        clientDispatcher.publish(serverAddress, sent);

        var result = received.getResult(60, TimeUnit.SECONDS);

        assertEquals(sent.getId(), result.getId());
        subscription.unsubscribe();
    }

    @Test
    public void testWildCardSubscription() {
        Dispatcher dispatcher = getClientDispatcher();
        var received = new Listener<Integer>();

        Function<Object, Object> handler =
                (payload) -> {
                    received.accept((Integer)payload);
                    return null;
                };

        dispatcher.subscribe(WILDCARD_ADDRESS, handler);

        for (int i = 0; i < 100; i++) {
            var address = new Topic<>(DispatcherTest.class, Integer.class, Integer.class, Map.of("service", i, "topic", i));
            dispatcher.publish(address, i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(i, received.getResult());
        }
    }



    @Test
    public void eventBusConcurrentRequests() {
//        EventBus eventBus = Vertx.vertx().eventBus();
//
//        eventBus.<String>consumer("xxxx$ff@uu", it -> {
//            executor.submit(() -> {
//                it.reply(Integer.valueOf(it.body()));
//            });
//        });
//        int n = 600;
//
//        List<Integer> results = IntStream.range(0, n)
//                        .boxed()
//                        .map(it -> {
//            CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
//
//            executor.submit(() -> {
//                final Handler<AsyncResult<Message<Integer>>> asyncResultHandler =
//                        event -> completableFuture.complete(event.result().body());
//                eventBus.request("xxxx$ff@uu", "" + it, asyncResultHandler);
//            });
//            return completableFuture;
//        }).map(it -> {
//                    try {
//                        return it.get();
//                    } catch (InterruptedException | ExecutionException e) {
//                        return null;
//                    }
//                }).collect(Collectors.toList());
//
//        List<Integer> expected = IntStream.range(0, n).boxed().collect(Collectors.toList());
//        assertEquals(expected, results);
    }

    public static void delay(int period) {
        try {
            Thread.sleep(period);
        } catch (InterruptedException e) {
            logger.logError("Unexpected error", e);
        }
    }
}

