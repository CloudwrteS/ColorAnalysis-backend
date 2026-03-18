package com.coloranalysisbackend.controller;

import com.coloranalysisbackend.service.ReportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/projects/{projectId}/summary")
    public ResponseEntity<?> projectSummary(@PathVariable String projectId) {
        try {
            Map<String, Object> summary = reportService.getProjectSummary(projectId);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/projects/{projectId}/images/{imageName}")
    public ResponseEntity<?> singleImageReport(@PathVariable String projectId,
                                               @PathVariable String imageName) {
        try {
            Map<String, Object> detail = reportService.getSingleImageReport(projectId, imageName);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/projects/{projectId}/export")
    public ResponseEntity<?> export(@PathVariable String projectId,
                                    @RequestParam(defaultValue = "csv") String format) {
        try {
            File file = reportService.exportProjectSummary(projectId, format);
            String lowered = format.toLowerCase();
            MediaType contentType = switch (lowered) {
                case "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                case "pdf" -> MediaType.APPLICATION_PDF;
                default -> MediaType.parseMediaType("text/csv");
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(contentType)
                    .body(new FileSystemResource(file));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }
}
