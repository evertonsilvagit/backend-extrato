package br.com.everton.backendextrato;

import org.springframework.boot.SpringApplication;

public class TestBackendExtratoApplication {

    public static void main(String[] args) {
        SpringApplication.from(BackendExtratoApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
