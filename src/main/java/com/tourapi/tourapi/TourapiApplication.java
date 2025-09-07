package com.tourapi.tourapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TourapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TourapiApplication.class, args);
	}

}
