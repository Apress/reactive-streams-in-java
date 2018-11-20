package com.github.adamldavis.akkahttp;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.model.ws.WebSocket;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.javadsl.settings.ClientConnectionSettings;
import akka.http.javadsl.settings.ServerSettings;
import akka.http.javadsl.settings.WebSocketSettings;
import akka.japi.Function;
import akka.japi.JavaPartialFunction;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketExample {

    public static HttpResponse handleRequest(HttpRequest request) {
        System.out.println("Handling request to " + request.getUri());

        if (request.getUri().path().equals("/greeter")) {
            return WebSocket.handleWebSocketRequestWith(request, greeter());
        } else {
            return HttpResponse.create().withStatus(404);
        }
    }

    public static void main(String[] args) throws Exception {
        var system = ActorSystem.create();
        try {
            var materializer = ActorMaterializer.create(system);

            final Function<HttpRequest, HttpResponse> handler = request -> handleRequest(request);
            final Http http = Http.get(system);
            CompletionStage<ServerBinding> serverBindingFuture =
                    http.bindAndHandleSync(
                            handler, ConnectHttp.toHost("localhost", 8080), materializer);

            // will throw Exception if binding fails or takes more than 1 second:
            serverBindingFuture.toCompletableFuture().get(1, TimeUnit.SECONDS);
            System.out.println("Press ENTER to stop.");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            system.terminate();
        }
    }

    /**
     * A handler that treats incoming messages as a name, and responds with a greeting to that name.
     */
    public static Flow<Message, Message, NotUsed> greeter() {
        return Flow.<Message>create()
            .collect(new JavaPartialFunction<>() {
                @Override
                public Message apply(Message msg, boolean isCheck) {
                    if (isCheck) {
                        if (msg.isText()) return null;
                        else throw noMatch();
                    } else {
                        return handleTextMessage(msg.asTextMessage());
                    }
                }
            });
    }

    public static TextMessage handleTextMessage(TextMessage msg) {
        if (msg.isStrict()) { // optimization that directly creates a simple response
            return TextMessage.create("Hello " + msg.getStrictText());
        } else {
            return TextMessage.create(Source.single("Hello ").concat(msg.getStreamedText()));
        }
    }


    /** Example of a websocket server with custom periodic keep alive heartbeat. */
    public static void websocketServer(ActorSystem system, ActorMaterializer materializer) {

        Flow<HttpRequest, HttpResponse, NotUsed> handler = null; // define your handler
        ServerSettings defaultSettings = ServerSettings.create(system);

        AtomicInteger pingCounter = new AtomicInteger();

        var webSocketSettings = defaultSettings.getWebsocketSettings()
                .withPeriodicKeepAliveData(() ->
                        ByteString.fromString(String.format("debug-%d", pingCounter.incrementAndGet()))
                );

        var serverSettings = defaultSettings.withWebsocketSettings(webSocketSettings);

        Http http = Http.get(system);
        http.bindAndHandle(handler,
                ConnectHttp.toHost("127.0.0.1"),
                serverSettings, // pass the configuration
                system.log(),
                materializer);
    }

    /** Example of a websocket client request with periodic keep alive data. */
    public static void websocketPing(ActorSystem system, ActorMaterializer materializer) {

        Flow<Message, Message, NotUsed> clientFlow = greeter();
        ClientConnectionSettings defaultSettings = ClientConnectionSettings.create(system);

        AtomicInteger pingCounter = new AtomicInteger();

        WebSocketSettings customWebsocketSettings = defaultSettings.getWebsocketSettings()
                .withPeriodicKeepAliveData(() ->
                        ByteString.fromString(String.format("debug-%d", pingCounter.incrementAndGet()))
                );

        ClientConnectionSettings customSettings =
                defaultSettings.withWebsocketSettings(customWebsocketSettings);

        Http http = Http.get(system);
        http.singleWebSocketRequest(
                WebSocketRequest.create("ws://127.0.0.1"),
                clientFlow,
                ConnectionContext.noEncryption(),
                Optional.empty(),
                customSettings,
                system.log(),
                materializer);
    }
}

