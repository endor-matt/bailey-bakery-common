package com.baileybakery.common.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Path-validated asset loader that prevents directory traversal.
 * Used for serving user-uploaded content where paths are not trusted.
 */
public class SafeAssetLoader {

    private static final Logger log = LoggerFactory.getLogger(SafeAssetLoader.class);
    private final Path basePath;

    public SafeAssetLoader(String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    public byte[] load(String relativePath) throws IOException {
        Path resolved = basePath.resolve(relativePath).normalize();

        if (!resolved.startsWith(basePath)) {
            throw new SecurityException("Path traversal attempt blocked: " + relativePath);
        }

        if (!Files.exists(resolved)) {
            throw new IOException("Asset not found: " + relativePath);
        }

        log.info("Loading validated asset: {}", resolved);
        return Files.readAllBytes(resolved);
    }

    public String loadText(String relativePath) throws IOException {
        return new String(load(relativePath));
    }
}
