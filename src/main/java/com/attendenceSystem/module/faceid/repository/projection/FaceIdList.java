package com.attendenceSystem.module.faceid.repository.projection;

public interface FaceIdList {
    Long getUserId();
    String getUserFullName();
    String getFaceCode();
    Boolean isAccept();
}