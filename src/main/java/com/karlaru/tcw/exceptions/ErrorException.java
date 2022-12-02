package com.karlaru.tcw.exceptions;

import com.karlaru.tcw.response.models.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "errorResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorException extends Throwable{
    @XmlElement(name = "statusCode")
    private int code;
    @XmlElement(name = "error")
    private String message;

    private ErrorResponse errorResponse;

    public ErrorException(int code, String message) {
        this.errorResponse = new ErrorResponse(code, message);
    }
    public ErrorResponse getErrorResponse(){
        return this.errorResponse;
    }

}

