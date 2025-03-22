package uma.informatica.sii.gestor_productos.microservice_gestor_productos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;

@SpringBootApplication
public class MicroserviceGestorProductosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceGestorProductosApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
		return (args) -> {
			// Crear y guardar algunas categorías y productos
			Categoria categoria1 = new Categoria();
			categoria1.setNombre("Electrónica");
			categoriaRepository.save(categoria1);

			Producto producto1 = new Producto();
			producto1.setNombre("Teléfono");
			producto1.setPrecio(299.99);
			producto1.setCategoria(categoria1);
			productoRepository.save(producto1);

			// Más lógica de inicialización si es necesario
		};
	}

}