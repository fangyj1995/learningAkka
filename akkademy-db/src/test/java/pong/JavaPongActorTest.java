package pong;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static scala.compat.java8.FutureConverters.*;
import static akka.pattern.Patterns.ask;

public class JavaPongActorTest {
    ActorSystem system = ActorSystem.create();
    ActorRef actorRef =  system.actorOf(Props.create(JavaPongActor.class), "yajing");


    @Test
    public void shouldReplyToPingWithPong() throws Exception {
        Future sFuture = ask(actorRef, "Ping", 1000);
        final CompletionStage<String> cs = toJava(sFuture);
        final CompletableFuture<String> jFuture = (CompletableFuture<String>) cs;

        assert(jFuture.get(1000, TimeUnit.MILLISECONDS).equals("Pong"));
    }

    @Test(expected = ExecutionException.class)
        public void shouldReplyToUnknownMessageWithFailure() throws  Exception {
        Future sFuture = ask(actorRef, "unknown", 1000);
        final CompletionStage<String> cs = toJava(sFuture);
        final CompletableFuture<String> jFuture = (CompletableFuture<String>) cs;
        jFuture.get(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void shouldPrintToConsole() throws Exception {
        askPong("Ping").thenAccept(
                x -> System.out.println("replied with: " + x)
        );
        Thread.sleep(100);
    }

    @Test
    public void shouldTransform() throws Exception {
       // char result = (char) get(
                askPong("Ping")
                .thenApply(x ->//对返回结果进行同步调用
                        x.charAt(0));//);
        //assertEquals('P', result);
    }

    @Test
    public void shouldTransformAsync() throws Exception {
        CompletionStage cs = askPong("Ping")
                .thenCompose(x ->//对返回结果进行异步调用，并且结果只嵌套在一个future中
                        askPong("Ping"));
        //assertEquals(get(cs), "Pong");
    }

    @Test
    public void shouldEffectOnError() throws Exception {
        System.out.println("error");
        /**
         * handle 接受一个 BiFunction 作为参数，该函数会对成功或失败情况进行转换。
         * handle 中的函数在成功情况下会提供结果，在失败情况下则会提供 Throwable，
         * 因此需要检查 Throwable 是否存在（结果和 Throwable 中只有一个不是 null）。
         * 如果 Throwable 存在，就 向日志输出一条语句。
         * 由于我们需要在该函数中返回一个值，而失败情况下又不需要对 返回值做任何操作，因此直接返回 null。
         */
        askPong("cause error").handle(
            (x, t) -> {//Bifunction
                if(t != null) {
                    System.out.println("Error: " + t);
                }
                return null;
            }
        );
    }

    @Test
    public void shouldRecoverOnError() throws Exception {
        //在 Java 中，可以使用 exceptionally将 Throwable 转换为一个可用的值
        CompletionStage<String> cs = askPong("cause error")
                .exceptionally(t -> "default");
        String result = (String) get(cs);
        assertEquals(result, "default");
    }

    @Test
    public void shouldRecoverOnErrorAsync() throws Exception {
        /**
         * 首先，检查 exception 是否为 null。如果为 null， 就返回包含结果的 Future，否则返回重试的 Future。
         * 接着，调用 thenCompose 将 CompletionStage[CompletionStage[String]]扁平化。
         */
        CompletionStage<String> cf = askPong("cause error")
                .handle((pong, ex) -> ex == null
                        ? CompletableFuture.completedFuture(pong)//Returns a new CompletableFuture that is already completed with the given value.
                        : askPong("Ping")
                ).thenCompose(x -> x);
        assertEquals("Pong", get(cf));
    }

    @Test
    public void shouldChainTogetherMultipleOperations() throws Exception {
        CompletionStage<String> cf = askPong("Ping")
                .thenCompose(x ->
                        askPong("Ping" + x))
                .handle((x, t) ->
                        t != null ? "exception": x);//
        assertEquals("exception", get(cf));

        cf = askPong("Ping")
                .thenCompose(x ->
                        askPong("Ping"))
                .handle((x, t) ->
                        t != null ? "exception": x);//
        assertEquals("Pong", get(cf));
    }

    @Test
    public void shouldPrintErrorToConsole() throws Exception {
        askPong("cause error").handle((x, t) -> {
           if(t != null) {
               System.out.println("Error: " + t);
           }
           return null;
        });
        Thread.sleep(100);
    }

    @Test
    public void shouldCombineMultipleFutureResult() throws Exception {
        CompletionStage<String> cf = askPong("Ping")
                .thenCombine(askPong("Ping"), (a, b) -> a+b);
        assertEquals("PongPong", get(cf));
    }

    //helpers
    public Object get(CompletionStage cs) throws Exception {
        return ((CompletableFuture<String>) cs).get(1000, TimeUnit.MILLISECONDS);
    }

    public CompletionStage<String> askPong(String message){
        Future sFuture = ask(actorRef, message, 1000);
        CompletionStage<String> cs = toJava(sFuture);
        return cs;
    }
}
