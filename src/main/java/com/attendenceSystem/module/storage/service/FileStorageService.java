package com.attendenceSystem.module.storage.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveFile(MultipartFile file, String directory) throws IOException;
}
