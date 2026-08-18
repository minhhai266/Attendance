package com.attendenceSystem.module.faceid.repository.projection;

import java.time.LocalDateTime;

public interface FaceIdDetail {
    Long getUserId();
    String getUserFullName();
    String getFaceCode();
    Integer getSampleCount();
    String getUserEmail();
    Boolean getIsAccept();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdateAt();

    String getThumbnailUrl();
}