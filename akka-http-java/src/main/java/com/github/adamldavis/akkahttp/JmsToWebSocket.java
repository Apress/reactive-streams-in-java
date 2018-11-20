package com.github.adamldavis.akkahttp;

import akka.Done;
import akka.actor.ActorSystem;
import akka.japi.Pair;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.javadsl.model.ws.WebSocketUpgradeResponse;

import akka.stream.ActorMaterializer;
import akka.stream.KillSwitch;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;

import scala.concurrent.ExecutionContext;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JmsToWebSocket {
    private final ActorSystem system = ActorSystem.create();
    private final Materializer materializer = ActorMaterializer.create(system);
    private final ExecutionContext ec = system.dispatcher();
/*
    public static void main(String[] args) throws Exception {
        JmsToWebSocket me = new JmsToWebSocket();
        me.run();
    }


    private void enqueue(ConnectionFactory connectionFactory, String... msgs) {
        Sink<String, ?> jmsSink =
                JmsProducer.textSink(JmsProducerSettings.create(connectionFactory).withQueue("test"));

        Source.from(Arrays.asList(msgs)).runWith(jmsSink, materializer);
    }

    private void run() throws Exception {
        ActiveMqBroker activeMqBroker = new ActiveMqBroker();
        activeMqBroker.start();

        WebServer webserver = new WebServer();
        webserver.start("localhost", 8080);

        ConnectionFactory connectionFactory = activeMqBroker.createConnectionFactory();
        enqueue(connectionFactory, "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k");

        final Http http = Http.get(system);

        Source<String, KillSwitch> jmsSource =
                JmsConsumer.textSource(
                        JmsConsumerSettings.create(connectionFactory).withBufferSize(10).withQueue("test"));

        Flow<Message, Message, CompletionStage<WebSocketUpgradeResponse>> webSocketFlow =
                http.webSocketClientFlow(WebSocketRequest.create("ws://localhost:8080/webSocket/ping"));

        int parallelism = 4;
        Pair<Pair<KillSwitch, CompletionStage<WebSocketUpgradeResponse>>, CompletionStage<Done>> pair =
                jmsSource
                        .map(s -> (Message) TextMessage.create(s))
                        .viaMat(webSocketFlow, Keep.both())
                        .mapAsync(parallelism, this::websocketMessageToString)
                        .map(s -> "client received: " + s)
                        .toMat(Sink.foreach(System.out::println), Keep.both())
                        .run(materializer);

        KillSwitch runningSource = pair.first().first();
        CompletionStage<WebSocketUpgradeResponse> wsUpgradeResponse = pair.first().second();
        CompletionStage<Done> streamCompletion = pair.second();

        wsUpgradeResponse
                .thenApply(
                        upgrade -> {
                            if (upgrade.response().status() == StatusCodes.SWITCHING_PROTOCOLS) {
                                return "WebSocket established";
                            } else {
                                throw new RuntimeException("Connection failed: " + upgrade.response().status());
                            }
                        })
                .thenAccept(System.out::println);

        Thread.sleep(2 * 1000);
        runningSource.shutdown();
        streamCompletion.thenAccept(res -> system.terminate());
        system
                .getWhenTerminated()
                .thenAccept(
                        t -> {
                            webserver.stop();
                            activeMqBroker.stop(ec);
                        });
    }

    /**
     * Convert potentially chunked WebSocket Message to a string.
     */
    private CompletionStage<String> websocketMessageToString(Message msg) {
        if (msg.isText()) {
            TextMessage text = msg.asTextMessage();
            if (text.isStrict()) {
                return CompletableFuture.completedFuture(text.getStrictText());
            } else {
                CompletionStage<List<String>> strings =
                        text.getStreamedText().runWith(Sink.seq(), materializer);

                return strings.thenApply(list -> String.join("", list));
            }
        } else {
            return CompletableFuture.completedFuture(msg.toString());
        }
    }

}

