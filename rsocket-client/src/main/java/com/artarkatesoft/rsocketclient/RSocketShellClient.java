package com.artarkatesoft.rsocketclient;

import com.artarkatesoft.rsocketclient.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@Slf4j
@ShellComponent
public class RSocketShellClient {

    private static final String CLIENT = "Client";
    private static final String REQUEST = "Request";
    private static final String FIRE_AND_FORGET = "Fire and forget";

    // Add a global class variable for the RSocketRequester
    private final RSocketRequester rsocketRequester;

    // Use an Autowired constructor to customize the RSocketRequester and store a reference to it in the global variable
    @Autowired
    public RSocketShellClient(RSocketRequester.Builder rsocketRequesterBuilder) {
        this.rsocketRequester = rsocketRequesterBuilder
                .connectTcp("localhost", 7000).block();
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
}
