package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }
}
