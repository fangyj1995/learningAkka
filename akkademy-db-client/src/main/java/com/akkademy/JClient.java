package com.akkademy;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.akkademy.messages.GetRequest;
import com.akkademy.messages.SetRequest;

import java.util.concurrent.CompletionStage;
import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.*;

public class JClient {
    private final ActorSystem system = ActorSystem.create("LocalSystem");
    private final ActorSelection remoteDb;

    //得到指向远程 Actor 的引用
    public JClient(String remoteAddress) {
        remoteDb = system.actorSelection("akka.tcp://akkademy@" + remoteAddress + "/user/akkademy-db");
    }

    //分别为 GetRequest 和 SetRequest 两种消息创建方法。
    // 在 Java 代码中，我们将 scala.concurrent.Future 转换成 CompletionStage
    public CompletionStage set(String key, Object value) {
        return toJava(ask(remoteDb, new SetRequest(key, value), 2000));
    }

    public CompletionStage<Object> get(String key) {
        return toJava(ask(remoteDb, new GetRequest(key), 2000));
    }

}
