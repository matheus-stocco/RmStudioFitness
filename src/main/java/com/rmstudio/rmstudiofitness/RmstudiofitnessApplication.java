package com.rmstudio.rmstudiofitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;

@SpringBootApplication
public class RmstudiofitnessApplication {

	public static void main(String[] args) {
		SpringApplication.run(RmstudiofitnessApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// Módulo para lidar com entidades do Hibernate (serializando lazy como null).
		mapper.registerModule(new Hibernate5JakartaModule());
		// Módulo para lidar com tipos de data e hora do Java 8 (LocalDate, LocalDateTime)
		mapper.registerModule(new JavaTimeModule());
        // Configura o mapper para não falhar se encontrar propriedades desconhecidas no JSON.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}
}
