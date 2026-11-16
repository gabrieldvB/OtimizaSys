package br.com.otimizasistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OtimizaSistemaApplication {
    public static void main(String[] args) {
        SpringApplication.run(OtimizaSistemaApplication.class, args);
    }
}
