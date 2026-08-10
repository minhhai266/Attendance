package com.attendenceSystem.module.faceid.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.entity.FaceSample;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.repository.FaceSampleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceEmbeddingCacheService {

    private final FaceProfileRepository faceProfileRepository;
    private final FaceSampleRepository faceSampleRepository;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile List<FaceEmbeddingEntry> cachedEmbeddings = null;

    public List<FaceEmbeddingEntry> getKnownEmbeddings() {
        List<FaceEmbeddingEntry> snapshot = cachedEmbeddings;
        if (snapshot != null) {
            return snapshot;
        }

        lock.lock();
        try {
            if (cachedEmbeddings == null) {
                cachedEmbeddings = loadFromDatabase();
                log.info("Đã nạp {} embedding khuôn mặt vào cache", cachedEmbeddings.size());
            }
            return cachedEmbeddings;
        } finally {
            lock.unlock();
        }
    }

    public void invalidate() {
        lock.lock();
        try {
            cachedEmbeddings = null;
        } finally {
            lock.unlock();
        }
        log.info("Đã invalidate face embedding cache");
    }

    private List<FaceEmbeddingEntry> loadFromDatabase() {
        List<FaceEmbeddingEntry> entries = new ArrayList<>();
        List<FaceProfile> profiles = faceProfileRepository.findAll();

        for (FaceProfile profile : profiles) {
            List<FaceSample> samples = faceSampleRepository.findByFaceProfile(profile);
            for (FaceSample sample : samples) {
                if (!StringUtils.hasText(sample.getEmbedding())) {
                    continue;
                }
                try {
                    float[] vector = decodeEmbedding(sample.getEmbedding());
                    entries.add(new FaceEmbeddingEntry(
                            profile.getUser().getId(),
                            profile.getUser().getUsername(),
                            profile.getUser().getFullName(),
                            vector
                    ));
                } catch (IllegalArgumentException ex) {
                    log.warn("Embedding lỗi định dạng cho user {}", profile.getUser().getUsername());
                }
            }
        }
        return entries;
    }

    private static float[] decodeEmbedding(String embedding) {
        byte[] bytes = Base64.getDecoder().decode(embedding);
        if (bytes.length % Float.BYTES != 0) {
            throw new IllegalArgumentException("Invalid embedding length");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] values = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.getFloat();
        }
        return values;
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0.0 : dot / denom;
    }
}
