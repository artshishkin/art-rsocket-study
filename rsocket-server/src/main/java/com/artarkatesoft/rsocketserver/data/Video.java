package com.artarkatesoft.rsocketserver.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Video {
    @Id
    private String id;

    private String filePath;
    private String fileName;
    private Long size;
    private LocalDateTime date;
    private String cameraName;
    private String videoType;

    private boolean hasSnapshot;
    private boolean errorSnapShot;
}
