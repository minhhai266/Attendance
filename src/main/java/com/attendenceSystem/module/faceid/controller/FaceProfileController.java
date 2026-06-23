package com.attendenceSystem.module.faceid.controller;

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
import com.attendenceSystem.module.faceid.dto.request.CreateFaceProfileRequest;
import com.attendenceSystem.module.faceid.service.FaceProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/faceid")
public class FaceProfileController {

    private final FaceProfileService faceProfileService;

    @GetMapping
    public String list(@ModelAttribute("query") String query,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        model.addAttribute("faceProfiles", faceProfileService.searchFaceProfiles(query, pageable));
        return "cms/face-id/faceID-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("createFaceProfileRequest", new CreateFaceProfileRequest());
        return "cms/face-id/faceID-create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute CreateFaceProfileRequest request,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "cms/face-id/faceID-create";
        }
        faceProfileService.createFaceProfile(request);
        return Routes.REDIRECT + "/faceid";
    }
}
