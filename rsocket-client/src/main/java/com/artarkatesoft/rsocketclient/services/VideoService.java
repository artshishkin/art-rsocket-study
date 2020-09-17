package com.artarkatesoft.rsocketclient.services;

import com.artarkatesoft.rsocketclient.data.VideoFile;
import com.artarkatesoft.rsocketclient.data.VideoFileRegion;
import com.artarkatesoft.rsocketclient.data.VideoFileRegionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
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
        if (rangeLength < 0) rangeLength = 1000000L;
        rangeLength = Math.min(rangeLength, 1000000L);
        System.out.println("rangeLength: " + rangeLength);

        Path path = Paths.get(videoLocation + "/" + name);
        UrlResource urlResource = new UrlResource("file:" + videoLocation + "/" + name);
        long contentLength = urlResource.contentLength();
        System.out.println("contentLength: " + contentLength);
        System.out.println("(int) rangeLength: " + ((int) rangeLength));
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) rangeLength);

        FileChannel fileChannel = FileChannel.open(path);
        int readCount = fileChannel.read(byteBuffer, startBytePosition);
        VideoFileRegion videoFileRegion = VideoFileRegion.builder()
                .contentLength(contentLength)
                .rangeLength(readCount)
                .name(name)
                .startPosition(startBytePosition)
                .content(byteBuffer.array())
                .build();
        return Mono.just(videoFileRegion);
    }
//public Mono<VideoFileRegion> getResourceRegion(VideoFileRegionRequest regionRequest) throws IOException, URISyntaxException {
//        String name = regionRequest.getName();
//        long startBytePosition = regionRequest.getStart();
//        long rangeLength = regionRequest.getRangeLength();
//
//        Path path = Paths.get(videoLocation + "/" + name);
//        FileUrlResource fileUrlResource = new FileUrlResource(videoLocation + "/" + name);
//        long contentLength = fileUrlResource.contentLength();
//        fileUrlResource
//        ByteBuffer byteBuffer = ByteBuffer.allocate((int) rangeLength);
//        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
//        int readCount = fileChannel.read(byteBuffer, startBytePosition);
//
//        VideoFile videoFile = new VideoFile();
//        videoFile.setContent(bytes);
//        videoFile.setName(name);
//        return Mono.just(videoFile);
//    }

}
