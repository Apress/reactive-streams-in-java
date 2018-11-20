package com.github.adamldavis.akkahttp;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.settings.ServerSettings;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public class WebApp extends AllDirectives {

    final ActorSystem system;
    final ChatServer chatServer;

    public WebApp(ActorSystem system) {
        this.system = system;
        this.chatServer = new ChatServer(system);
    }

    public static void main(String[] args) {
        // boot up server using the route as defined below
        var system = ActorSystem.create("routes");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are defined.
        var app = new WebApp(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.joinedRoutes()
                .flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 5010), getSettings(system),
                system.log(), materializer);

        System.out.println("Server online at http://localhost:5010/\nUse Ctrl+C to stop...");

        // add shutdown Hook to terminate system:
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                    .thenAccept(unbound -> system.terminate()); // and shutdown when done
        }));
    }

    private static ServerSettings getSettings(ActorSystem system) {
        ServerSettings defaultSettings = ServerSettings.create(system);
        var webSocketSettings = defaultSettings.getWebsocketSettings();
        System.out.println("mode=" + webSocketSettings.periodicKeepAliveMode());
        System.out.println("max-idle=" + webSocketSettings.periodicKeepAliveMaxIdle());

        return defaultSettings.withWebsocketSettings(webSocketSettings);
    }

    private Route createHelloRoute() {
        final Source<String,NotUsed> file = Source.single("/akkahttp/index.html");
        return route(
                path("hello", () ->
                    get(() ->
                        complete(HttpEntities.create(ContentTypes.TEXT_HTML_UTF8,
                            file.map(f -> WebApp.class.getResourceAsStream(f))
                                .map(stream -> stream.readAllBytes())
                                .map(bytes -> ByteString.fromArray(bytes))))
                    )));
    }

    /**
     * Creates a stream of random numbers for request on path /random.
     */
    private Route createRandomRoute() {
        final Random rnd = new Random();
        // streams are re-usable so we can define it here
        // and use it for every request
        Source<Integer, NotUsed> numbers =
                Source.fromIterator(() -> Stream.generate(rnd::nextInt).iterator());

        return route(
                path("random", () ->
                    get(() ->
                        complete(HttpEntities.create(ContentTypes.TEXT_PLAIN_UTF8,
                                // transform each number to a chunk of bytes
                                numbers.map(x -> ByteString.fromString(x + "\n"))))
                    )));
    }

    public Route createWebsocketRoute() {
        return path("chatws", () ->
                handleWebSocketMessages(chatServer.flow())
        );
        // was handleWebSocketMessages(WebSocketExample.greeter())
    }

    private Route joinedRoutes() {
        return route(createHelloRoute(), createRandomRoute(), createWebsocketRoute());
    }

    // using the HTTP client API
    private CompletionStage<HttpResponse> httpRequest(String url, ActorSystem system, ActorMaterializer materializer) {
        return Http.get(system)
                .singleRequest(HttpRequest.create(url), materializer);
    }

}
