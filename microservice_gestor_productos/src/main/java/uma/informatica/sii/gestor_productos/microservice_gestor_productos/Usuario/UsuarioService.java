package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        return Optional.empty();
    }

    public Integer getCuentaIdDelUsuario(Long usuarioId, String jwtToken) {
        return getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getCuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    public Optional<UsuarioDTO> getUsuarioConectado(String jwtToken) {
        UsuarioDTO mock = new UsuarioDTO();
        mock.setId(1L); // Must match the JWT you use
        mock.setCuentaId(1); // Must match Categoria.cuentaId
        mock.setNombre("Admin Local");
        mock.setRole(Usuario.Rol.ADMINISTRADOR);
        return Optional.of(mock);
    }

    public boolean usuarioPerteneceACuenta(Integer idCuenta, Long idUsuario, String jwtTokenDelUsuario) {
        // Local development: simulate the user belongs to the account
        return idCuenta.equals(1) && idUsuario.equals(1L);
    }
}