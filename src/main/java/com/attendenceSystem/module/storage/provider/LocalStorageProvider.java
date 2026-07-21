package com.attendenceSystem.module.storage.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalStorageProvider implements StorageProvider {

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
        Path fullPath = rootDir.resolve(targetPath).normalize();

        // Kiểm tra path traversal
        if (!fullPath.startsWith(rootDir)) {
            throw new SecurityException("Path traversal không hợp lệ: " + targetPath);
        }

        Files.createDirectories(fullPath.getParent());
        file.transferTo(fullPath);
    }

    @Override
    public Resource load(String path) throws IOException {
        Path fullPath = rootDir.resolve(path).normalize();

        // Kiểm tra path traversal
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
    public void delete(String path) throws IOException {
        Path fullPath = rootDir.resolve(path).normalize();

        // Kiểm tra path traversal
        if (!fullPath.startsWith(rootDir)) {
            throw new SecurityException("Path traversal không hợp lệ: " + path);
        }

        Files.deleteIfExists(fullPath);
    }

    @Override
    public String getPublicUrl(String relativePath) {
        return "/uploads/" + relativePath;
    }

    @Override
    public Path resolvePath(String relativePath) {
        Path fullPath = rootDir.resolve(relativePath).normalize();
        if (!fullPath.startsWith(rootDir)) {
            throw new SecurityException("Path traversal không hợp lệ: " + relativePath);
        }
        return fullPath;
    }
}
