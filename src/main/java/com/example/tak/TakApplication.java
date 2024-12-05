package com.example.tak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TakApplication {

	public static void main(String[] args) {
		System.getenv().forEach((key, value) -> {
			System.out.println(key + " = " + value);
		});

		SpringApplication.run(TakApplication.class, args);
	}

}
