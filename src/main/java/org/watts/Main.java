package org.watts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // Inicia el servidor en el puerto 8080, que es el que está por defecto
        SpringApplication.run(Main.class, args);
        System.out.println("APP INICIADA CORRECTAMENTE");
    }
}
