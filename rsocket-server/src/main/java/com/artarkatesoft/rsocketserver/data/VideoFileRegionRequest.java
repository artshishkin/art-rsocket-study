package com.artarkatesoft.rsocketserver.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoFileRegionRequest {
    private String name;
    private long startPosition;
    private long rangeLength;
}
