package com.akkademy.clientactor;

import akka.actor.AbstractFSM;
import akka.actor.ActorSelection;
import com.akkademy.messages.Connected;
import com.akkademy.messages.Request;

import java.util.LinkedList;
import static com.akkademy.clientactor.State.*;

enum State{
    DISCONNECTED,
    CONNECTED,
    CONNECTED_AND_PENDING
}

class EventQueue extends LinkedList<Request> {}
class FlushMsg{}
/**
 * To revert to disconnected, we could send occassional heartbeat pings
 * and revert by restarting the actor (throwing an exception)
 */

public class FSMClientActor extends AbstractFSM<State, EventQueue> {
    private ActorSelection remoteDb;

    public FSMClientActor(String dbPath) {
        System.out.println("constructor start");
        remoteDb = context().actorSelection(dbPath);
        System.out.println("constructor end");
    }

    {
        System.out.println("setup state machine");

        startWith(DISCONNECTED, new EventQueue());

        when(DISCONNECTED,
                //离线状态收到刷新消息
                matchEvent(FlushMsg.class, (msg, eventQueue) -> stay())
                //离线状态收到新请求
                .event(Request.class, (msg, eventQueue) -> {
                    remoteDb.tell(new Connected(), self());
                    eventQueue.add(msg);
                    return stay();
                })
                //离线状态收到已连接的消息
                .event(Connected.class, (msg, eventQueue) -> {
                    if(eventQueue.size() == 0)
                        return goTo(CONNECTED);//转到已连接状态
                    else
                        return goTo((CONNECTED_AND_PENDING));//转到已连接Pending状态
                })
        );

        when(CONNECTED,
                //已连接状态收到刷新消息，保持不变
                matchEvent(FlushMsg.class, (msg, eventQueue) ->
                        stay())
                //已连接状态收到新请求， 转到已连接pending状态
                .event(Request.class, (msg, eventQueue) -> {
                    eventQueue.add(msg);
                    return goTo(CONNECTED_AND_PENDING);
                })
        );

        when(CONNECTED_AND_PENDING,
                //刷新队列中的消息
                matchEvent(FlushMsg.class, (msg, eventQueue) -> {
                    remoteDb.tell(eventQueue, self());
                    //eventQueue = new EventQueue();error in book?
                    eventQueue.clear();
                    return goTo(CONNECTED);//转到已连接状态
                })
                .event(Request.class, (msg, eventQueue) -> {
                    eventQueue.add(msg);//将新消息加入队列
                    return goTo(CONNECTED_AND_PENDING);
                })
        );

        initialize();
        System.out.println("setup state machine complete");
    }
}

