package com.karlaru.tcw.exceptions;

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
public class NotFoundException extends Throwable{
    @XmlElement(name = "statusCode")
    private int code;
    @XmlElement(name = "error")
    private String message;

    public String response(){
        return "{\"code\":\""+code+"\"," +
                "\"message\":\""+message+"\"}";
    }

}

