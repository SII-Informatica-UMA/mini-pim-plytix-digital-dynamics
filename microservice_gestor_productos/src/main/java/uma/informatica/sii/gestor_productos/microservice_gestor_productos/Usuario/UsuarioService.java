package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtUtil;
import java.util.Optional;

@Service
public class UsuarioService {

    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;

    private final Usuario usuarioAplicacion;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    public UsuarioService(RestTemplate restTemplate, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.jwtUtil = jwtUtil;
        this.usuarioAplicacion = Usuario.builder()
            .id(-1L)
            .role(Usuario.Rol.ADMINISTRADOR)
            .nombre("Servicio de llamadas")
            .build();
    }

    public Optional<UsuarioDTO> getUsuario(Long id, String jwtToken) {
        var uri = UriComponentsBuilder.fromUriString(baseUrl+"/usuario")
            .queryParam("id", id)
            .build()
            .toUri();

        String appJwtToken = jwtUtil.generateToken(usuarioAplicacion);

        var peticion = RequestEntity.get(uri)
            .header("Authorization", "Bearer " + appJwtToken)
            .build();

        try {
            return Optional.of(restTemplate.exchange(peticion, UsuarioDTO[].class).getBody()[0]);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<UsuarioDTO> getUsuarioConectado(String jwtToken) {
        var peticion = RequestEntity.get(baseUrl + "/usuario")
            .header("Authorization", "Bearer " +  jwtToken)
            .build();
        try {
            return Optional.of(restTemplate.exchange(peticion, UsuarioDTO[].class).getBody()[0]);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean usuarioPerteneceACuenta(Integer idCuenta, Long idUsuario, String jwtTokenDelUsuario) {
        // Obtenemos los datos del usuario usando el token del sistema
        Optional<UsuarioDTO> usuario = getUsuario(idUsuario, jwtTokenDelUsuario);
        // Si no se encuentra el usuario, no pertenece
        if (usuario.isEmpty()) {
            return false;
        }
        // Si el usuario es administrador, siempre pertenece
        if (usuario.get().getRole().equals(Usuario.Rol.ADMINISTRADOR)) {
            return true;
        }
        // Usamos el token del sistema para hacer la consulta interna a la cuenta
        String appJwtToken = jwtUtil.generateToken(usuarioAplicacion);
        var peticion = RequestEntity.get(baseUrl + "/cuenta/" + idCuenta + "/usuarios")
            .header("Authorization", "Bearer " + appJwtToken)
            .build();
        try {
            ResponseEntity<UsuarioDTO[]> respuesta = restTemplate.exchange(peticion, UsuarioDTO[].class);
            UsuarioDTO[] lista = respuesta.getBody();
    
            for (UsuarioDTO usu : lista) {
                if (usu.getId().equals(idUsuario)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            // Puedes registrar el error si lo necesitas
            return false;
        }
    }
}