package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam Long userId, @RequestParam String role) {
        Usuario usuario = Usuario.builder()
                .id(userId)
                .role(Usuario.Rol.valueOf(role.toUpperCase()))
                .nombre("Usuario de prueba")
                .build();

        String token = jwtUtil.generateToken(usuario);
        return ResponseEntity.ok(token);
    }
}
