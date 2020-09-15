package com.artarkatesoft.rsocketserver.controllers;

import com.artarkatesoft.rsocketserver.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class RSocketController {

    private final List<RSocketRequester> CLIENTS = new ArrayList<>();

    static final String SERVER = "Server";
    static final String RESPONSE = "Response";
    static final String STREAM = "Stream";
    private static final String CHANNEL = "Channel";

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

    @MessageMapping("channel")
    public Flux<Message> channel(final Flux<Duration> settings) {
        return settings
                .doOnNext(setting -> log.info("Frequency duration is {} seconds", setting.getSeconds()))
                .switchMap(setting -> Flux.interval(setting).map(index -> new Message(SERVER, CHANNEL, index)));
    }

    @ConnectMapping("shell-client")
    void connectShellClientAndAskForTelemetry(RSocketRequester requester, @Payload String client) {
        // The code for the method will go HERE
        requester.rsocket()
                .onClose() // (1)
                .doFirst(() -> {
                    log.info("Client: {} CONNECTED.", client);
                    CLIENTS.add(requester); // (2)
                })
                .doOnError(error -> {
                    log.warn("Channel to client {} CLOSED", client); // (3)
                })
                .doFinally(consumer -> {
                    CLIENTS.remove(requester);
                    log.info("Client {} DISCONNECTED", client); // (4)
                })
                .subscribe();

        requester.route("client-status")
                .data("OPEN")
                .retrieveFlux(String.class)
                .doOnNext(s -> log.info("Client: {} Free Memory: {}.",client,s))
                .subscribe();
    }

}
