package com.attendenceSystem.constant;

import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileConstants {
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf",
            "doc",
            "docx",
            "xls",
            "xlsx",
            "jpg",
            "jpeg",
            "png",
            "zip");
}
