package com.artarkatesoft.rsocketserver.repositories;

import com.artarkatesoft.rsocketserver.data.Video;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface VideoRepository extends ReactiveMongoRepository<Video, String> {
}
