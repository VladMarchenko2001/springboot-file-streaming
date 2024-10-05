package com.stream.example.storage;

import com.stream.example.dto.StreamContentDto;
import com.stream.example.enums.AttachmentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class ObjectStorageImpl implements ObjectStorage {

    private static final String MEDIA_TYPE_VIDEO_MP4 = "video/mp4";
    private static final String MEDIA_TYPE_IMAGE_PNG = "image/png";
    private static final String MEDIA_TYPE_HTML = "text/html";

    @Override
    public StreamContentDto streamContent(AttachmentType attachmentType, @Nullable String range) {
        log.info("Streaming range content for attachment type: {}, range: {}", attachmentType, range);
        try {
            Path filePath = getObjectPath(attachmentType);
            long fileSize = Files.size(filePath);
            Pair<Long, Long> ranges = getRange(range, fileSize);

            boolean partial = range != null;
            return new StreamContentDto(
                    partial,
                    getMediaType(attachmentType),
                    getContentLength(ranges),
                    getContentRange(ranges, fileSize),
                    getStreamingResponseBody(filePath, ranges));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found", e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while streaming content", e);
        }
    }

    private static StreamingResponseBody getStreamingResponseBody(Path filePath, Pair<Long, Long> ranges) {
        byte[] buffer = new byte[1024];
        return os -> {
            try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                long pos = ranges.getLeft();
                file.seek(pos);

                while (pos < ranges.getRight()) {
                    file.read(buffer);
                    os.write(buffer);
                    pos += buffer.length;
                }

                os.flush();
            } catch (Exception ignored) {
                log.error("Error occurred while streaming content {}", ignored.getMessage());
            }
        };
    }

    private static long getContentLength(Pair<Long, Long> ranges) {
        return (ranges.getRight() - ranges.getLeft()) + 1;
    }

    private static String getContentRange(Pair<Long, Long> ranges, long fileSize) {
        return new StringBuilder()
                .append("bytes ")
                .append(ranges.getLeft())
                .append("-")
                .append(ranges.getRight())
                .append("/")
                .append(fileSize)
                .toString();
    }

    private String getObjectName(AttachmentType attachmentType) {
        return switch (attachmentType) {
            case VIDEO -> "video.mov";
            case IMAGE -> "image.png";
            case FILE -> "html.html";
            default -> "";
        };
    }

    private Path getObjectPath(AttachmentType attachmentType) throws FileNotFoundException {
        return getObjectPath(getObjectName(attachmentType));
    }

    private Path getObjectPath(String objectName) throws FileNotFoundException {
        URL mediaResource = ObjectStorage.class.getClassLoader().getResource(objectName);

        if (mediaResource != null) {
            try {
                return Paths.get(mediaResource.toURI());
            } catch (URISyntaxException e) {
                throw new FileNotFoundException();
            }
        }

        throw new FileNotFoundException();
    }

    private String getMediaType(AttachmentType attachmentType) {
        switch (attachmentType) {
            case VIDEO:
                return MEDIA_TYPE_VIDEO_MP4;
            case IMAGE:
                return MEDIA_TYPE_IMAGE_PNG;
            case FILE:
                return MEDIA_TYPE_HTML;
            default:
                return MediaType.APPLICATION_OCTET_STREAM.getType();
        }
    }

    private Pair<Long, Long> getRange(@Nullable String range, long fileSize) {
        if (range == null) {
            return Pair.of(0L, fileSize - 1);
        }
        String[] ranges = range.split("-");
        Long rangeStart = Long.parseLong(ranges[0].substring(6));
        long rangeEnd;
        if (ranges.length > 1) {
            rangeEnd = Long.parseLong(ranges[1]);
        } else {
            rangeEnd = fileSize - 1;
        }
        if (fileSize < rangeEnd) {
            rangeEnd = fileSize - 1;
        }
        return Pair.of(rangeStart, rangeEnd);
    }

}
