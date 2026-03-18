package com.coloranalysisbackend.controller;

import com.coloranalysisbackend.model.Project;
import com.coloranalysisbackend.model.Task;
import com.coloranalysisbackend.service.ProjectAnalysisService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectAnalysisService projectAnalysisService;

    public ProjectController(ProjectAnalysisService projectAnalysisService) {
        this.projectAnalysisService = projectAnalysisService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateProjectRequest req) {
        try {
            Project p = projectAnalysisService.createProject(
                    req.getName(),
                    req.getOwnerId(),
                    req.getDatasetId(),
                    req.getTemplateId(),
                    req.getConfig()
            );
            return ResponseEntity.ok(p);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Project>> list() {
        return ResponseEntity.ok(projectAnalysisService.listProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> get(@PathVariable("id") String id) {
        Project p = projectAnalysisService.getProject(id);
        if (p == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(p);
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<?> run(@PathVariable("id") String id, @RequestBody RunProjectRequest req) {
        try {
            Task task = projectAnalysisService.runProject(
                    id,
                    req.getSteps(),
                    req.getModelImagePath(),
                    req.getButterflyJsonPath(),
                    req.getEdgeJsonPath(),
                    req.getNotes()
            );
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<Task>> listTasks(@PathVariable("id") String id) {
        return ResponseEntity.ok(projectAnalysisService.listProjectTasks(id));
    }

    @Data
    public static class CreateProjectRequest {
        private String name;
        private String ownerId;
        private String datasetId;
        private String templateId;
        private Map<String, Object> config;
    }

    @Data
    public static class RunProjectRequest {
        private List<String> steps;
        private String modelImagePath;
        private String butterflyJsonPath;
        private String edgeJsonPath;
        private String notes;
    }
}
