package com.artarkatesoft.rsocketserver.controllers;

import com.artarkatesoft.rsocketserver.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class RSocketController {

    static final String SERVER = "Server";
    static final String RESPONSE = "Response";

    @MessageMapping("request-response")
    Message requestResponse(Message request) {
        log.info("Received request-response request: {}", request);
        return new Message(SERVER, RESPONSE);
    }
}
