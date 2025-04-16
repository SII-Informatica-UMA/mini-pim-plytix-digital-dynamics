package uma.informatica.sii.gestor_productos.microservice_gestor_productos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@SpringBootApplication
public class GestorProductosApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(GestorProductosApplication.class, args);
    }

        @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
    
    @Override
    public void run(String... args) throws IOException {

    }
}