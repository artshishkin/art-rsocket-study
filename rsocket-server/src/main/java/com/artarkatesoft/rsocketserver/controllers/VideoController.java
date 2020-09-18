package com.artarkatesoft.rsocketserver.controllers;

import com.artarkatesoft.rsocketserver.data.VideoFileRegion;
import com.artarkatesoft.rsocketserver.data.VideoFileRegionRequest;
import com.artarkatesoft.rsocketserver.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.chunk-size:60000}")
    public long CHUNK_SIZE;
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
        return clientService.getResourceRegionFromClient(regionRequest);
    }

    private VideoFileRegionRequest buildRegionRequest(String resourceName, HttpHeaders headers)  {

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
        return VideoFileRegionRequest.builder()
                .name(resourceName)
                .startPosition(start)
                .rangeLength(rangeLength)
                .build();
    }
}
