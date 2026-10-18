package com.healthpiard.lfmserver.adapter;

import com.healthpiard.lfmserver.config.IngestProperties;
import com.healthpiard.lfmserver.domain.StoredImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemImageObjectStorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void reusesSameImageBySha256() {
        FileSystemImageObjectStorage storage = new FileSystemImageObjectStorage(
                new IngestProperties(temporaryDirectory, 1024));

        StoredImage first = storage.store(new byte[]{1, 2, 3}, "image/jpeg");
        StoredImage second = storage.store(new byte[]{1, 2, 3}, "image/jpeg");

        assertThat(first.duplicate()).isFalse();
        assertThat(second.duplicate()).isTrue();
        assertThat(second.storageKey()).isEqualTo(first.storageKey());
        assertThat(second.sha256()).isEqualTo(first.sha256());
    }
}
