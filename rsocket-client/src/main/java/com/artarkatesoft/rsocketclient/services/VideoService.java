package com.artarkatesoft.rsocketclient.services;

import com.artarkatesoft.rsocketclient.data.VideoFile;
import com.artarkatesoft.rsocketclient.data.VideoFileRegion;
import com.artarkatesoft.rsocketclient.data.VideoFileRegionRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class VideoService {

    public static final long CHUNK_SIZE = 1000000L;
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

    public Mono<VideoFileRegion> getResourceRegion(VideoFileRegionRequest regionRequest) throws IOException {
        String name = regionRequest.getName();
        long startBytePosition = regionRequest.getStartPosition();
        long rangeLength = regionRequest.getRangeLength();
        if (rangeLength < 0) rangeLength = CHUNK_SIZE;
        rangeLength = Long.min(rangeLength, CHUNK_SIZE);
        Path path = Paths.get(videoLocation + "/" + name);
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) rangeLength);
        FileChannel fileChannel = FileChannel.open(path);
        Mono<FileChannel> channelMono = Mono.just(fileChannel);
        Mono<Long> contentLengthMono = channelMono.map(this::fileSize);
        Mono<Integer> readCountMono = channelMono.map(channel -> getReadCount(startBytePosition, byteBuffer, channel));
        return contentLengthMono.zipWith(readCountMono).map(tuple2 -> {
            Long fileSize = tuple2.getT1();
            Integer readCount = tuple2.getT2();
            return VideoFileRegion.builder()
                    .contentLength(fileSize)
                    .rangeLength(readCount)
                    .name(name)
                    .startPosition(startBytePosition)
                    .content(byteBuffer.array())
                    .build();
        });
    }

    @SneakyThrows
    private int getReadCount(long startBytePosition, ByteBuffer byteBuffer, FileChannel fileChannel) {
        return fileChannel.read(byteBuffer, startBytePosition);
    }

    @SneakyThrows
    private long fileSize(FileChannel channel) {
        return channel.size();
    }
}
