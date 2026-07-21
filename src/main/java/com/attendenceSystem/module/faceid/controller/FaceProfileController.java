package com.attendenceSystem.module.faceid.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.faceid.dto.request.CreateFaceProfileRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceIdResponse;
import com.attendenceSystem.module.faceid.mapper.response.FaceIdResponseMapper;
import com.attendenceSystem.module.faceid.service.FaceProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(Routes.FaceId.ROOT)
public class FaceProfileController {

    private final FaceProfileService faceProfileService;
    private final FaceIdResponseMapper faceIdResponseMapper;

    @GetMapping
    public String list(@ModelAttribute("query") String query,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        Page<FaceIdResponse> faceIdResponses = faceProfileService.searchFaceProfiles(query, pageable)
                .map(faceIdResponseMapper::fromEntity);
        model.addAttribute("faceProfiles", faceIdResponses);
        return Views.FaceId.LIST;
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("createFaceProfileRequest", new CreateFaceProfileRequest());
        return Views.FaceId.CREATE;
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute CreateFaceProfileRequest request,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return Views.FaceId.CREATE;
        }
        faceProfileService.createFaceProfile(request);
        return Routes.REDIRECT + Routes.FaceId.ROOT;
    }
}
