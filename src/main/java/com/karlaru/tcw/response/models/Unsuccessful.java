package com.karlaru.tcw.response.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "errorResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class Unsuccessful{
    @XmlElement(name = "statusCode")
    private HttpStatus code;
    @XmlElement(name = "error")
    private String message;
}

