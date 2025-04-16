package uma.informatica.sii.gestor_productos.microservice_gestor_productos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;

@SpringBootApplication
public class GestorProductosApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(GestorProductosApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {

    }
}