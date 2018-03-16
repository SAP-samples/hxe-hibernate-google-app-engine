package com.sap.hana.hibernate.sample.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = { "com.sap.hana.hibernate.sample.app", "com.sap.hana.hibernate.sample.controllers",
		"com.sap.hana.hibernate.sample.repositories", "com.sap.hana.hibernate.sample.web" })
@EntityScan(basePackages = { "com.sap.hana.hibernate.sample.entities" })
public class Application {

	public static void main(String[] args) {
		SpringApplication.run( Application.class, args );
	}
}
