package com.karlaru.tcw.response.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ErrorResponse {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;

}

