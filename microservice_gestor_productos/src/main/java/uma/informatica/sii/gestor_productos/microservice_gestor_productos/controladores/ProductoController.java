package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import org.springframework.web.util.UriComponentsBuilder;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.ProductoService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.CredencialesNoValidas;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.SinPermisosSuficientes;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.ProductoMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/producto")
public class ProductoController {
    private final ProductoService productoService;
    private final ProductoMapper productoMapper;
    private final ProductoRepository productoRepository;
    public ProductoController(ProductoService productoService, ProductoMapper productoMapper, ProductoRepository productoRepository) {
        this.productoService = productoService;
        this.productoMapper = productoMapper;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public ResponseEntity<?> getProducto(
            @RequestParam(required = false) Integer idProducto,
            @RequestParam(required = false) String gtin,
            @RequestParam(required = false) Integer idCuenta,
            @RequestParam(required = false) Integer idCategoria,
            @RequestHeader("Authorization") String jwtToken) {

        int count = 0;
        if (idProducto != null) count++;
        if (gtin != null) count++;
        if (idCuenta != null) count++;
        if (idCategoria != null) count++;

        if (count != 1) {
            return ResponseEntity.badRequest().body("Debe proporcionar exactamente un parámetro de consulta.");
        }

        if (idProducto != null) {
            return ResponseEntity.ok(productoService.getProductoPorId(idProducto, jwtToken));
        }

        if (gtin != null) {
            return ResponseEntity.ok(productoService.getProductoPorGtin(gtin, jwtToken));
        }

        if (idCuenta != null) {
            return ResponseEntity.ok(productoService.getProductosPorIdCuenta(idCuenta, jwtToken));
        }

        if (idCategoria != null) {
            return ResponseEntity.ok(productoService.getProductosPorIdCategoria(idCategoria, jwtToken));
        }

        return ResponseEntity.badRequest().build();
    }


    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoDTO productoDTO,@RequestParam  Integer cuentaId, @RequestHeader("Authorization") String authorizationHeader,UriComponentsBuilder builder) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        ProductoDTO producto = productoMapper.toDTO(
            productoService.crearProducto(productoDTO,cuentaId,jwtToken)
        );
        URI uri = builder
                .path(String.format("/%d", productoDTO.getId()))
                .build()
                .toUri();
        return ResponseEntity.created(uri).body(producto);
    }

    @PutMapping ("/{idProducto}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Integer idProducto,
            @RequestBody ProductoDTO productoDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        ProductoDTO actualizado = productoService.actualizarProducto(idProducto, productoDTO, jwtToken);
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }
    
    @DeleteMapping("/{idProducto}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer idProducto, @RequestHeader("Authorization") String jwtToken) {
        productoService.eliminarProducto(idProducto, jwtToken);
        return ResponseEntity.noContent().build();
    }
}
