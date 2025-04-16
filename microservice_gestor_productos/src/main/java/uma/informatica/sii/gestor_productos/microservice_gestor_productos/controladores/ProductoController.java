package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.ProductoService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.CredencialesNoValidas;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.SinPermisosSuficientes;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.ProductoMapper;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;




@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productoService;
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoDTO> obtenerTodosLosProductos(Integer idProducto, Integer idCuenta, Integer idCategoria, String gtin) {
        List<Producto> productos = productoService.buscarProductos(idProducto, idCuenta, idCategoria, gtin);
        return productos.stream()
                .map(p -> new ProductoDTO())
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoDTO productoDTO, UriComponentsBuilder builder) {
        productoDTO.setId(null);
        ProductoDTO producto = ProductoMapper.toDTO(
            productoService.crearProducto(
                ProductoMapper.toEntity(productoDTO), productoDTO.getCuentaId()
            )
        );
        URI uri = builder
                .path(String.format("/%d", productoDTO.getId()))
                .build()
                .toUri();
        return ResponseEntity.created(uri).body(producto);
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductoDTO> modificarProducto(@PathVariable Integer id, @RequestBody ProductoDTO producto) {
        producto.setId(id);
        Producto productoModificado = productoService.modificarProducto(ProductoMapper.toEntity(producto));
        return ResponseEntity.ok(ProductoMapper.toDTO(productoModificado));
    }
    
    @DeleteMapping("/{idProducto}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer idProducto) {
        productoService.eliminarProducto(idProducto);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntidadNoExistente.class)
    public ResponseEntity<String> handleEntidadNoExistente(EntidadNoExistente ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SinPermisosSuficientes.class)
    public ResponseEntity<String> handleSinPermisosSuficientes(SinPermisosSuficientes ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(CredencialesNoValidas.class)
    public ResponseEntity<String> handleCredencialesNoValidas(CredencialesNoValidas ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
