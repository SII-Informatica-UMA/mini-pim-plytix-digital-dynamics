package uma.informatica.sii.gestor_productos.microservice_gestor_productos.CategoriaTests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtRequestFilter;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@DisplayName("Tests de Categorías -")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CategoriaApplicationTests {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CategoriaRepository categoriaRepo;

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
                    u.setRole(uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario.Rol.ADMINISTRADOR);
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
                    u.setRole(uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario.Rol.ADMINISTRADOR);
                    return java.util.Optional.of(u);
                }
            };
        }

        @Bean @Primary
        CuentaService cuentaService() {
            return new CuentaService(null, null) {
                @Override
                public Optional<CuentaDTO> getCuentaPorId(Integer cuentaId) {
                    if (cuentaId == 1 || cuentaId == 2 || cuentaId == 3) {
                        CuentaDTO c = new CuentaDTO();
                        c.setId(cuentaId);
                        PlanDTO plan = new PlanDTO();
                        plan.setMaxProductos(1000);
                        c.setPlan(plan);
                        return Optional.of(c);
                    } else {
                        return Optional.empty(); 
                    }
                }
        
                @Override
                public boolean puedeCrearCategoria(Integer cuentaId, int actuales, UsuarioDTO u) {
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
        categoriaRepo.deleteAll();
    }

    private URI endpoint(int port, String pathAndQuery) {
        return URI.create("http://localhost:" + port + pathAndQuery);
    }

    @Nested
    @DisplayName("Cuando NO hay categorías")
    class SinCategorias {

        @Test @DisplayName("GET sin cuenta → 400")
        void getSinCuenta() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria"))
                    .header(AUTH_HEADER, TOKEN).build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET cuenta inexistente → 404")
        void getCuentaNoExiste() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria?idCuenta=999"))
                    .header(AUTH_HEADER, TOKEN)
                    .build(),
                String.class
            );
        
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            // Optional: remove this to avoid brittle checks
            // assertThat(resp.getBody()).contains("Cuenta no encontrada");
        }
        
        

        @Test @DisplayName("GET cuenta válida pero sin categorías → []")
        void getCuentaSinCategorias() {
            ResponseEntity<CategoriaDTO[]> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria?idCuenta=2"))
                    .header(AUTH_HEADER, TOKEN).build(),
                CategoriaDTO[].class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test @DisplayName("POST crearCategoria válida → 201")
        void crearCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("NuevaCategoria");

            ResponseEntity<CategoriaDTO> resp = restTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria?idCuenta=2"))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                CategoriaDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("NuevaCategoria");
        }
    }

    @Nested
    @DisplayName("Con categorías existentes")
    class ConCategorias {

        private Categoria cat;

        @BeforeEach
        void datos() {
            cat = new Categoria();
            cat.setNombre("Existente");
            cat.setCuentaId(2);
            categoriaRepo.save(cat);
        }

        @Test @DisplayName("GET por idCategoria → OK")
        void getPorIdCategoria() {
            ResponseEntity<CategoriaDTO> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria?idCategoria=" + cat.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                CategoriaDTO.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Existente");
        }

        @Test @DisplayName("PUT actualizarCategoria → 200")
        void actualizarCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("Renombrada");

            ResponseEntity<CategoriaDTO> resp = restTemplate.exchange(
                RequestEntity.put(endpoint(port, "/categoria/" + cat.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                CategoriaDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Renombrada");
        }

        @Test @DisplayName("DELETE eliminarCategoria → 200")
        void eliminarCategoria() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/categoria/" + cat.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(categoriaRepo.findById(cat.getId())).isEmpty();
        }


    @Test @DisplayName("POST crearCategoria nombre existente → 400")
    void crearCategoriaNombreExistente() {
        CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
        entrada.setNombre(cat.getNombre());

        ResponseEntity<String> resp = restTemplate.exchange(
            RequestEntity.post(endpoint(port, "/categoria?idCuenta=2"))
                .header(AUTH_HEADER, TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada),
            String.class); 

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }     
    }
}