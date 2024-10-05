package com.stream.example.service.attachment;

import com.stream.example.dto.StreamContentDto;
import com.stream.example.enums.AttachmentType;
import com.stream.example.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class AttachmentServiceImpl implements AttachmentService {

    private final ObjectStorage objectStorage;

    @Override
    public StreamContentDto getStreamContent(AttachmentType attachmentType, @Nullable String range) {
        return objectStorage.streamContent(attachmentType, range);
    }

}
