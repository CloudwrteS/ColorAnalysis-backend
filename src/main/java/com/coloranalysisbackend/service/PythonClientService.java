package com.coloranalysisbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PythonClientService {
    private final WebClient webClient;

    public PythonClientService(WebClient.Builder builder, com.coloranalysisbackend.config.PythonProperties props) {
        this.webClient = builder.baseUrl(props.getUrl()).build();
    }

    /**
     * 将图片字节发送给 Python 服务取得处理后的结果
     */
    public byte[] callCanny(byte[] imageBytes) {
        return webClient.post()
                .uri("/canny")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(imageBytes)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
