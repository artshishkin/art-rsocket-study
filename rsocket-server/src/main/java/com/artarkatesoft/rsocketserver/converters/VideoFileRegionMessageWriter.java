package com.artarkatesoft.rsocketserver.converters;

import com.artarkatesoft.rsocketserver.data.VideoFileRegion;
import lombok.val;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.ByteBufferEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.*;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.*;

public class VideoFileRegionMessageWriter implements HttpMessageWriter<VideoFileRegion> {

    private static final ResolvableType REGION_TYPE = ResolvableType.forClass(VideoFileRegion.class);

    private ByteBufferEncoder byteBufferEncoder = new ByteBufferEncoder();

    private DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

    private List<MediaType> mediaTypes = MediaType.asMediaTypes(Arrays.asList(MimeType.valueOf("video/mp4")));

    @Override
    public List<MediaType> getWritableMediaTypes() {
        return mediaTypes;
    }

    @Override
    public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
        // TODO: 17.09.2020 Fix this
        return true;
    }

    @Override
    public Mono<Void> write(Publisher<? extends VideoFileRegion> inputStream, ResolvableType elementType, MediaType mediaType, ReactiveHttpOutputMessage message, Map<String, Object> hints) {
        return null;
    }

    @Override
    public Mono<Void> write(Publisher<? extends VideoFileRegion> inputStream,
                            ResolvableType actualType,
                            ResolvableType elementType,
                            MediaType mediaType,
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            Map<String, Object> hints) {

        HttpHeaders headers = response.getHeaders();
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

        return Mono.from(inputStream).flatMap(
                videoFileRegion -> {
                    response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
                    MediaType resourceMediaType = getVideoFileRegionMediaType(mediaType, videoFileRegion);
                    headers.setContentType(resourceMediaType);

                    long contentLength = videoFileRegion.getContentLength();

                    val start = videoFileRegion.getStartPosition();
                    val end = Math.min(start + videoFileRegion.getRangeLength() - 1, contentLength - 1);
                    headers.add("Content-Range", "bytes " + start + '-' + end + '/' + contentLength);
                    headers.setContentLength(end - start + 1);

                    return zeroCopy(videoFileRegion, response)
                            .orElseGet(() -> {
                                ByteBuffer byteBuffer = ByteBuffer.wrap(videoFileRegion.getContent());
                                val input = Mono.just(byteBuffer);
                                val body = this.byteBufferEncoder.encode(input, response.bufferFactory(), REGION_TYPE, resourceMediaType, new HashMap<>());
                                response.writeWith(body);
                                return Mono.empty();
                            });
                }
        );
    }

    private MediaType getVideoFileRegionMediaType(MediaType mediaType, VideoFileRegion videoFileRegion) {
        return (mediaType != null && mediaType.isConcrete() && mediaType != MediaType.APPLICATION_OCTET_STREAM) ?
                mediaType :
                MediaTypeFactory.getMediaType(videoFileRegion.getName())
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    private Optional<Mono<Void>> zeroCopy(VideoFileRegion videoFileRegion,
                                          ReactiveHttpOutputMessage message) {
        if (message instanceof ZeroCopyHttpOutputMessage) {
            DataBuffer dataBuffer = dataBufferFactory.wrap(videoFileRegion.getContent());
            return Optional.of(((ZeroCopyHttpOutputMessage) message).writeWith(Mono.just(dataBuffer)));
        }
        return Optional.empty();
    }
}
