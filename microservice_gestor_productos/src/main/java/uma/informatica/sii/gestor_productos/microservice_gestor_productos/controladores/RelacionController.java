package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.RelacionService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.RelacionMapper;

@RestController
@RequestMapping("/relacion")
public class RelacionController {
    private final RelacionService relacionService;

    public RelacionController(RelacionService relacionService, RelacionMapper relacionMapper) {
        this.relacionService = relacionService;
    }

    @GetMapping
    public ResponseEntity<?> getRelacion(
            @RequestParam(required = false) Integer idRelacion,
            @RequestParam(required = false) Integer idCuenta,
            @RequestHeader("Authorization") String authorizationHeader) {

        String jwtToken = authorizationHeader.replace("Bearer ", "");
        int count = 0;
        if (idRelacion != null) count++;
        if (idCuenta != null) count++;

        if (count != 1) {
            return ResponseEntity.badRequest().body("Debe proporcionar exactamente un parámetro de consulta.");
        }

        if (idRelacion != null) {
            return ResponseEntity.ok(relacionService.getRelacionPorId(idRelacion, jwtToken));
        }

        if (idCuenta != null) {
            return ResponseEntity.ok(relacionService.getRelacionesPorIdCuenta(idCuenta, jwtToken));
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping
    public ResponseEntity<RelacionDTO> crearRelacion(@RequestBody RelacionDTO relacionDTO,
    @RequestParam Integer idCuenta, @RequestHeader("Authorization") String authorizationHeader,UriComponentsBuilder builder) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        RelacionDTO relacion = relacionService.crearRelacion(relacionDTO,idCuenta,jwtToken);
        URI uri = builder
                .path(String.format("/%d", relacionDTO.getId()))
                .build()
                .toUri();
        return ResponseEntity.created(uri).body(relacion);
    }

    @PutMapping ("/{idRelacion}")
    public ResponseEntity<?> actualizarRelacion(
            @PathVariable Integer idRelacion,
            @RequestBody RelacionDTO relacionDTO,
            @RequestHeader("Authorization") String authorizationHeader) {
            String jwtToken = authorizationHeader.replace("Bearer ", "");
            RelacionDTO actualizado = relacionService.actualizarRelacion(idRelacion, relacionDTO, jwtToken);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{idRelacion}")
    public ResponseEntity<Void> eliminarRelacion(
        @PathVariable Integer idRelacion,
        @RequestHeader("Authorization") String authorizationHeader
        ) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");
        relacionService.eliminarRelacion(idRelacion, jwtToken);
        return ResponseEntity.noContent().build();
    }
}