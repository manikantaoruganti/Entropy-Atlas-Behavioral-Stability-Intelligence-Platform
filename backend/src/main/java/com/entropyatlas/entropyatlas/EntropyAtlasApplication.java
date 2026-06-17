package com.entropyatlas.entropyatlas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EntropyAtlasApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntropyAtlasApplication.class, args);
    }

}
