package com.projet.app;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.projet")
@EntityScan(basePackages = "com.projet.user.domain")
@EnableJpaRepositories(basePackages = "com.projet.user.repository")
public class ProjetApplication {

    public static void main(String[] args) {
        Dotenv.configure()
                .directory("./")
                .filename(".env")
                .ignoreIfMissing()
                .load()
                .entries()
                .forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(ProjetApplication.class, args);
    }
}
