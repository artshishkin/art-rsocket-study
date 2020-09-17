package com.artarkatesoft.rsocketclient;

import com.artarkatesoft.rsocketclient.data.VideoFile;
import com.artarkatesoft.rsocketclient.data.VideoFileRegion;
import com.artarkatesoft.rsocketclient.data.VideoFileRegionRequest;
import com.artarkatesoft.rsocketclient.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ClientHandler {

    private final VideoService videoService;

    @MessageMapping("client-status")
    public Flux<String> statusUpdate(String status) {
        log.info("Connection {}", status);
        return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }

    @MessageMapping("video-full")
    public Mono<VideoFile> getVideoResource(String name) throws IOException, URISyntaxException {
        return videoService.getResourceByName(name);
    }

    @MessageMapping("video-region")
    public Mono<VideoFileRegion> getVideoFileRange(VideoFileRegionRequest regionRequest) throws IOException, URISyntaxException {
        return videoService.getResourceRegion(regionRequest);
    }
}
