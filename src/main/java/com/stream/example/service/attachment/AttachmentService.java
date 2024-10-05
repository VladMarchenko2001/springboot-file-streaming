package com.stream.example.service.attachment;

import com.stream.example.dto.StreamContentDto;
import com.stream.example.enums.AttachmentType;
import org.springframework.lang.Nullable;

public interface AttachmentService {

    StreamContentDto getStreamContent(AttachmentType attachmentType, @Nullable String range);

}
