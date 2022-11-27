package com.karlaru.tcw;

import com.karlaru.tcw.models.Workshop;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@SpringBootApplication
public class TcwApplication {

	public static void main(String[] args) {
		SpringApplication.run(TcwApplication.class, args);
	}

	@Bean
	public WebClient webClient(){
		return WebClient.builder().build();
	}

	@Bean
	public List<Workshop> workshop() {

		return List.of(
				new Workshop("Manchester", "14 Bury New Rd, Manchester",
						List.of(Workshop.VehicleType.CAR, Workshop.VehicleType.TRUCK)),
				new Workshop("London", "1A Gunton Rd, London",
						List.of(Workshop.VehicleType.CAR)));
	}
}
