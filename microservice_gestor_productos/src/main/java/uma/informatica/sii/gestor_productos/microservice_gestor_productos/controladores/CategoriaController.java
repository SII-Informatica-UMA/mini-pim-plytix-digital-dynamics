package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/categoria-producto")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;

    }

    @GetMapping
    public ResponseEntity<?> getCategoria(
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idCuenta,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        int count = 0;
        if (idCategoria != null) count++;
        if (idCuenta != null) count++;

        if (count != 1) {
            return ResponseEntity.badRequest().body("Debe proporcionar exactamente un parámetro: idCategoria o idCuenta.");
        }

        if (idCategoria != null) {
            return ResponseEntity.ok(categoriaService.getCategoriaById(idCategoria, jwtToken));
        }

        return ResponseEntity.ok(categoriaService.getCategoriasByidCuenta(idCuenta, jwtToken));
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crearCategoria(
            @RequestBody CategoriaEntradaDTO dto,
            @RequestParam Integer idCuenta,
            @RequestHeader("Authorization") String authorizationHeader,
            UriComponentsBuilder builder) {
        
        String jwtToken = authorizationHeader.replace("Bearer ", "");

        CategoriaDTO nueva = categoriaService.crearCategoria(dto, idCuenta, jwtToken);
        URI uri = builder.path("/{id}").buildAndExpand(nueva.getId()).toUri();
        return ResponseEntity.created(uri).body(nueva);
    }

    @PutMapping("/{idCategoria}")
    public ResponseEntity<CategoriaDTO> modificarCategoria(
            @PathVariable Integer idCategoria,
            @RequestBody CategoriaEntradaDTO dto,
            @RequestHeader("Authorization") String authorizationHeader) {

        String jwtToken = authorizationHeader.replace("Bearer ", "");
        
        return ResponseEntity.ok(categoriaService.modificarCategoria(idCategoria, dto, jwtToken));
    }

    @DeleteMapping("/{idCategoria}")
    public ResponseEntity<Void> eliminarCategoria(
            @PathVariable Integer idCategoria,
            @RequestHeader("Authorization") String authorizationHeader) {

        String jwtToken = authorizationHeader.replace("Bearer ", "");

        categoriaService.eliminarCategoria(idCategoria, jwtToken);
        return ResponseEntity.ok().build();
    }
}
