package com.artarkatesoft.rsocketserver.data;

import lombok.Data;

@Data
public class VideoFileRegion {
    private byte[] content;
    private String name;
    private long startPosition;
    private long contentLength;
    private long rangeLength;
}
