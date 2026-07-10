package com.ecommerce.inventory.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Added in Version 6. Handles local file system storage for product images -
 * validation, saving, loading, and deleting. Kept separate from ProductService so
 * file system concerns don't leak into product business logic.
 */
public interface FileStorageService {

    /**
     * Validates and stores the given file on disk, returning the generated stored
     * filename (not the original filename, to avoid collisions/path issues).
     */
    String storeFile(MultipartFile file);

    /**
     * Loads a previously stored file's bytes and resolved content type.
     */
    ProductImageData loadFile(String filename);

    /**
     * Deletes a previously stored file, if it exists. No-op if filename is null/blank
     * or the file is already gone.
     */
    void deleteFile(String filename);
}
