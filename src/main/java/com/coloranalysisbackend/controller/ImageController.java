package com.coloranalysisbackend.controller;

import com.coloranalysisbackend.service.PythonClientService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final PythonClientService pythonClientService;

    public ImageController(PythonClientService pythonClientService) {
        this.pythonClientService = pythonClientService;
    }

    /**
     * 把上传的图像发送给本地的 Python 服务进行 Canny 边缘检测
     */
    @PostMapping(value = "/canny", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> canny(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] input = file.getBytes();
        byte[] output = pythonClientService.callCanny(input);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(output);
    }
}
