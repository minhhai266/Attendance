package com.attendenceSystem.module.storage.service;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Lưu file lên storage
     * @param file file upload
     * @param directory thư mục con (VD: "avatars", "documents")
     * @return đường dẫn public để truy cập file
     */
    String saveFile(MultipartFile file, String directory) throws IOException;

    /**
     * Đọc file từ storage
     * @param path đường dẫn file (relative, VD: "documents/abc123.pdf")
     * @return Resource để stream/download
     */
    Resource loadFile(String path) throws IOException;

    /**
     * Xoá file khỏi storage
     * @param path đường dẫn file (relative)
     */
    void deleteFile(String path) throws IOException;
}