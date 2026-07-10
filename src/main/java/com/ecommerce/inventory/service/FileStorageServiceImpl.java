package com.ecommerce.inventory.service;

import com.ecommerce.inventory.exception.EmptyFileException;
import com.ecommerce.inventory.exception.FileSizeExceededException;
import com.ecommerce.inventory.exception.FileStorageException;
import com.ecommerce.inventory.exception.ImageNotFoundException;
import com.ecommerce.inventory.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Added in Version 6. Stores product images on the local file system under the
 * directory configured by "file.upload-dir" (see application.properties), and never
 * persists image binary data in the database - only the stored filename is kept on
 * the Product entity.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private final Path uploadDirPath;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirPath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory: " + uploadDir, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            Path targetPath = uploadDirPath.resolve(storedFilename).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store uploaded file: " + originalFilename, ex);
        }
    }

    @Override
    public ProductImageData loadFile(String filename) {
        Path filePath = uploadDirPath.resolve(filename).normalize();

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new ImageNotFoundException("Image file not found on disk: " + filename);
        }

        try {
            byte[] data = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = resolveContentTypeFromExtension(getExtension(filename));
            }
            return new ProductImageData(data, contentType);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to read image file: " + filename, ex);
        }
    }

    @Override
    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path filePath = uploadDirPath.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to delete image file: " + filename, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EmptyFileException("Uploaded image file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileSizeExceededException("File size exceeds the maximum allowed limit of 5 MB");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = getExtensionOrEmpty(originalFilename);

        boolean validContentType = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
        boolean validExtension = ALLOWED_EXTENSIONS.contains(extension.toLowerCase());

        if (!validContentType || !validExtension) {
            throw new InvalidFileTypeException("Only JPG, JPEG, and PNG image files are allowed");
        }
    }

    private String getExtension(String filename) {
        String extension = getExtensionOrEmpty(filename);
        if (extension.isEmpty()) {
            throw new InvalidFileTypeException("Uploaded file must have a valid image extension (.jpg, .jpeg, .png)");
        }
        return extension;
    }

    private String getExtensionOrEmpty(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private String resolveContentTypeFromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }
}
