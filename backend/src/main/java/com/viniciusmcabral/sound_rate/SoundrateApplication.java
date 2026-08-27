package com.viniciusmcabral.sound_rate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SoundrateApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoundrateApplication.class, args);
	}
}
