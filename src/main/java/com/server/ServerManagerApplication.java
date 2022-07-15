package com.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.server.enumeration.Status;
import com.server.model.Server;
import com.server.repository.ServerRepository;

@SpringBootApplication
public class ServerManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerManagerApplication.class, args);
	}

	@Bean
	CommandLineRunner run(ServerRepository serverRepository) {
		return args -> {
			serverRepository.save(new Server(null, "192.168.1.39", "EndeavourOS", "16 GB", "Personal PC",
					"http://localhost:8080/server/server1.png", Status.SERVER_UP));
		};
	}
}
