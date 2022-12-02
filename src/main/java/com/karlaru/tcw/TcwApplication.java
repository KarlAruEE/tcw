package com.karlaru.tcw;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@OpenAPIDefinition(info = @Info(title = "Tire Workshop API",
		version = "0.1",
		description = "Integrate many workshops under to one booking system",
		contact = @Contact(name = "Karl Aru", email = "kiri@karlaru.ee")))
@SpringBootApplication
public class TcwApplication {

	public static void main(String[] args) {
		SpringApplication.run(TcwApplication.class, args);
	}

	@Bean
	public WebClient webClient(){
		return WebClient.builder().build();
	}
}
