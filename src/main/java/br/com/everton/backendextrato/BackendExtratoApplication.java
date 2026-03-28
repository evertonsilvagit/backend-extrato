package br.com.everton.backendextrato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendExtratoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendExtratoApplication.class, args);
    }

}
