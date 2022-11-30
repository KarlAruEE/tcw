package com.karlaru.tcw.controllers;

import com.karlaru.tcw.response.models.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<String> notFound(NotFoundException notFoundException){
        return ResponseEntity
                .status(404)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(notFoundException.response());
    }
}
