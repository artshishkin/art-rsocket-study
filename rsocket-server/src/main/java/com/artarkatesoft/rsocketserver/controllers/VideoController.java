package com.artarkatesoft.rsocketserver.controllers;

import com.artarkatesoft.rsocketserver.data.VideoFileRegion;
import com.artarkatesoft.rsocketserver.data.VideoFileRegionRequest;
import com.artarkatesoft.rsocketserver.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("videos")
@RequiredArgsConstructor
public class VideoController {

    public static final long CHUNK_SIZE = 1000000L;
    private final ClientService clientService;

    @GetMapping(value = "{name}/full")
    public Mono<ResponseEntity> getFullVideo(@PathVariable String name) throws IOException {
        return clientService.getVideoFileFromClient(name)
                .map(videoFile -> ResponseEntity.ok()
                        .contentType(MediaTypeFactory.getMediaType(videoFile.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .body(videoFile.getContent()));
    }

    @GetMapping("{name}")
    @ResponseBody
    public Mono<VideoFileRegion> getVideo(@PathVariable String name, @RequestHeader HttpHeaders headers) throws IOException {
        VideoFileRegionRequest regionRequest = buildRegionRequest(name, headers);
        Mono<VideoFileRegion> videoFileRegionMono = clientService.getResourceRegionFromClient(regionRequest);

        return videoFileRegionMono;
    }
//
//    @GetMapping("{name}")
//    public Mono<ResponseEntity<VideoFileRegion>> getVideo(@PathVariable String name, @RequestHeader HttpHeaders headers) throws IOException {
//        VideoFileRegionRequest regionRequest = buildRegionRequest(name, headers);
//        Mono<VideoFileRegion> videoFileRegionMono = clientService.getResourceRegionFromClient(regionRequest);
//
//        return videoFileRegionMono.map(videoFileRegion -> ResponseEntity
//                .status(HttpStatus.PARTIAL_CONTENT)
//                .contentType(MediaTypeFactory.getMediaType(videoFileRegion.getName())
//                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
//                .body(videoFileRegion));
//    }

    private VideoFileRegionRequest buildRegionRequest(String resourceName, HttpHeaders headers) throws IOException {

        List<HttpRange> headersRange = headers.getRange();
        HttpRange range = headersRange.iterator().hasNext() ? headersRange.iterator().next() : null;
        long start;
        long rangeLength;
        if (range != null) {
            long contentLength = headers.getContentLength();
            start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            rangeLength = Long.min(CHUNK_SIZE, end - start + 1L);
        } else {
            start = 0;
            rangeLength = CHUNK_SIZE;
        }

        VideoFileRegionRequest regionRequest = VideoFileRegionRequest.builder()
                .name(resourceName)
                .startPosition(start)
                .rangeLength(rangeLength)
                .build();
        return regionRequest;
    }


    //@GetMapping("{name}")
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
