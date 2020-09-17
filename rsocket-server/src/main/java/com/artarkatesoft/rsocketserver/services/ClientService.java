package com.artarkatesoft.rsocketserver.services;

import com.artarkatesoft.rsocketserver.data.VideoFile;
import com.artarkatesoft.rsocketserver.data.VideoFileRegion;
import com.artarkatesoft.rsocketserver.data.VideoFileRegionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ClientService {
    private final List<RSocketRequester> CLIENTS = new ArrayList<>();

    public void addClient(RSocketRequester client) {
        CLIENTS.add(client);
    }

    public void removeClient(RSocketRequester client) {
        CLIENTS.remove(client);
    }

    public Mono<VideoFile> getVideoFileFromClient(String resourceName) {
        RSocketRequester requester = CLIENTS.get(0);

        return requester.route("video-full")
                .data(resourceName)
                .retrieveMono(VideoFile.class)
                .doOnNext(s -> log.info("Resource name: {} From client: {}.", resourceName, requester));
    }

    public Mono<VideoFileRegion> getResourceRegionFromClient(VideoFileRegionRequest regionRequest) {
        RSocketRequester requester = CLIENTS.get(0);

        log.info("getResourceRegionFromClient with argument {}", regionRequest);

        return requester.route("video-region")
                .data(regionRequest)
                .retrieveMono(VideoFileRegion.class)
                .doOnNext(s -> log.info("Resource name: {} From client: {}.", s.getName(), requester));
    }

}
