package uma.informatica.sii.gestor_productos.microservice_gestor_productos.RelacionTests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.annotation.DirtiesContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.PlanDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtRequestFilter;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@DisplayName("Test de relaciones -")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RelacionTest {
@Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RelacionRepository relacionRepo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private RelacionProductoRepository relacionProductoRepo;

    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN = "Bearer token";

    @TestConfiguration
    static class StubsConfig {
        @Bean @Primary
        UsuarioService usuarioService() {
            return new UsuarioService(null, null) {
                @Override
                public java.util.Optional<UsuarioDTO> getUsuarioConectado(String jwt) {
                    UsuarioDTO u = new UsuarioDTO();
                    u.setId(1L);
                    u.setRole(uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario.Rol.CLIENTE);
                    return java.util.Optional.of(u);
                }
                @Override
                public boolean usuarioPerteneceACuenta(Integer idCuenta, Long idUsuario, String jwt) {
                    return true;
                }
                @Override
                public java.util.Optional<UsuarioDTO> getUsuario(Long id, String jwt) {
                    UsuarioDTO u = new UsuarioDTO();
                    u.setId(id);
                    u.setRole(uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario.Rol.CLIENTE);
                    return java.util.Optional.of(u);
                }
            };
        }

        @Bean @Primary
        CuentaService cuentaService() {
            return new CuentaService(null, null) {
                @Override
                public java.util.Optional<CuentaDTO> getCuentaPorId(Integer cuentaId) {
                    CuentaDTO c = new CuentaDTO();
                    c.setId(cuentaId);
                    PlanDTO plan = new PlanDTO();
                    plan.setMaxProductos(1000);
                    c.setPlan(plan);
                    return java.util.Optional.of(c);
                }
                @Override
                public boolean puedeCrearRelacion(Integer cuentaId, int actuales, UsuarioDTO u) {
                    return false;
                }
            };
        }

        @Bean @Primary
        public JwtRequestFilter jwtRequestFilter() {
            return new JwtRequestFilter() {
                @Override
                protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain chain
                ) throws ServletException, IOException {
                    chain.doFilter(request, response);
                }
            };
        }

        @Bean @Primary
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setup() {
        relacionRepo.deleteAll();
    }

    private static URI endpoint(int port, String pathAndQuery) {
        return URI.create("http://localhost:" + port + pathAndQuery);
    }

    @Nested 
    @DisplayName("Cuando NO hay relaciones")
    public class SinRelaciones {
        @Test @DisplayName("POST crearRelacion → FORBIDDEN")
        void crearRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            ResponseEntity<RelacionDTO> resp = restTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=1"))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }
}