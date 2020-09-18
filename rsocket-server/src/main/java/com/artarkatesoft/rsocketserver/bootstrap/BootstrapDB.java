package com.artarkatesoft.rsocketserver.bootstrap;

import com.artarkatesoft.rsocketserver.data.Video;
import com.artarkatesoft.rsocketserver.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class BootstrapDB implements CommandLineRunner {

    private final VideoRepository videoRepository;

    @Override
    public void run(String... args) throws Exception {
        videoRepository.count().filter(count -> count > 0)
                .switchIfEmpty(initData())
                .subscribe();
    }

    private Mono<Long> initData() {
        Stream<Video> videoStream = IntStream.rangeClosed(1, 3)
                .mapToObj(this::createFakeVideo);
        Flux<Video> videoFlux = videoRepository.saveAll(Flux.fromStream(videoStream));
        return videoFlux.then(Mono.just(3L));
    }

    private Video createFakeVideo(int index) {
        return Video.builder()
                .cameraName("camName" + index)
                .fileName("fileName" + index)
                .date(LocalDateTime.now())
                .filePath("filePath" + index)
                .videoType("type" + index)
                .size(1000L + index)
                .hasSnapshot(true)
                .build();
    }
}
