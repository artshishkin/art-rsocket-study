package com.artarkatesoft.rsocketserver.controllers;

import com.artarkatesoft.rsocketserver.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

@Controller
@RequestMapping("videos")
@RequiredArgsConstructor
public class VideoController {

    private final ClientService clientService;

//    public static final long ChunkSize = 1000000L;

    @GetMapping(value = "{name}/full", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity> getFullVideo(@PathVariable String name) throws IOException {
        return clientService.getVideoFileFromClient(name)
                .map(videoFile -> ResponseEntity.ok()
                        .body(videoFile.getContent()));
    }

//    @GetMapping("{name}")
//    public ResponseEntity<ResourceRegion> getVideo(@PathVariable String name, @RequestHeader HttpHeaders headers) throws IOException {
//        UrlResource video = new UrlResource("file:" + videoLocation + '/' + name);
//        ResourceRegion region = resourceRegion(video, headers);
//        return ResponseEntity
//                .status(HttpStatus.PARTIAL_CONTENT)
//                .contentType(MediaTypeFactory.getMediaType(video)
//                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
//                .body(region);
//    }
//
//
//    private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws IOException {
//        long contentLength = video.contentLength();
//        List<HttpRange> headersRange = headers.getRange();
//        HttpRange range = headersRange.iterator().hasNext() ? headersRange.iterator().next() : null;
//        long start;
//        ResourceRegion resourceRegion;
//        if (range != null) {
//            start = range.getRangeStart(contentLength);
//            long end = range.getRangeEnd(contentLength);
//            long rangeLength = Long.min(1000000L, end - start + 1L);
//            resourceRegion = new ResourceRegion(video, start, rangeLength);
//        } else {
//            start = Long.min(1000000L, contentLength);
//            resourceRegion = new ResourceRegion(video, 0L, start);
//        }
//        return resourceRegion;
//    }

//    @GetMapping
//    public String videoIndex() {
//        return "videos/index";
//    }
}
