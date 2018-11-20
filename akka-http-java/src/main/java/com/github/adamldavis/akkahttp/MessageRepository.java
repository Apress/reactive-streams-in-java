package com.github.adamldavis.akkahttp;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MessageRepository {

    public CompletionStage<ChatMessage> save(ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500); // imitate long running operation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("saving message: " + message);
            return message;
        });
    }

}
