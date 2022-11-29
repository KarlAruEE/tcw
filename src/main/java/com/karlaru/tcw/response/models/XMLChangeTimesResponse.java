package com.karlaru.tcw.response.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "tireChangeTimesResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLChangeTimesResponse {

    @XmlElement(name = "availableTime")
    List<XMLAvailableTime> availableTime;

}
