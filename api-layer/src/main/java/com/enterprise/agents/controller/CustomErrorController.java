package com.enterprise.agents.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");

        body.put("status", statusCode);
        body.put("error", exception != null ? exception.getMessage() : "Unknown error");
        body.put("path", request.getAttribute("jakarta.servlet.error.request_uri"));

        return ResponseEntity.status(statusCode != null ? statusCode : 500).body(body);
    }
} 