package com.karlaru.tcw.workshops;

import java.util.List;


public record Workshop(String name, String address, List<VehicleType> vehicles) {
    public enum VehicleType {
        Car, Truck;
    }
}
