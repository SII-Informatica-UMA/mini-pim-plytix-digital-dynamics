package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario;


import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtUtil;


@Service
public class UsuarioService {

    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;

    private Usuario usuarioAplicacion;
    private JwtUtil jwtUtil;

    private RestTemplate restTemplate;
    public UsuarioService(RestTemplate restTemplate, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        usuarioAplicacion = Usuario.builder()
            .id(-1L)
            .role(Usuario.Rol.ADMINISTRADOR)
            .nombre("Servicio de llamadas")
            .build();
        this.jwtUtil = jwtUtil;
    }

    public Optional<UsuarioDTO> getUsuario(Long id, String jwtToken) {
        var uri = UriComponentsBuilder.fromUriString(baseUrl+"/usuario")
            .queryParam("id", id)
            .build()
            .toUri();

        String appJwtToken = jwtUtil.generateToken(usuarioAplicacion);

        var peticion = RequestEntity.get(uri)
            .header("Authorization", "Bearer "+appJwtToken)
            .build();

        try {
            return Optional.of(restTemplate.exchange(peticion, UsuarioDTO[].class).getBody()[0]);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<UsuarioDTO> getUsuarioConectado(String jwtToken) {
        var peticion = RequestEntity.get(baseUrl + "/usuario")
                .header("Authorization", jwtToken)
                    .build();
        try {
            return Optional.of(restTemplate.exchange(peticion, UsuarioDTO[].class).getBody()[0]);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean usuarioPerteneceACuenta(Integer idCuenta, Long idUsuario, String jwtTokenDelUsuario) {
        String url = baseUrl + "/cuenta/" + idCuenta + "/usuarios";
        String tokenServicio = jwtUtil.generateToken(usuarioAplicacion);

        var peticion = RequestEntity.get(url)
            .header("Authorization", "Bearer " + tokenServicio)
            .build();

        try {
            var respuesta = restTemplate.exchange(peticion, UsuarioDTO[].class).getBody();
            return Arrays.stream(respuesta).anyMatch(u -> u.getId().equals(idUsuario));
        } catch (Exception e) {
            return false;
        }
    }
}

