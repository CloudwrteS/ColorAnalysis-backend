package com.coloranalysisbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Map;

@Service
public class PythonClientService {
    private final WebClient webClient;

    public PythonClientService(WebClient.Builder builder, com.coloranalysisbackend.config.PythonProperties props) {
        this.webClient = builder.baseUrl(props.getUrl()).build();
    }

    /**
     * 获取 canny 多边形区域 JSON
     */
    public Map<String, Object> detectCanny(byte[] imageBytes, Map<String,Object> config) {
        return webClient.post()
                .uri(uri -> uri.path("/canny").build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("image", imageBytes)
                        .with("config", config.toString()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /**
     * 角点检测
     */
    public Map<String, Object> detectPoints(byte[] imageBytes) {
        return webClient.post()
                .uri("/image/correction/points")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("image", imageBytes))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /**
     * 图像校正：传入模板和待校正图片，返回校正后的图片字节
     */
    public byte[] alignImage(byte[] modelBytes, byte[] imageBytes) {
        return webClient.post()
                .uri("/image/correction/align")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("model", modelBytes)
                        .with("image", imageBytes))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    /**
     * HSV 掩膜处理，返回处理后的图像
     */
    public byte[] hsvProcess(byte[] imageBytes, byte[] maskBytes) {
        return webClient.post()
                .uri("/hsv/process_image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("image", imageBytes)
                        .with("mask", maskBytes))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
