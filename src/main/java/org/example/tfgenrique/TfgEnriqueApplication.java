package org.example.tfgenrique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.example.tfgenrique", "controller"})
public class TfgEnriqueApplication {

    public static void main(String[] args) {
        SpringApplication.run(TfgEnriqueApplication.class, args);
    }

}
