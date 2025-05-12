package uma.informatica.sii.gestor_productos.microservice_gestor_productos.ProductoTests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
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
import org.springframework.data.annotation.Transient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.PlanDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtRequestFilter;


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true",
    }
    )
@DisplayName("Tests de Productos con Usuario NO Pertenece a Cuenta - ")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductoApplicationUserTests {

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
                    return false;
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
                    return true;
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
    @DisplayName("Hay productos")
    public class usuarioNoPerteneceACuenta {
        private Categoria cat;
        private Producto prod;

        @BeforeEach
        void datos() {
            cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(2);
            // categoriaRepo.save(cat);
            // cat = categoriaRepo.findById(cat.getId()).get();

            prod = new Producto();
            prod.setGtin("GTIN-123");
            prod.setSku("SKU-123");
            prod.setNombre("ProdA");
            prod.setCuentaId(2);
            prod.getCategorias().add(cat);
            prod.setRelacionesOrigen(Collections.emptySet());
            prod.setRelacionesDestino(Collections.emptySet());
            prod.setAtributos(Collections.emptySet());
            productoRepo.save(prod);
        }

        @Test @DisplayName("GET por idProducto → FORBIDDEN")
        void getPorId() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idProducto=" + prod.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("GET por idCategoria → FORBIDDEN")
        void getPorCategoria() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCategoria=" + cat.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("GET por idCuenta → FORBIDDEN")
        void getPorCuenta() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCuenta=2"))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }


        @Test @DisplayName("POST crearProducto → FORBIDDEN")
        void crearProducto() {
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            catDto.setId(cat.getId());
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());


            ResponseEntity<ProductoDTO> resp = restTemplate.exchange(
                RequestEntity.post(endpoint(port, "/producto?idCuenta=2"))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                ProductoDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("PUT actualizarProducto → FORBIDDEN")
        void actualizarProducto() {
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("GTIN-123");
            entrada.setSku("SKU-123");
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            catDto.setId(cat.getId());
            entrada.setCategorias(Collections.singleton(catDto));

            entrada.setAtributos(Collections.emptySet());

            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.put(endpoint(port, "/producto/" + prod.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("DELETE eliminarProducto → FORBIDDEN")
        void eliminarProducto() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/producto/" + prod.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }
}
