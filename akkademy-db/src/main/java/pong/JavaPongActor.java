package pong;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;

public class JavaPongActor  extends AbstractActor {
    public PartialFunction receive() {
        return ReceiveBuilder
                .matchEquals("Ping", s -> sender().tell("Pong", ActorRef.noSender()))
                .matchAny(x -> sender().tell(new Status.Failure(new Exception("unknown message")), self()))
                .build();
    }
    public static Props props(String response) {  return Props.create(JavaPongActor.class, response); }
}
