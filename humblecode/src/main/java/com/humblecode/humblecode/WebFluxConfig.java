package com.humblecode.humblecode;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@EnableWebFlux
public class WebFluxConfig extends WebFluxConfigurationSupport {

    @Override
    public WebExceptionHandler responseStatusExceptionHandler() {
        return (exchange, ex) -> Mono.create(callback -> {
                    exchange.getResponse().setStatusCode(HttpStatus.I_AM_A_TEAPOT);
                    System.err.println(ex.getMessage());
                    callback.success(null);
                });
    }
}
