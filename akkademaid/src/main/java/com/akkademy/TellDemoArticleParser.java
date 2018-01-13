package com.akkademy;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import com.akkademy.messages.GetRequest;
import com.akkademy.messages.SetRequest;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeoutException;

public class TellDemoArticleParser  extends AbstractActor {
    private final ActorSelection cacheActor;
    private final ActorSelection httpClientActor;
    private final ActorSelection articleParserActor;
    private final Timeout timeout;

    public TellDemoArticleParser(String cacheActorPath, String httpClientActorPath, String articleParseActorPath, Timeout timeout) {
        this.cacheActor = context().actorSelection(cacheActorPath);
        this.httpClientActor = context().actorSelection(httpClientActorPath);
        this.articleParserActor = context().actorSelection(articleParseActorPath);
        this.timeout = timeout;
    }

    @Override
    public PartialFunction receive() {
        return ReceiveBuilder
                .match(ParseArticle.class, msg -> {
                    ActorRef extraActor = buildExtraActor(sender(), msg.url);
                    cacheActor.tell(new GetRequest(msg.url), extraActor);
                    httpClientActor.tell(msg.url, extraActor);

                    context().system().scheduler().scheduleOnce(timeout.duration(),
                            extraActor, "timeout", context().system().dispatcher(), ActorRef.noSender());
                }).build();
    }

    private ActorRef buildExtraActor(ActorRef senderRef, String uri) {
        class MyActor extends AbstractActor {
            public MyActor() {
                receive(
                        ReceiveBuilder
                        .matchEquals(String.class, x -> x.equals("timeout"), x -> {
                            senderRef.tell(new Status.Failure(new TimeoutException("timeout!")), self());
                            context().stop(self());
                        })
                        .match(HttpResponse.class, httpResponse -> {
                            articleParserActor.tell(new ParseHtmlArticle(uri, httpResponse.body), self());
                        })
                        .match(String.class, body -> {
                            senderRef.tell(body, self());
                            context().stop(self());
                        })
                        .match(ArticleBody.class, articleBody -> {
                            cacheActor.tell(new SetRequest(articleBody.uri, articleBody.body), self());
                            senderRef.tell(articleBody.body, self());
                            context().stop(self());
                        })
                        .matchAny(t -> {
                            System.out.println("ignoring msg " + t.getClass());
                        })
                        .build()
                );
            }
        }

        return context().actorOf(Props.create(MyActor.class, () -> new MyActor()));
    }
}

