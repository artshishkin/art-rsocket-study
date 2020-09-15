package com.artarkatesoft.rsocketserver.controllers;

import com.artarkatesoft.rsocketserver.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Controller
public class RSocketController {

    static final String SERVER = "Server";
    static final String RESPONSE = "Response";
    static final String STREAM = "Stream";

    @MessageMapping("request-response")
    Message requestResponse(Message request) {
        log.info("Received request-response request: {}", request);
        return new Message(SERVER, RESPONSE);
    }

    @MessageMapping("fire-and-forget")
    public void fireAndForget(Message message) {
        log.info("Received fire and forget message: {}", message);
    }

    @MessageMapping("stream")
    public Flux<Message> stream(Message request) {
        log.info("Received stream request: {}", request);
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> new Message(SERVER, STREAM, i))
                .log();
    }
}
