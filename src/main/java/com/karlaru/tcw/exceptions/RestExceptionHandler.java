package com.karlaru.tcw.exceptions;

import com.karlaru.tcw.response.models.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})})
    ResponseEntity<ErrorResponse> badRequest(BadRequestException badRequestException){

        return ResponseEntity
                .status(400)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(badRequestException.getExceptionData());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})})
    ResponseEntity<ErrorResponse> notFound(NotFoundException notFoundException){
        return ResponseEntity
                .status(404)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(notFoundException.getErrorResponse());
    }

    @ExceptionHandler(ErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponses(value = {
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                                 content = { @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = ErrorResponse.class))})})
    ResponseEntity<ErrorResponse> error(ErrorException errorException){
        return ResponseEntity
                .status(500)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(errorException.getErrorResponse());
    }
}
