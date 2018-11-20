package com.humblecode.humblecode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@EnableAutoConfiguration
@SpringBootApplication
public class HumblecodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HumblecodeApplication.class, args);
    }

    @Bean
    public Flux<String> exampleBean() {
        return Flux.just("example");
    }
}
