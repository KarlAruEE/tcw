package com.karlaru.tcw;

import com.karlaru.tcw.models.Workshop;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WorkshopConfig {

    @Value("#{${workshops}}")
    List<List<String>> workshops;

    @Bean
    public List<Workshop> getWorkshops(){
        return workshops.stream()
                .map(workshop -> {
                    String name = workshop.get(0);
                    String address = workshop.get(1);
                    List<Workshop.VehicleType> vehicleTypes = Arrays.stream(workshop.get(2).split(","))
                                                                    .map(Workshop.VehicleType::valueOf).toList();
                    String apiUrl = workshop.get(3);

                    return new Workshop(name, address, vehicleTypes, apiUrl);
                })
                .collect(Collectors.toList());
    }

}
