package com.karlaru.tcw.controllers;

import com.karlaru.tcw.response.models.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<String> notFound(ApiException apiException){
        return ResponseEntity
                .status(404)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(apiException.response());
    }
}
