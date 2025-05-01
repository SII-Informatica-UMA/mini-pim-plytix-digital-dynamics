package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtUtil;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario;

import java.net.URI;
import java.util.Optional;

@Service
public class CuentaService {

    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    public CuentaService(RestTemplate restTemplate, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.jwtUtil = jwtUtil;
    }

    public Optional<CuentaDTO> getCuentaPorId(Long cuentaId) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", cuentaId)
                .build()
                .toUri();

        // Construir un usuario técnico para generar el token
        Usuario usuarioSistema = Usuario.builder()
                .id(-1L)
                .nombre("CuentaService")
                .role(Usuario.Rol.ADMINISTRADOR)
                .build();

        String jwtToken = jwtUtil.generateToken(usuarioSistema);

        RequestEntity<Void> request = RequestEntity
                .get(uri)
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        try {
            return Optional.of(restTemplate.exchange(request, CuentaDTO[].class).getBody()[0]);
        } catch (Exception e) {
            System.err.println("Error consultando la cuenta: " + e.getMessage());
        }

        return Optional.empty();
    }

    public boolean puedeCrearProducto(Long cuentaId, int productosActuales) {
        return getCuentaPorId(cuentaId)
                .map(cuenta -> productosActuales < cuenta.getPlan().getMaxProductos())
                .orElse(false);
    }
    public boolean puedeCrearRelacion(Long cuentaId, int relacionesActuales) {
        return getCuentaPorId(cuentaId)
                .map(cuenta -> relacionesActuales < cuenta.getPlan().getMaxRelaciones())
                .orElse(false);
    }
    public boolean puedeCrearCategoria(Long cuentaId, int categoriasActuales) {
        return getCuentaPorId(cuentaId)
                .map(cuenta -> categoriasActuales < cuenta.getPlan().getMaxCategoriasActivos())
                .orElse(false); 
    }
    
}