package com.github.adamldavis.akkahttp;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.japi.JavaPartialFunction;
import akka.stream.*;
import akka.stream.javadsl.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ChatServer {

    static final int BUFFER_SIZE = 1;
    private final ActorSystem actorSystem;
    private final MessageRepository messageRepository = new MessageRepository();
    private final ActorMaterializer materializer;

    // reactivestreams.org API used for dynamic topologies
    private final Sink<ChatMessage, NotUsed> sink;
    private final Publisher<ChatMessage> publisher;
    // MergeHub and BroadcastHub can be used as well:
    final RunnableGraph<Sink<ChatMessage, NotUsed>> mergeHub;
    private final Sink<ChatMessage, NotUsed> mergeSink;
    final ObjectMapper jsonMapper = new ObjectMapper();

    private final int parallelism;

    public ChatServer(ActorSystem actorSystem) {
        parallelism = Runtime.getRuntime().availableProcessors();
        this.actorSystem = actorSystem;
        materializer = ActorMaterializer.create(actorSystem);
        var asPublisher = Sink.<ChatMessage>asPublisher(AsPublisher.WITH_FANOUT);
        var publisherSinkPair = asPublisher.preMaterialize(materializer);
        publisher = publisherSinkPair.first();
        sink = publisherSinkPair.second();
        mergeHub = MergeHub.of(ChatMessage.class, BUFFER_SIZE).to(sink);
        mergeSink = mergeHub.run(materializer); // must run to get the sink
    }

    public Publisher<ChatMessage> getPublisher() {
        return publisher;
    }

    private Flow<String, ChatMessage, NotUsed> parseContent() {
        return Flow.of(String.class).map(line -> jsonMapper.readValue(line, ChatMessage.class));
    }

    private Sink<ChatMessage, CompletionStage<ChatMessage>> storeChatMessages() {
        return Flow.of(ChatMessage.class)
                .mapAsyncUnordered(parallelism, messageRepository::save)
                .toMat(Sink.last(), Keep.right());
    }


    CompletionStage<ChatMessage> storeMessageFromContent(CompletionStage<String> content) {
        return Source.fromCompletionStage(content)
                .via(parseContent())
                .runWith(storeChatMessages(), materializer)
                .whenComplete((message, ex) -> {
                    if (message != null) {
                        System.out.println("Saved message: " + message);
                    } else {
                        ex.printStackTrace();
                    }
                });
    }

    public Flow<Message, Message, NotUsed> flow() {
        /* JavaPartialFunction: Helper for implementing a *pure* partial function: it will possibly be invoked multiple
        times for a single 'application', because its only abstract method is used for both isDefinedAt()
        and apply(); the former is mapped to isCheck == true and the latter to isCheck == false for those cases
        where this is important to know. */
        Flow<Message, ChatMessage, NotUsed> savingFlow = Flow.<Message>create()
                .buffer(BUFFER_SIZE, OverflowStrategy.backpressure())
                .collect(new JavaPartialFunction<Message, CompletionStage<ChatMessage>>() {
                    @Override
                    public CompletionStage<ChatMessage> apply(Message msg, boolean isCheck) {
                        if (msg.isText()) {
                            if (isCheck) return null;

                            TextMessage textMessage = msg.asTextMessage();
                            if (textMessage.isStrict()) {
                                return storeMessageFromContent(
                                        CompletableFuture.completedFuture(textMessage.getStrictText()));
                            } else {
                                return storeMessageFromContent(textMessage.getStreamedText()
                                        .runFold("", (each, total) -> total + each, materializer));
                            }
                        } else if (isCheck) throw noMatch();
                        return CompletableFuture.completedStage(new ChatMessage(null, null));
                    }
                })
                .mapAsync(parallelism, stage -> stage) // unwraps the CompletionStage
                .filter(m -> m.username != null);

        final Graph<FlowShape<Message, Message>, NotUsed> graph =
                GraphDSL.create(builder -> {
                    final FlowShape<ChatMessage, Message> toMessage =
                            builder.add(Flow.of(ChatMessage.class)
                                    .map(jsonMapper::writeValueAsString).async()
                                    .map(TextMessage::create));

                    Inlet<ChatMessage> sinkInlet = builder.add(mergeSink).in();
                    Outlet<ChatMessage> publisherOutput = builder.add(Source.fromPublisher(publisher)).out();
                    FlowShape<Message, ChatMessage> saveFlow = builder.add(savingFlow);

                    // copy saveFlow to both sinkInlet and Merge that goes to output
                    builder.from(saveFlow.out()).toInlet(sinkInlet);
                    builder.from(publisherOutput).toInlet(toMessage.in()); // from source send to converter

                    return new FlowShape<>(saveFlow.in(), toMessage.out()); // define FlowShape
                });
        return Flow.fromGraph(graph);
    }

    public int getParallelism() {
        return parallelism;
    }
}
