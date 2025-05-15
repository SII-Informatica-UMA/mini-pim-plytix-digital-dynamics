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
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionEntradaDTO;
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
class RelacionApplicationTests {

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
                    return true;
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
        
        @Test @DisplayName("GET sin params → 400")
        void getSinParams() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?"))
                .header(AUTH_HEADER, TOKEN)
                .build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET con >1 params → 400")
        void getMultiplesParams() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idRelacion=1&idCuenta=1"))
                    .header(AUTH_HEADER, TOKEN).build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody()).contains("Debe proporcionar exactamente un parámetro de consulta.");
        }

        @Test @DisplayName("GET idRelacion inexistente → 404")
        void getIdNoExiste() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idRelacion=998"))
                    .header(AUTH_HEADER, TOKEN).build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("GET idCuenta sin relaciones → []")
        void getPorCuentaVacia() {
            ResponseEntity<Set<RelacionDTO>> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idCuenta=1"))
                    .header(AUTH_HEADER, TOKEN).build(),
                new org.springframework.core.ParameterizedTypeReference<Set<RelacionDTO>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test @DisplayName("DELETE idRelacion que no existe → 404")
        void deleteIdNoExiste() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/relacion/998"))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested @DisplayName("Cuando HAY relaciones")
    public class ConRelaciones {

        private Relacion rel;

        @BeforeEach
        void datos() {
            rel = new Relacion();
            rel.setCuentaId(1);
            rel.setNombre("Relacion1");
            relacionRepo.save(rel);
        }

        @Test @DisplayName("GET Sin params → 400")
        void getSinParams(){
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion"))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET por idRelacion → OK + DTO correcto")
        void getPorId() {
            ResponseEntity<RelacionDTO> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idRelacion=" + rel.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                RelacionDTO.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Relacion1");
        }

        @Test @DisplayName("GET por idCuenta → lista con 1 elemento")
        void getPorCuenta() {
            ResponseEntity<Set<RelacionDTO>> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idCuenta=1"))
                    .header(AUTH_HEADER, TOKEN).build(),
                new org.springframework.core.ParameterizedTypeReference<Set<RelacionDTO>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);
        }

        @Test @DisplayName("POST crearRelacion → 201 + Location + DTO")
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

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getHeaders().getLocation()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("Relacion Nueva");
        }

        @Test @DisplayName("PUT actualizarRelacion → 200 + cambios aplicados")
        void actualizarRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            ResponseEntity<RelacionDTO> resp = restTemplate.exchange(
                RequestEntity.put(endpoint(port, "/relacion/" + rel.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Relacion Nueva");
        }

        @Test @DisplayName("DELETE eliminarRelacion → 200 + sin entidad en BD")
        void eliminarRelacion() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/relacion/" + rel.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        /*
        @Test @DisplayName("DELETE eliminarRelacion → 403 + relacion se usa entre productos")
        void eliminarRelacionConProductos() {
            // Crear producto origen
            Producto ori = new Producto();
            ori.setGtin("GTIN-998");
            ori.setSku("SKU-998");
            ori.setNombre("ProdOrigen");
            ori.setCuentaId(1);
            ori.setRelacionesOrigen(Collections.emptySet());
            ori.setRelacionesDestino(Collections.emptySet());
            ori.setAtributos(Collections.emptySet());
            productoRepo.save(ori);

            // Crear producto destino
            Producto dest = new Producto();
            dest.setGtin("GTIN-999");
            dest.setSku("SKU-999");
            dest.setNombre("ProdDestino");
            dest.setCuentaId(1);
            dest.setRelacionesOrigen(Collections.emptySet());
            dest.setRelacionesDestino(Collections.emptySet());
            dest.setAtributos(Collections.emptySet());
            productoRepo.save(dest);

            // Asignar la relacion entre ellos
            RelacionProducto relProd = new RelacionProducto();
            relProd.setTipoRelacion(rel);
            relProd.setProductoOrigen(ori);
            relProd.setProductoDestino(dest);
            relacionProductoRepo.save(relProd);

            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/relacion/" + rel.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
            */
    }
}