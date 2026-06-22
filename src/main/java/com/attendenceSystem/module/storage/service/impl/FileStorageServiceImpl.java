package com.attendenceSystem.module.storage.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.attendenceSystem.constant.FileConstants;
import com.attendenceSystem.module.storage.service.FileStorageService;

public class FileStorageServiceImpl implements FileStorageService {

    @Override
    public String saveFile(
            MultipartFile file,
            String directory) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File upload không hợp lệ");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !FileConstants.ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Định dạng file không được hỗ trợ");
        }

        String fileHash;
        try (InputStream is = file.getInputStream()) {
            fileHash = DigestUtils.md5DigestAsHex(is);
        }
        String fileName = fileHash + extension;
        Path uploadDir = Paths.get("uploads", directory);
        Files.createDirectories(uploadDir);
        Path targetPath = uploadDir.resolve(fileName);
        if (!Files.exists(targetPath)) {
            file.transferTo(targetPath);
        }
        return "/uploads/"
                + directory
                + "/"
                + fileName;
    }
}
