package com.artarkatesoft.rsocketserver.services;

import com.artarkatesoft.rsocketserver.data.Video;
import com.artarkatesoft.rsocketserver.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public Flux<Video> getAllVideoFiles(){
        return videoRepository.findAll();
    }
}
