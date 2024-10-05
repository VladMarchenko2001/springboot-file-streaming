package com.stream.example.controller;

import com.stream.example.dto.StreamContentDto;
import com.stream.example.enums.AttachmentType;
import com.stream.example.service.attachment.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.springframework.http.HttpHeaders.*;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
public class StreamController {

    private final AttachmentService attachmentService;

    @GetMapping("/{attachmentType}")
    public ResponseEntity<StreamingResponseBody> stream(@PathVariable AttachmentType attachmentType,
                                                                  @RequestHeader(value = "Range", required = false) String range) {
        return asStreamResponse(attachmentService.getStreamContent(attachmentType, range));
    }

    private ResponseEntity<StreamingResponseBody> asStreamResponse(StreamContentDto streamContent) {
        if (streamContent.partial()) {
            return asPartialStreamResponse(streamContent);
        } else {
            return asFullStreamResponse(streamContent);
        }
    }

    private ResponseEntity<StreamingResponseBody> asPartialStreamResponse(StreamContentDto streamingContent) {
        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .header(CONTENT_TYPE, streamingContent.mediaType())
                .header(CONTENT_LENGTH, Long.toString(streamingContent.contentLength()))
                .header(ACCEPT_RANGES, "bytes")
                .header(CONTENT_RANGE, streamingContent.contentRange())
                .body(streamingContent.streamingResponseBody());
    }

    private ResponseEntity<StreamingResponseBody> asFullStreamResponse(StreamContentDto streamingContent) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(CONTENT_TYPE, streamingContent.mediaType())
                .header(CONTENT_LENGTH, Long.toString(streamingContent.contentLength()))
                .body(streamingContent.streamingResponseBody());
    }

}
