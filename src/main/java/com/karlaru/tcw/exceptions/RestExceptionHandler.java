package com.karlaru.tcw.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<String> badRequest(BadRequestException badRequestException){
        return ResponseEntity
                .status(400)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(badRequestException.response());
    }
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<String> notFound(NotFoundException notFoundException){
        return ResponseEntity
                .status(404)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(notFoundException.response());
    }
    @ExceptionHandler(ErrorException.class)
    ResponseEntity<String> error(ErrorException errorException){
        return ResponseEntity
                .status(500)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(errorException.response());
    }
}
