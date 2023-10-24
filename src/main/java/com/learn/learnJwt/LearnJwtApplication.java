package com.learn.learnJwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.SecureRandom;
import java.util.Base64;

@SpringBootApplication
public class LearnJwtApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnJwtApplication.class, args);
	}
}
