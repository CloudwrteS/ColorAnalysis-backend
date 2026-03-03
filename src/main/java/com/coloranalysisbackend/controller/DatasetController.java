package com.coloranalysisbackend.controller;

import com.coloranalysisbackend.model.Dataset;
import com.coloranalysisbackend.model.Image;
import com.coloranalysisbackend.service.DatasetService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {
    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @PostMapping
    public ResponseEntity<Dataset> createDataset(@RequestBody CreateRequest req) {
        Dataset ds = datasetService.createDataset(req.getName(), req.getDescription(), req.getOwnerId());
        return ResponseEntity.ok(ds);
    }

    @GetMapping
    public ResponseEntity<List<Dataset>> list() {
        return ResponseEntity.ok(datasetService.listDatasets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dataset> get(@PathVariable("id") String id) {
        Dataset ds = datasetService.getDataset(id);
        if (ds == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ds);
    }

    @PostMapping(value = "/{id}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Image> uploadImage(@PathVariable("id") String id,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        Image img = datasetService.storeImage(id, file);
        return ResponseEntity.ok(img);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<Image>> listImages(@PathVariable("id") String id) {
        return ResponseEntity.ok(datasetService.listImages(id));
    }

    public static class CreateRequest {
        private String name;
        private String description;
        private String ownerId;

        // getters/setters omitted for brevity
        public String getName() {return name;} public void setName(String n){name=n;}
        public String getDescription(){return description;} public void setDescription(String d){description=d;}
        public String getOwnerId(){return ownerId;} public void setOwnerId(String o){ownerId=o;}
    }
}