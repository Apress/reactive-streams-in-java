package com.github.adamldavis.akkhttp;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.ws.BinaryMessage;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.stream.ActorMaterializer;
import akka.stream.Graph;
import akka.stream.SinkShape;
import akka.stream.SourceShape;
import akka.stream.javadsl.*;
import akka.stream.javadsl.Flow;
import akka.testkit.javadsl.TestKit;
import akka.util.ByteString;
import com.github.adamldavis.akkahttp.ChatMessage;
import com.github.adamldavis.akkahttp.ChatServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatServerTest {

    ChatServer chatServer;
    ActorSystem actorSystem;
    ActorMaterializer materializer;

    @Before
    public void setup() {
        actorSystem = ActorSystem.create("test-system");
        chatServer = new ChatServer(actorSystem);
        materializer = ActorMaterializer.create(actorSystem);
    }
    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
    }

    @Test
    public void flow_should_exist(){
        assertThat(chatServer.flow()).isNotNull();
    }

    @Test
    public void flow_should_copy_messages() throws ExecutionException, InterruptedException {
        final Collection<Message> list = new ConcurrentLinkedDeque<>();
        Flow<Message, Message, NotUsed> flow = chatServer.flow();

        assertThat(flow).isNotNull();

        List<Message> messages = Arrays.asList(TextMessage.create(jsonMsg(0)));
        Graph<SourceShape<Message>, ?> testSource = Source.from(messages);
        Graph<SinkShape<Message>, CompletionStage<Done>> testSink = Sink.foreach(list::add);

        CompletionStage<Done> results = flow.runWith(testSource, testSink, materializer).second();
        try {
            results.toCompletableFuture().get(2, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            System.out.println("caught expected: " + te.getMessage());
        }

        Iterator<Message> iterator = list.iterator();
        assertThat(list.size()).isEqualTo(1);
        assertThat(iterator.next().asTextMessage().getStrictText())
                .isEqualTo("{\"username\":\"foo\",\"message\":\"bar0\"}");
    }

    @Test
    public void flow_should_copy_many_messages() throws ExecutionException, InterruptedException {
        System.out.println("parallelism=" + chatServer.getParallelism());
        final Collection<Message> list = new ConcurrentLinkedDeque<>();
        Graph<SourceShape<Message>, ?> testSource = Source.range(1, 100).map(i -> TextMessage.create(jsonMsg(i)));
        Graph<SinkShape<Message>, CompletionStage<Done>> testSink = Sink.foreach(list::add);
        Flow<Message, Message, NotUsed> flow = chatServer.flow();
        assertThat(flow).isNotNull();
        CompletionStage<Done> results = flow.runWith(testSource, testSink, materializer).second();
        // this would go one forever, so we have to timeout:
        try {
            results.toCompletableFuture().get(15, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            System.out.println("caught expected: " + te.getMessage());
        }
        assertThat(list.size()).isEqualTo(100);

        list.stream().map(m -> (TextMessage) m)
                .map(TextMessage::getStrictText).forEach(System.out::println);
    }

    @Test
    public void flow_should_publish() throws ExecutionException, InterruptedException {
        final Collection<Message> list = new ConcurrentLinkedDeque<>();
        final var flow = chatServer.flow();
        var subscriber = Source.<ChatMessage>asSubscriber();
        var preMat = subscriber.preMaterialize(materializer);
        Sink<Message, CompletionStage<Done>> testSink = Sink.foreach(list::add);
        Graph<SourceShape<Message>, NotUsed> testSource =
                Source.range(1, 100).map(i -> TextMessage.create(jsonMsg(i)));

        chatServer.getPublisher().subscribe(preMat.first());
        preMat.second().map(m -> "sub:" + m).runWith(Sink.foreach(System.out::println), materializer);

        var result = flow.runWith(testSource, testSink, materializer);

        try {
            result.second().toCompletableFuture().get(15, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            System.out.println("caught expected: " + te.getMessage());
        }
        assertThat(list.size()).isEqualTo(100);
    }


    @Test
    public void flow_should_copy_messages_from_2() throws ExecutionException, InterruptedException {
        System.out.println("parallelism=" + chatServer.getParallelism());
        final Collection<Message> list1 = new ConcurrentLinkedDeque<>(),
                list2 = new ConcurrentLinkedDeque<>();
        Graph<SourceShape<Message>, ?> testSource2 = Source.range(1, 100)
                .map(i -> (i%2==0) ? BinaryMessage.create(ByteString.empty()) : TextMessage.create(jsonMsg(i)));
        // testSource2 just has BinaryMessages so it doesn't close until source1 is done.
        // these are completely ignored by our chat server
        Graph<SourceShape<Message>, ?> testSource1 = Source.range(1, 100)
                .map(i -> (i%2!=0) ? BinaryMessage.create(ByteString.empty()) : TextMessage.create(jsonMsg(i)));
        Graph<SinkShape<Message>, CompletionStage<Done>> testSink1 = Sink.foreach(list1::add);
        Graph<SinkShape<Message>, CompletionStage<Done>> testSink2 = Sink.foreach(list2::add);
        var flow1 = chatServer.flow();
        var flow2 = chatServer.flow();
        assertThat(flow1).isNotNull();
        assertThat(flow2).isNotNull();
        CompletionStage<Done> results1 = flow1.runWith(testSource1, testSink1, materializer).second();
        CompletionStage<Done> results2 = flow2.runWith(testSource2, testSink2, materializer).second();

        try {
            results1.thenAcceptBothAsync(results2, (a,b) -> {}).toCompletableFuture().get(15, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            System.out.println("caught expected: " + te.getMessage());
        }
        System.out.println("size1 = " + list1.size() + " size2 = " + list2.size());
        assertThat(list1.size()).isEqualTo(100);
        assertThat(list2.size()).isEqualTo(100);
    }

    static final String jsonMsg(int i) {
        return "{\"username\": \"foo\", \"message\": \"bar" + i + "\"}";
    }
}
