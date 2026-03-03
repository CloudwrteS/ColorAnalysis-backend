package com.coloranalysisbackend.service;

import com.coloranalysisbackend.model.Dataset;
import com.coloranalysisbackend.model.Image;
import com.coloranalysisbackend.repository.DatasetRepository;
import com.coloranalysisbackend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DatasetService {
    private final DatasetRepository datasetRepository;
    private final ImageRepository imageRepository;
    private final Path baseDir;

    public DatasetService(DatasetRepository datasetRepository,
                          ImageRepository imageRepository,
                          @Value("${storage.base-dir}") String baseDir) throws IOException {
        this.datasetRepository = datasetRepository;
        this.imageRepository = imageRepository;
        this.baseDir = Paths.get(baseDir);
        if (!Files.exists(this.baseDir)) {
            Files.createDirectories(this.baseDir);
        }
    }

    public Dataset createDataset(String name, String description, String ownerId) {
        Dataset d = new Dataset();
        d.setId(UUID.randomUUID().toString());
        d.setName(name);
        d.setDescription(description);
        d.setOwnerId(ownerId);
        d.setStoragePrefix(d.getId());
        d.setFileCount(0);
        return datasetRepository.save(d);
    }

    public Dataset getDataset(String id) {
        return datasetRepository.findById(id).orElse(null);
    }

    public List<Dataset> listDatasets() {
        return datasetRepository.findAll();
    }

    public Image storeImage(String datasetId, MultipartFile file) throws IOException {
        Dataset ds = getDataset(datasetId);
        if (ds == null) {
            throw new IllegalArgumentException("dataset not found");
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = UUID.randomUUID().toString();
        }
        Path dir = baseDir.resolve(ds.getStoragePrefix());
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target);
        Image img = new Image();
        img.setId(UUID.randomUUID().toString());
        img.setDatasetId(datasetId);
        img.setFileName(filename);
        img.setStorageKey(target.toString());
        img = imageRepository.save(img);
        ds.setFileCount(ds.getFileCount() + 1);
        datasetRepository.save(ds);
        return img;
    }

    public List<Image> listImages(String datasetId) {
        return imageRepository.findByDatasetId(datasetId);
    }
}