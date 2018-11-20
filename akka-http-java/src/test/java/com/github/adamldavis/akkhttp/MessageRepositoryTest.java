package com.github.adamldavis.akkhttp;

import com.github.adamldavis.akkahttp.ChatMessage;
import com.github.adamldavis.akkahttp.MessageRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageRepositoryTest {

    MessageRepository repository;

    @Before
    public void setup() {
        repository = new MessageRepository();
    }
    @After
    public void tearDown() {
        repository = null;
    }

    @Test(timeout = 2000)
    public void save_should_take_a_while() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();

        repository.save(new ChatMessage("foo", "testing 123")).toCompletableFuture().get();

        long took = System.currentTimeMillis() - start;

        assertThat(took).isGreaterThan(498);
    }
}
