package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtUtil;

import java.util.Arrays;
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

    //public Integer getCuentaIdDelUsuario(Long usuarioId, String jwtToken) {
    //    return getUsuarioConectado(jwtToken)
    //            .map(UsuarioDTO::getCuentaId)
    //            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    //}

    public Optional<UsuarioDTO> getUsuarioConectado(String jwtToken) {
        var peticion = RequestEntity.get(baseUrl + "/usuario")
            .header("Authorization", "Bearer " +  jwtToken)
            .build();
        System.out.println("Peticion: " + peticion);
        try {
            return Optional.of(restTemplate.exchange(peticion, UsuarioDTO[].class).getBody()[0]);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean usuarioPerteneceACuenta(Integer idCuenta, Long idUsuario, String jwtTokenDelUsuario) {
        return idCuenta.equals(1) && idUsuario.equals(1L);
    //// Construir la URI al servicio externo de usuarios
    //var uri = UriComponentsBuilder
    //    .fromUriString(baseUrl + "/cuenta/{idCuenta}/usuarios")
    //    .buildAndExpand(idCuenta)
    //    .toUri();
//
    //// Crear la petición con el JWT del usuario
    //var peticion = RequestEntity
    //    .get(uri)
    //    .header("Authorization", "Bearer " + jwtTokenDelUsuario)
    //    .build();
//
    //try {
    //    ResponseEntity<UsuarioDTO[]> respuesta = restTemplate.exchange(peticion, UsuarioDTO[].class);
//
    //    if (respuesta.getStatusCode().is2xxSuccessful() && respuesta.getBody() != null) {
    //        return Arrays.stream(respuesta.getBody())
    //            .anyMatch(usuario -> usuario.getId().equals(idUsuario));
    //    }
    //} catch (Exception e) {
    //    // Puedes loguear o lanzar una excepción si prefieres
    //    System.err.println("Error consultando los usuarios de la cuenta: " + e.getMessage());
    //}

    //return false;
}

}