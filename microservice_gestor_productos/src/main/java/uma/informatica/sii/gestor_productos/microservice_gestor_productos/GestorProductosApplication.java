package uma.informatica.sii.gestor_productos.microservice_gestor_productos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class GestorProductosApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(GestorProductosApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        File ddlFile = new File("src/main/resources/ddl-schema.sql");
        if (ddlFile.exists()) {
            ddlFile.delete();  // Elimina el archivo existente
            ddlFile.createNewFile();  // Crea un archivo vacío
        }
        System.out.println("Microservicio iniciado y archivo DDL regenerado.");
    }
}