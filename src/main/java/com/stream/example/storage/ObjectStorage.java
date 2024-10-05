package com.stream.example.storage;

import com.stream.example.dto.StreamContentDto;
import com.stream.example.enums.AttachmentType;
import org.springframework.lang.Nullable;

public interface ObjectStorage {

    StreamContentDto streamContent(AttachmentType attachmentType, @Nullable String range);

}
