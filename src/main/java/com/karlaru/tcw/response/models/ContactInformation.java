package com.karlaru.tcw.response.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "london.tireChangeBookingRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContactInformation {
    @XmlElement(name = "contactInformation")
    private String contactInformation;
}
