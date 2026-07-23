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
import com.attendenceSystem.module.user.entity.enums.Department;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;

@Controller
@RequestMapping(Routes.Report.ROOT)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String reportList() {
        return Views.Document.LIST;
    }

    @GetMapping(Routes.Action.CREATE)
    public String reportCreate(Model model) {
        model.addAttribute("departments", Department.values());
        return Views.Document.CREATE;
    }

    @PostMapping(Routes.Action.CREATE)
    public String submitReport(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("departmentId") String departmentId,
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

            reportService.createReport(request);

            return Routes.REDIRECT + Routes.Report.ROOT + "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", Department.values());
            return Views.Document.CREATE;
        }
    }
}
