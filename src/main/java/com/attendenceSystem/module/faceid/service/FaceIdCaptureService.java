package com.attendenceSystem.module.faceid.service;

import com.attendenceSystem.module.faceid.dto.request.FaceCaptureRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceCaptureResponse;
import com.attendenceSystem.module.faceid.dto.response.LatestFaceResponse;

public interface FaceIdCaptureService {
    FaceCaptureResponse registerFace(FaceCaptureRequest request);

    FaceAiClient.PoseResult checkPose(String imageBase64);

    LatestFaceResponse getLatestFace();

    FaceCaptureResponse updateForAttendance(FaceCaptureRequest request);

    void deleteFaceSample(Long sampleId);
}
