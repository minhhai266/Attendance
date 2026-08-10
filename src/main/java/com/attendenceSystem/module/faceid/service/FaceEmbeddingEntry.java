package com.attendenceSystem.module.faceid.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FaceEmbeddingEntry {
    private final Long userId;
    private final String studentCode;
    private final String fullName;
    private final float[] vector;
}
