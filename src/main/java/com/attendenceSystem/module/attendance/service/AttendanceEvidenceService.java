package com.attendenceSystem.module.attendance.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.storage.provider.StorageProvider;
import com.attendenceSystem.module.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceEvidenceService {
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StorageProvider storageProvider;
    private final ZoneId applicationZoneId;

    public void saveCheckInImage(User user, String imageBase64) {
        saveImage(user, imageBase64, true);
    }

    public void saveCheckOutImage(User user, String imageBase64) {
        saveImage(user, imageBase64, false);
    }

    private void saveImage(User user, String imageBase64, boolean checkIn) {
        if (!StringUtils.hasText(imageBase64)) {
            return;
        }

        DecodedImage image = decode(imageBase64);
        String action = checkIn ? "checkin" : "checkout";
        String relativePath = "attendance_evidence/" + user.getId() + "/" + action + "_"
                + LocalDateTime.now().format(FILE_TIMESTAMP) + "_" + UUID.randomUUID() + "." + image.extension();

        try {
            storageProvider.save(Path.of(relativePath), image.bytes());
            AttendanceRecord record = attendanceRecordRepository
                    .findByUserAndAttendanceDate(user, LocalDate.now(applicationZoneId))
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy bản ghi điểm danh vừa tạo"));
            if (checkIn) {
                record.setCheckInImagePath(relativePath);
            } else {
                record.setCheckOutImagePath(relativePath);
            }
            attendanceRecordRepository.save(record);
        } catch (IOException | RuntimeException exception) {
            try {
                storageProvider.delete(relativePath);
            } catch (RuntimeException ignored) {
                // Preserve the original failure while attempting best-effort cleanup.
            }
            throw new IllegalStateException("Không thể lưu ảnh điểm danh", exception);
        }
    }

    private DecodedImage decode(String dataUrl) {
        if (!dataUrl.startsWith("data:image/")) {
            throw new IllegalArgumentException("Ảnh điểm danh không hợp lệ");
        }
        int separator = dataUrl.indexOf(",");
        if (separator < 0 || !dataUrl.substring(0, separator).contains(";base64")) {
            throw new IllegalArgumentException("Ảnh điểm danh không hợp lệ");
        }

        String header = dataUrl.substring(0, separator).toLowerCase();
        String extension = header.contains("image/png") ? "png" : header.contains("image/jpeg") || header.contains("image/jpg") ? "jpg" : null;
        if (extension == null) {
            throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPEG hoặc PNG");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUrl.substring(separator + 1));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Ảnh điểm danh không hợp lệ", exception);
        }
        if (bytes.length == 0 || bytes.length > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Kích thước ảnh điểm danh không hợp lệ");
        }
        return new DecodedImage(bytes, extension);
    }

    private record DecodedImage(byte[] bytes, String extension) {
    }
}