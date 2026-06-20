package br.com.example.sdd.customers;

import org.springframework.boot.SpringApplication;

public class TestCustomersApplication {

	public static void main(String[] args) {
		SpringApplication.from(CustomersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
