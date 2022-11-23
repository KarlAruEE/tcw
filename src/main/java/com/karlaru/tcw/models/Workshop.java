package com.karlaru.tcw.models;

import lombok.Data;

import java.util.List;

@Data
public class Workshop {
    private final String name;
    private final String address;
    private final List<VehicleType> vehicles;
    private final String apiUrl;

    public enum VehicleType {
        CAR, TRUCK
    }
}
