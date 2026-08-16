package tn.econstruction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EConstructionApplication {
    public static void main(String[] args) {
        SpringApplication.run(EConstructionApplication.class, args);
    }
}
