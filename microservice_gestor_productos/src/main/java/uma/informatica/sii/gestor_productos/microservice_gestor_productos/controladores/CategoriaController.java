package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.CategoriaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.SinPermisosSuficientes;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.CredencialesNoValidas;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<?> getCategoria(
            @RequestParam Integer idCategoria,
            @RequestParam Integer cuentaId,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        return ResponseEntity.ok(categoriaService.getCategoriaByIdAndCuenta(idCategoria, cuentaId, jwtToken));
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crearCategoria(
            @RequestBody CategoriaDTO dto,
            @RequestParam Integer idCuenta,
            @RequestHeader("Authorization") String authorizationHeader,
            UriComponentsBuilder builder) {
        
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        CategoriaDTO nueva = categoriaService.crearCategoria(dto, idCuenta,jwtToken);
        URI uri = builder.path("/categoria/{id}").buildAndExpand(nueva.getId()).toUri();
        return ResponseEntity.created(uri).body(nueva);
    }

    @PutMapping("/{idCategoria}")
    public ResponseEntity<CategoriaDTO> modificarCategoria(
            @PathVariable Integer idCategoria,
            @RequestBody CategoriaDTO dto,
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
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntidadNoExistente.class)
    public ResponseEntity<String> handleEntidadNoExistente(EntidadNoExistente ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SinPermisosSuficientes.class)
    public ResponseEntity<String> handleSinPermisosSuficientes(SinPermisosSuficientes ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos suficientes.");
    }

    @ExceptionHandler(CredencialesNoValidas.class)
    public ResponseEntity<String> handleCredencialesNoValidas(CredencialesNoValidas ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales no válidas.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}