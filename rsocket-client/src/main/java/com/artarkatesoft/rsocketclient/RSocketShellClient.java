package com.artarkatesoft.rsocketclient;

import com.artarkatesoft.rsocketclient.data.Message;
import io.rsocket.SocketAcceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@ShellComponent
public class RSocketShellClient {


    private static final String CLIENT = "Client";
    private static final String REQUEST = "Request";
    private static final String FIRE_AND_FORGET = "Fire and forget";
    private static final String STREAM = "Stream";

    // Add a global class variable for the RSocketRequester
    private final RSocketRequester rsocketRequester;
    private Disposable disposable;
    private Disposable channelDisposable;


    // Use an Autowired constructor to customize the RSocketRequester and store a reference to it in the global variable
    @Autowired
    public RSocketShellClient(RSocketRequester.Builder rsocketRequesterBuilder, RSocketStrategies strategies, ClientHandler clientHandler,
                              @Value("${app.server.url}") String serverUrl,
                              @Value("${app.server.rsocket.port}") Integer serverPort) {

        String client = UUID.randomUUID().toString();
        log.info("Connecting using client ID {}", client);

        SocketAcceptor responder = RSocketMessageHandler.responder(strategies, clientHandler);

        this.rsocketRequester = rsocketRequesterBuilder
                .setupRoute("shell-client")
                .setupData(client)
                .rsocketStrategies(strategies)
                .rsocketConnector(connector -> connector.acceptor(responder))
//                .connectTcp(serverUrl, serverPort)
                .connectWebSocket(URI.create("ws://localhost:8080/ws"))
//                .connectWebSocket(URI.create("ws://art-rsocket-server.herokuapp.com/ws"))
                .block();

        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(error -> log.warn("Connection CLOSED"))
                .doFinally(consumer -> log.info("I'm DISCONNECTED from server"))
                .subscribe();
    }

    @ShellMethod("Send one request. One response will be printed.")
    public void requestResponse() {
        log.debug("Sending one request. Waiting for one response...");
        Message message = rsocketRequester.route("request-response")
                .data(new Message(CLIENT, REQUEST))
                .retrieveMono(Message.class)
                .block();
        log.debug("Response was: {}", message);
    }

    @ShellMethod("Send one request. No response will be returned.")
    public void fireAndForget() {
        log.debug("Sending fire and forget message. Sending one request. Expect no response (check server log)...");
        rsocketRequester.route("fire-and-forget")
                .data(new Message(CLIENT, FIRE_AND_FORGET))
                .send()
                .block();
    }

    @ShellMethod("Send one request. Stream response will be returned.")
    public void stream() {
        log.debug("Request-Stream. Sending one request. Expect stream response");
        disposable = rsocketRequester.route("stream")
                .data(new Message(CLIENT, STREAM))
                .retrieveFlux(Message.class)
                .subscribe(message -> log.info("Received a message: {}. (Type `s` to stop streaming)", message));
    }

    @ShellMethod(value = "Stop streaming messages from the server.", key = "s")
    public void streamStop() {
        if (disposable != null) {
            disposable.dispose();
        }
        if (channelDisposable != null)
            channelDisposable.dispose();
    }

    @ShellMethod("Send setting stream. Expecting changeable stream response")
    public void channel() {
        log.debug("Channel. Sending request stream of settings. Expect stream response of messages");
        Mono<Duration> mono1 = Mono.just(Duration.ofSeconds(1));
        Mono<Duration> mono2 = Mono.just(Duration.ofSeconds(2)).delayElement(Duration.ofSeconds(4));
        Mono<Duration> mono3 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(10));
        Flux<Duration> settingsFlux = Flux.concat(mono1, mono2, mono3)
                .doOnNext(d -> log.info("Sending setting for {}-second interval.", d.getSeconds()));
        channelDisposable = rsocketRequester.route("channel")
                .data(settingsFlux)
                .retrieveFlux(Message.class)
                .subscribe(message -> log.info("Received a message: {}. (Type `s` to stop streaming)", message));

    }
}

