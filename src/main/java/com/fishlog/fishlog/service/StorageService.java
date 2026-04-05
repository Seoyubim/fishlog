package com.fishlog.fishlog.service;

import com.fishlog.fishlog.config.StorageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/heic");

    private final StorageConfig storageConfig;

    /**
     * 이미지 파일을 로컬 디스크에 저장하고 접근 URL을 반환한다.
     *
     * @return "/uploads/{저장된 파일명}" 형태의 URL
     */
    public String store(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + extension;

        Path targetPath = Paths.get(storageConfig.getUploadDir()).resolve(storedFilename);
        try {
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + storedFilename, e);
        }

        return "/uploads/" + storedFilename;
    }

    /**
     * 저장된 파일을 삭제한다. 파일이 없으면 무시한다.
     */
    public void delete(String photoUrl) {
        String filename = Paths.get(photoUrl).getFileName().toString();
        Path filePath = Paths.get(storageConfig.getUploadDir()).resolve(filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제에 실패했습니다: " + filename, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "허용되지 않는 파일 형식입니다. 허용 형식: JPEG, PNG, WEBP, HEIC");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
