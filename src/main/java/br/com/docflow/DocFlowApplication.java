package br.com.docflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocFlowApplication.class, args);
	}

}
