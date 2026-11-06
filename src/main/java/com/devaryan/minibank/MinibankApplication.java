package com.devaryan.minibank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MinibankApplication {

	public static void main(String[] args) {
		// Run the app in UTC so timestamps are consistent and PostgreSQL accepts the time zone
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(MinibankApplication.class, args);
	}

}
