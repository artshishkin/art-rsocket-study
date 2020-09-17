package com.artarkatesoft.rsocketclient.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoFileRegion {
    private byte[] content;
    private String name;
    private long startPosition;
    private long contentLength;
    private long rangeLength;
}
