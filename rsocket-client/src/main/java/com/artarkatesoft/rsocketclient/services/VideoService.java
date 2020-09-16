package com.artarkatesoft.rsocketclient.services;

import com.artarkatesoft.rsocketclient.data.VideoFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class VideoService {

    @Value("${videoLocation}")
    private String videoLocation;

    public Mono<VideoFile> getResourceByName(String name) throws IOException, URISyntaxException {
        Path path = Paths.get(videoLocation + "/" + name);
        byte[] bytes = Files.readAllBytes(path);
        VideoFile videoFile = new VideoFile();
        videoFile.setContent(bytes);
        videoFile.setName(name);
        return Mono.just(videoFile);
    }

}
