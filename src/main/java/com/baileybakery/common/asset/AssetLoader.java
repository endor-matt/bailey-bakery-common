package com.baileybakery.common.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads static assets (images, PDFs, cake design templates) from the
 * configured asset storage directory. Supports both local filesystem
 * and mounted NFS volumes used in production.
 */
public class AssetLoader {

    private static final Logger log = LoggerFactory.getLogger(AssetLoader.class);

    private final String basePath;

    public AssetLoader(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Loads a file from the asset directory. Used for serving cake design images,
     * downloadable menus, and supplier invoices.
     *
     * @param relativePath the path relative to the asset base directory
     * @return the file contents as bytes
     */
    public byte[] load(String relativePath) throws IOException {
        Path filePath = Paths.get(basePath, relativePath);
        log.info("Loading asset: {}", filePath);

        if (!Files.exists(filePath)) {
            throw new IOException("Asset not found: " + relativePath);
        }

        return Files.readAllBytes(filePath);
    }

    /**
     * Loads a file and returns it as a string. Used for loading HTML email templates
     * and text-based configuration files.
     *
     * @param relativePath the path relative to the asset base directory
     * @return the file contents as a string
     */
    public String loadText(String relativePath) throws IOException {
        return new String(load(relativePath));
    }

    /**
     * Checks if an asset exists at the given path.
     *
     * @param relativePath the path relative to the asset base directory
     * @return true if the asset exists
     */
    public boolean exists(String relativePath) {
        return Files.exists(Paths.get(basePath, relativePath));
    }

    /**
     * Returns the MIME type for the given asset path based on file extension.
     *
     * @param relativePath the asset path
     * @return the MIME type string
     */
    public String getMimeType(String relativePath) throws IOException {
        Path path = Paths.get(basePath, relativePath);
        String mimeType = Files.probeContentType(path);
        return mimeType != null ? mimeType : "application/octet-stream";
    }
}
