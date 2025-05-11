package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import org.springframework.web.util.UriComponentsBuilder;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.ProductoService;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/producto")
public class ProductoController {
    private final ProductoService productoService;
    
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<?> getProducto(
            @RequestParam(required = false) Integer idProducto,
            @RequestParam(required = false) Integer idCuenta,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) String gtin,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        int count = 0;
        if (idProducto != null) count++;
        if (gtin != null) count++;
        if (idCuenta != null) count++;
        if (idCategoria != null) count++;

        if (count > 1) {
            return ResponseEntity.badRequest().body("Debe proporcionar exactamente un parámetro de consulta.");
        }
        if (idProducto != null) {
            return ResponseEntity.ok(productoService.getProductoPorId(idProducto, jwtToken));
        }
        if (idCuenta != null) {
            return ResponseEntity.ok(productoService.getProductosPorIdCuenta(idCuenta, jwtToken));
        }
        if (idCategoria != null) {
            return ResponseEntity.ok(productoService.getProductosPorIdCategoria(idCategoria, jwtToken));
        }
        if (gtin != null) {
            return ResponseEntity.ok(productoService.getProductoPorGtin(gtin, jwtToken));
        }
        return ResponseEntity.badRequest().build();
    }


    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoEntradaDTO productoDTO,@RequestParam  Integer idCuenta, 
            @RequestHeader("Authorization") String authorizationHeader,UriComponentsBuilder builder) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        ProductoDTO producto = productoService.crearProducto(productoDTO,idCuenta,jwtToken);
        URI uri = builder
                .path(String.format("/%d", producto.getId()))
                .build()
                .toUri();
        return ResponseEntity.created(uri).body(producto);
    }

    @PutMapping ("/{idProducto}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Integer idProducto,
            @RequestBody ProductoEntradaDTO productoDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        ProductoDTO actualizado = productoService.actualizarProducto(idProducto, productoDTO, jwtToken);
        return ResponseEntity.ok(actualizado);
    }
    
    @DeleteMapping("/{idProducto}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer idProducto, @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        productoService.eliminarProducto(idProducto, jwtToken);
        return ResponseEntity.ok().build();
    }
}
