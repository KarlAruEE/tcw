package com.karlaru.tcw.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.karlaru.tcw.response.models.ErrorResponse;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@NoArgsConstructor
@XmlRootElement(name = "errorResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class NotFoundException extends Throwable{
    @XmlElement(name = "statusCode")
    @JsonProperty("code")
    private int code;
    @XmlElement(name = "error")
    @JsonProperty("message")
    private String message;

    public NotFoundException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    public ErrorResponse getExceptionData(){
        return new ErrorResponse(this.code, this.message);
    }

}

