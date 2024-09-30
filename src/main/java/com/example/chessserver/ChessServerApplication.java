package com.example.chessserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.chessserver.repository")
public class ChessServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessServerApplication.class, args);
    }

}
