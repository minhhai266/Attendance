package com.attendenceSystem.module.storage.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.attendenceSystem.constant.FileConstants;
import com.attendenceSystem.module.storage.exception.FileStorageException;
import com.attendenceSystem.module.storage.provider.StorageProvider;
import com.attendenceSystem.module.storage.service.FileStorageService;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final StorageProvider storageProvider;

    public FileStorageServiceImpl(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    @Override
    public String saveFile(
            final MultipartFile file,
            final String directory) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File upload không hợp lệ");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !FileConstants.ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Định dạng file không được hỗ trợ");
        }

        // Validate directory (chống path traversal)
        validateDirectory(directory);

        // Tính SHA-256 hash từ nội dung file
        String fileHash = sha256Hex(file);

        String fileName = fileHash + "." + extension;
        String relativePath = directory + "/" + fileName;
        Path targetPath = Paths.get(directory, fileName);

        // Lưu file qua StorageProvider
        try {
            storageProvider.save(targetPath, file);
        } catch (IOException e) {
            throw new FileStorageException("Không thể lưu file", e);
        }

        // Detect và kiểm tra MIME type từ file đã lưu
        Path savedPath = storageProvider.resolvePath(relativePath);
        String detectedMime;
        try {
            detectedMime = Files.probeContentType(savedPath);
        } catch (IOException e) {
            throw new FileStorageException("Không thể xác định MIME type", e);
        }
        String expectedMime = FileConstants.ALLOWED_EXTENSION_MIME.get(extension.toLowerCase());
        if (detectedMime == null || expectedMime == null || !expectedMime.equals(detectedMime)) {
            // Xóa file nếu MIME type không khớp
            storageProvider.delete(relativePath);
            throw new IllegalArgumentException(
                    "Nội dung file không khớp với định dạng khai báo");
        }

        return storageProvider.getPublicUrl(relativePath);
    }

    @Override
    public Resource loadFile(final String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Đường dẫn file không hợp lệ");
        }
        return storageProvider.load(path);
    }

    @Override
    public void deleteFile(final String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Đường dẫn file không hợp lệ");
        }
        storageProvider.delete(path);
    }

    private String sha256Hex(final MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            try (InputStream is = file.getInputStream()) {
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (IOException e) {
            throw new FileStorageException("Không thể đọc nội dung file", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không được hỗ trợ", e);
        }
    }

    private void validateDirectory(final String directory) {
        if (directory == null || directory.isBlank()) {
            throw new IllegalArgumentException("Directory không hợp lệ");
        }
        if (directory.contains("..")) {
            throw new IllegalArgumentException(
                    "Directory không hợp lệ: chứa path traversal");
        }
        if (directory.contains("/") || directory.contains("\\")) {
            throw new IllegalArgumentException(
                    "Directory không hợp lệ: chứa ký tự phân cách");
        }
        if (!directory.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Directory chứa ký tự không hợp lệ");
        }
    }
}