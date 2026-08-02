package com.codingshuttle.razorpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableScheduling
public class
RazorpayApplication {

	public static void main(String[] args) {
		System.out.println("******** RAZORPAY APPLICATION STARTED ********");

		SpringApplication.run(RazorpayApplication.class, args);
	}

}
