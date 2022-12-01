package com.karlaru.tcw.response.models;

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
public class ApiException extends Throwable{
    @XmlElement(name = "statusCode")
    private int code;
    @XmlElement(name = "error")
    private String message;

    public String response(){
        return "{\"code\":\""+code+"\"," +
                "\"message\":\""+message+"\"}";
    }

}

