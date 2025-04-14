package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.ProductoService;    
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }
    @GetMapping
    public List<ProductoDTO> obtenerTodos(){
        return productoService.obtenerTodos().stream()
        .map(ProductoMapper::toDTO)
        .collect(Collectors.toList());
    }
    
}
