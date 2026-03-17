package com.example.wustchat.service;

import cn.wustlinghang.mywust.common.core.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;

@Service
public class AuthProxyService {
    private final RestTemplate restTemplate;
    private final String basicAuthBaseUrl;
    private final ObjectMapper objectMapper;

    public AuthProxyService(RestTemplateBuilder restTemplateBuilder,
                            ObjectMapper objectMapper,
                            @Value("${basic.auth.base-url}") String basicAuthBaseUrl,
                            @Value("${basic.auth.connect-timeout-ms:3000}") long connectTimeoutMs,
                            @Value("${basic.auth.read-timeout-ms:5000}") long readTimeoutMs) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
        this.objectMapper = objectMapper;
        this.basicAuthBaseUrl = basicAuthBaseUrl;
    }

    public ResponseEntity<String> login(JsonNode requestBody) {
        String targetUrl = UriComponentsBuilder.fromHttpUrl(basicAuthBaseUrl)
                .path("/auth/login")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        HttpEntity<byte[]> entity = new HttpEntity<>(toJsonBytes(requestBody), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, HttpMethod.POST, entity, String.class);
            return buildProxyResponse(response.getStatusCode(), response.getHeaders(), response.getBody());
        } catch (HttpStatusCodeException ex) {
            return buildProxyResponse(ex.getStatusCode(), ex.getResponseHeaders(), ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            HttpStatusCode status = isTimeout(ex) ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY;
            String message = isTimeout(ex) ? "基础认证服务请求超时" : "基础认证服务不可用";
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJson(Result.fail(status.value(), message)));
        } catch (RestClientException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJson(Result.fail(HttpStatus.BAD_GATEWAY.value(), "基础认证服务响应异常")));
        }
    }

    private ResponseEntity<String> buildProxyResponse(HttpStatusCode status, HttpHeaders headers, String body) {
        return ResponseEntity.status(status)
                .contentType(resolveContentType(headers))
                .body(body);
    }

    private MediaType resolveContentType(HttpHeaders headers) {
        if (headers == null || headers.getContentType() == null) {
            return MediaType.APPLICATION_JSON;
        }
        return headers.getContentType();
    }

    private byte[] toJsonBytes(JsonNode requestBody) {
        try {
            return objectMapper.writeValueAsBytes(requestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("登录请求体格式不正确", e);
        }
    }

    private String toJson(Result<?> result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", result.getCode(), result.getMsg());
        }
    }

    private boolean isTimeout(ResourceAccessException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof SocketTimeoutException || cause instanceof ClosedChannelException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
