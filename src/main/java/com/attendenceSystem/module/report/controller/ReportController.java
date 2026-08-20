package com.attendenceSystem.module.report.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.report.dto.request.CreateReportRequest;
import com.attendenceSystem.module.report.service.ReportService;
import com.attendenceSystem.module.user.dto.response.UserWithSpecializationResponse;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Specialization;
import com.attendenceSystem.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.ui.Model;

@Controller
@RequestMapping(Routes.Report.ROOT)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @GetMapping
    public String reportList() {
        return Views.Document.LIST;
    }

    @GetMapping(Routes.Action.CREATE)
    public String reportCreate(Model model) {
        List<UserWithSpecializationResponse> users = userRepository.findAllUsersWithSpecializationByRoleNot(Role.ADMIN);
        model.addAttribute("users", users);
        model.addAttribute("specializations", Specialization.values());
        return Views.Document.CREATE;
    }

    @PostMapping(Routes.Action.CREATE)
    public String submitReport(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "sharedUserIds", required = false) Long[] sharedUserIds,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "link", required = false) String link,
            HttpSession session,
            Model model) {

        try {
            CreateReportRequest request = new CreateReportRequest();
            request.setTitle(title);
            request.setContent(content);
            request.setAttachmentUrl(link);
            request.setSharedUserIds(sharedUserIds);
            request.setFiles(files);

            reportService.createReport(request);

            return Routes.REDIRECT + Routes.Report.ROOT + "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<UserWithSpecializationResponse> users = userRepository.findAllUsersWithSpecializationByRoleNot(Role.ADMIN);
            model.addAttribute("users", users);
            model.addAttribute("specializations", Specialization.values());
            return Views.Document.CREATE;
        }
    }
}
