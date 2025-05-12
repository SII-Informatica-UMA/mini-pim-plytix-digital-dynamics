package uma.informatica.sii.gestor_productos.microservice_gestor_productos.ProductoTests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
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
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.PlanDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtRequestFilter;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true",
    }
    )
@DisplayName("Tests de Productos- ")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductoTest {
    
    @Value(value = "${local.server.port}")
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductoRepository productoRepo;
    
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
                    u.setRole(uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario.Rol.CLIENTE);
                    return java.util.Optional.of(u);
                }
                @Override
                public boolean usuarioPerteneceACuenta(Integer idCuenta, Long idUsuario, String jwt) {
                    return idCuenta == 1 || idCuenta == 3;
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
                public boolean puedeCrearProducto(Integer cuentaId, int actuales, UsuarioDTO u) {
                    return false;
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
                        // No parseamos nada, simplemente delegamos
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
        productoRepo.deleteAll();
        categoriaRepo.deleteAll();
    }
    
    // Helper para construir URIs
    private static URI endpoint(int port, String pathAndQuery) {
        return URI.create("http://localhost:" + port + pathAndQuery);
    }
    
    @Nested 
    @DisplayName("Cuando NO hay productos")
    public class SinProductos {
        private Categoria cat;
        private Producto prod;

        @Test @DisplayName("GET por idCategoria sin productos → 404")
        void getPorCategoriaSinProductos() {
            // Creamos una categoría para la cuenta
            Categoria c = new Categoria();
            c.setNombre("C1");
            c.setCuentaId(1);
            //categoriaRepo.save(c);

            Producto prod2;
            prod2 = new Producto();
            prod2.setGtin("GTIN-345");
            prod2.setSku("SKU-123");
            prod2.setNombre("ProdA");
            prod2.setCuentaId(4);
            prod2.getCategorias().add(c);
            prod2.setRelacionesOrigen(Collections.emptySet());
            prod2.setRelacionesDestino(Collections.emptySet());
            prod2.setAtributos(Collections.emptySet());
            productoRepo.save(prod2);

            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCategoria=" + c.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
        @Test @DisplayName("POST crearProducto → FORBIDDEN")
        void crearProducto() {
            cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(3);
            categoriaRepo.save(cat);

            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre(cat.getNombre());
            catDto.setId(cat.getId());
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());


            ResponseEntity<ProductoDTO> resp = restTemplate.exchange(
                RequestEntity.post(endpoint(port, "/producto?idCuenta=" + 3))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                ProductoDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }
}