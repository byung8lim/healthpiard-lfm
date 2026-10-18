/*
 * 파일명: FileSystemImageObjectStorage.java
 * 설명: 로컬 파일시스템에 이미지 원본을 불변 객체로 저장한다.
 * 주요기능: SHA-256 계산, 해시 경로 저장, 중복 이미지 재사용
 * 주의사항: 클래스 변경 시 원본 파일을 이동하지 않는다.
 * 변경이력: 2026-10-19 최초 작성
 */
package com.healthpiard.lfmserver.adapter;

import com.healthpiard.lfmserver.config.IngestProperties;
import com.healthpiard.lfmserver.domain.StoredImage;
import com.healthpiard.lfmserver.repository.ImageObjectStorage;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Repository
public class FileSystemImageObjectStorage implements ImageObjectStorage {
    private final Path objectRoot;

    public FileSystemImageObjectStorage(IngestProperties ingestProperties) {
        this.objectRoot = ingestProperties.storageRoot().toAbsolutePath().normalize().resolve("objects");
    }

    /*
     * 기능: 이미지 바이트를 해시 기반 불변 경로에 저장한다.
     * 처리절차: SHA-256 계산 후 임시 파일을 쓰고 원자 이동하며 기존 파일은 재사용한다.
     * 입력: 이미지 바이트와 검증된 MIME 타입
     * 출력: 해시, 상대 저장 키, 크기, 중복 여부
     * 예외: 디렉터리 또는 파일 쓰기에 실패하면 저장소 예외
     */
    @Override
    public StoredImage store(byte[] imageBytes, String contentType) {
        String sha256 = calculateSha256(imageBytes);
        String extension = resolveExtension(contentType);
        Path target = objectRoot.resolve(sha256.substring(0, 2)).resolve(sha256 + extension).normalize();
        ensureInsideObjectRoot(target);
        boolean duplicate = Files.exists(target);
        if (!duplicate) {
            writeAtomically(target, imageBytes);
            validateStoredSize(target, imageBytes.length);
        }
        String storageKey = objectRoot.relativize(target).toString().replace('\\', '/');
        return new StoredImage(sha256, storageKey, imageBytes.length, duplicate);
    }

    private void validateStoredSize(Path target, long expectedSize) {
        try {
            if (Files.size(target) != expectedSize) {
                throw new ImageStorageException("동일 해시의 저장 파일 크기가 일치하지 않습니다.");
            }
        } catch (IOException exception) {
            throw new ImageStorageException("저장한 이미지 원본을 확인할 수 없습니다.", exception);
        }
    }

    private void writeAtomically(Path target, byte[] imageBytes) {
        try {
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(target.getParent(), ".upload-", ".tmp");
            try {
                Files.write(temporary, imageBytes);
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
                } catch (FileAlreadyExistsException ignored) {
                    // 동시에 같은 이미지가 들어온 경우 먼저 저장된 불변 객체를 재사용한다.
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException exception) {
            throw new ImageStorageException("이미지 원본 저장에 실패했습니다.", exception);
        }
    }

    private String calculateSha256(byte[] imageBytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(imageBytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new ImageStorageException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private void ensureInsideObjectRoot(Path target) {
        if (!target.startsWith(objectRoot)) {
            throw new ImageStorageException("허용되지 않은 이미지 저장 경로입니다.");
        }
    }
}
