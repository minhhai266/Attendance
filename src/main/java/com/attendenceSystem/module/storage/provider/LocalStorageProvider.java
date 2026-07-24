package com.attendenceSystem.module.storage.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.attendenceSystem.module.storage.exception.FileStorageException;

@Component
public class LocalStorageProvider implements StorageProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageProvider.class);

    private final Path rootDir;

    public LocalStorageProvider(@Value("${app.storage.upload-dir}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục gốc: " + this.rootDir, e);
        }
    }

    @Override
    public void save(Path targetPath, MultipartFile file) throws IOException {
        // Chuẩn hóa path để tránh dấu "/" ở đầu gây lỗi path traversal
        String pathStr = normalizeRelativePath(targetPath.toString());
        Path normalizedPath = Paths.get(pathStr);
        Path fullPath = rootDir.resolve(normalizedPath).normalize();

        // Kiểm tra path traversal thực sự (chứa ..)
        if (!fullPath.startsWith(rootDir)) {
            log.warn("Path traversal detected! rootDir={}, targetPath={}, fullPath={}", rootDir, targetPath, fullPath);
            throw new SecurityException("Path traversal không hợp lệ: " + targetPath);
        }

        log.debug("Saving file: rootDir={}, targetPath={}, fullPath={}", rootDir, targetPath, fullPath);
        Files.createDirectories(fullPath.getParent());
        file.transferTo(fullPath);
    }

    @Override
    public Resource load(String path) throws IOException {
        String normalizedPath = normalizeRelativePath(path);
        Path fullPath = rootDir.resolve(normalizedPath).normalize();

        // Kiểm tra path traversal thực sự (chứa ..)
        if (!fullPath.startsWith(rootDir)) {
            throw new SecurityException("Path traversal không hợp lệ: " + path);
        }

        Resource resource = new FileSystemResource(fullPath);
        if (!resource.exists()) {
            throw new IOException("File không tồn tại: " + path);
        }
        return resource;
    }

    @Override
    public void delete(String path) {
        String normalizedPath = normalizeRelativePath(path);
        Path fullPath = rootDir.resolve(normalizedPath).normalize();

        // Kiểm tra path traversal thực sự (chứa ..)
        if (!fullPath.startsWith(rootDir)) {
            throw new SecurityException("Path traversal không hợp lệ: " + path);
        }

        try {
            Files.deleteIfExists(resolvePath(path));
        } catch (IOException e) {
            throw new FileStorageException("Không thể xóa file", e);
        }
    }

    @Override
    public String getPublicUrl(String relativePath) {
        return "/uploads/" + relativePath;
    }

    @Override
    public Path resolvePath(String relativePath) {
        String normalizedPath = normalizeRelativePath(relativePath);
        Path fullPath = rootDir.resolve(normalizedPath).normalize();
        if (!fullPath.startsWith(rootDir)) {
            throw new SecurityException("Path traversal không hợp lệ: " + relativePath);
        }
        return fullPath;
    }

    @Override
    public void save(Path targetPath, byte[] data) throws IOException {
        String pathStr = normalizeRelativePath(targetPath.toString());
        Path normalizedPath = Paths.get(pathStr);
        Path fullPath = rootDir.resolve(normalizedPath).normalize();

        // Kiểm tra path traversal
        if (!fullPath.startsWith(rootDir)) {
            log.warn("Path traversal detected! rootDir={}, targetPath={}, fullPath={}", rootDir, targetPath, fullPath);
            throw new SecurityException("Path traversal không hợp lệ: " + targetPath);
        }

        log.debug("Saving byte array file: rootDir={}, targetPath={}, fullPath={}", rootDir, targetPath, fullPath);
        Files.createDirectories(fullPath.getParent());

        Files.write(fullPath, data);
    }

    /**
     * Chuẩn hóa đường dẫn relative: loại bỏ dấu "/" ở đầu để tránh
     * rootDir.resolve() coi là đường dẫn tuyệt đối.
     */
    private String normalizeRelativePath(String path) {
        if (path == null)
            return null;
        String normalized = path;
        // Loại bỏ dấu "/" hoặc "\" ở đầu để tránh path traversal false positive
        while (normalized.startsWith("/") || normalized.startsWith("\\")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
