package com.attendenceSystem.module.storage.provider;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction cho storage backend (local filesystem, S3, MinIO, ...)
 */
public interface StorageProvider {

    /**
     * Lưu file vào storage
     * @param targetPath đường dẫn đích (relative)
     * @param file file upload
     * @throws IOException nếu lỗi I/O
     */
    void save(Path targetPath, MultipartFile file) throws IOException;

    /**
     * Đọc file từ storage dưới dạng Resource
     * @param path đường dẫn file (relative)
     * @return Resource để download/stream
     * @throws IOException nếu file không tồn tại hoặc lỗi I/O
     */
    Resource load(String path) throws IOException;

    /**
     * Xoá file khỏi storage
     * @param path đường dẫn file (relative)
     * @throws IOException nếu lỗi I/O
     */
    void delete(String path);

    /**
     * Tạo đường dẫn public để truy cập file (URL cho web)
     * @param relativePath đường dẫn relative
     * @return đường dẫn public
     */
    String getPublicUrl(String relativePath);

    /**
     * Giải quyết đường dẫn relative thành Path thực tế
     * @param relativePath đường dẫn relative
     * @return Path thực tế trên filesystem
     */
    Path resolvePath(String relativePath);

    void save(Path targetPath, byte[] data) throws IOException;
}