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
@DisplayName("Tests de Productos- ")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductoApplicationTests {
    
    @Value(value = "${local.server.port}")
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductoRepository productoRepo;
    
    @Autowired
    private CategoriaRepository categoriaRepo;

    @Autowired
    private RelacionProductoRepository relacionProductoRepo;

    @Autowired
    private RelacionRepository relacionRepo;
    
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
    private URI uri(String scheme, String host, int port, String... paths) {
        UriBuilderFactory ubf = new DefaultUriBuilderFactory();
        UriBuilder ub = ubf.builder()
        .scheme(scheme)
        .host(host).port(port);
        for (String path : paths) {
            ub = ub.path(path);
        }
        return ub.build();
    }
    
    private RequestEntity<Void> get(String scheme, String host, int port, String path) {
        URI uri = uri(scheme, host, port, path);
        var peticion = RequestEntity.get(uri)
        .accept(MediaType.APPLICATION_JSON)
        .build();
        return peticion;
    }
    
    private RequestEntity<Void> delete(String scheme, String host, int port, String path) {
        URI uri = uri(scheme, host, port, path);
        var peticion = RequestEntity.delete(uri)
        .build();
        return peticion;
    }
    
    private <T> RequestEntity<T> post(String scheme, String host, int port, String path, T object) {
        URI uri = uri(scheme, host, port, path);
        var peticion = RequestEntity.post(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(object);
        return peticion;
    }
    
    private <T> RequestEntity<T> put(String scheme, String host, int port, String path, T object) {
        URI uri = uri(scheme, host, port, path);
        var peticion = RequestEntity.put(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(object);
        return peticion;
    }
    
    // Helper para construir URIs
    private static URI endpoint(int port, String pathAndQuery) {
        return URI.create("http://localhost:" + port + pathAndQuery);
    }
    
    @Nested 
    @DisplayName("Cuando NO hay productos")
    public class SinProductos {
        
        @Test @DisplayName("GET sin params → 400")
        void getSinParams() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?"))
                .header(AUTH_HEADER, TOKEN)
                .build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET con >1 params → 400")
        void getMultiplesParams() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idProducto=1&idCuenta=1"))
                    .header(AUTH_HEADER, TOKEN).build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody()).contains("Debe proporcionar exactamente un parámetro de consulta.");
        }

        @Test @DisplayName("GET idProducto inexistente → 404")
        void getIdNoExiste() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idProducto=999"))
                    .header(AUTH_HEADER, TOKEN).build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("GET gtin inexistente → 404")
        void getGtinNoExiste() {
            ResponseEntity<String> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?gtin=XX"))
                    .header(AUTH_HEADER, TOKEN).build(),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("GET idCuenta sin productos → []")
        void getPorCuentaVacia() {
            // Creamos una categoría para la cuenta
            Categoria c = new Categoria();
            c.setNombre("C1");
            c.setCuentaId(1);
            categoriaRepo.save(c);

            ResponseEntity<Set<ProductoDTO>> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCuenta=1"))
                    .header(AUTH_HEADER, TOKEN).build(),
                new org.springframework.core.ParameterizedTypeReference<Set<ProductoDTO>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test @DisplayName("GET idCategoria inexistente → 404")
        void getCategoriaNoExiste() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCategoria=50"))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("GET idCategoria sin productos → 404")
        void getCategoriaSinProductos() {
            // Creamos una categoría para la cuenta
            Categoria c = new Categoria();
            c.setNombre("C1");
            c.setCuentaId(1);
            categoriaRepo.save(c);

            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCategoria=" + c.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("DELETE idProducto inexistente → 404")
        void deleteIdNoExiste() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/producto/999"))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested @DisplayName("Con productos existentes")
    public class ConProductos {

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

        @Test @DisplayName("GET Sin params → 400")
        void getSinParams(){
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto"))
                    .header(AUTH_HEADER, TOKEN).build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET por idProducto → OK + DTO correcto")
        void getPorId() {
            ResponseEntity<ProductoDTO> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idProducto=" + prod.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                ProductoDTO.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA");
        }

        @Test @DisplayName("GET por gtin → OK + DTO correcto")
        void getPorGtin() {
            ResponseEntity<ProductoDTO> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?gtin=GTIN-123"))
                    .header(AUTH_HEADER, TOKEN).build(),
                ProductoDTO.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getId()).isEqualTo(prod.getId());
        }

        @Test @DisplayName("GET por idCuenta → lista con 1 elemento")
        void getPorCuenta() {
            ResponseEntity<Set<ProductoDTO>> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCuenta=2"))
                    .header(AUTH_HEADER, TOKEN).build(),
                new org.springframework.core.ParameterizedTypeReference<Set<ProductoDTO>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);
        }

        @Test @DisplayName("GET por idCategoria → lista con 1 elemento")
        void getPorCategoria() {
            ResponseEntity<Set<ProductoDTO>> resp = restTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCategoria=" + cat.getId()))
                    .header(AUTH_HEADER, TOKEN).build(),
                new org.springframework.core.ParameterizedTypeReference<Set<ProductoDTO>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);
        }

        @Test @DisplayName("POST crearProducto → 201 + Location + DTO")
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

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getHeaders().getLocation()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("NuevoProd");
        }

        @Test @DisplayName("POST crearProducto → 404 + sin categoria")
        void crearProductoSinCategoria() {
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            entrada.setCategorias(Collections.emptySet());
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());


            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.post(endpoint(port, "/producto?idCuenta=2"))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("POST crearProducto con GTIN existente → 403")
        void crearProductoConGtinExistente() {
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("GTIN-123");
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

            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.post(endpoint(port, "/producto?idCuenta=2"))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("PUT actualizarProducto → 200 + cambios aplicados")
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

            ResponseEntity<ProductoDTO> resp = restTemplate.exchange(
                RequestEntity.put(endpoint(port, "/producto/" + prod.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                ProductoDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA-Edit");
        }

        @Test
        @Transient
        @DisplayName("PUT actualizarProducto → elimina relaciones obsoletas en ambos sentidos")
        void actualizarProductoEliminarRelaciones() {
            // 1) crea un segundo producto y un tipo de relación
            Producto dest = new Producto();
            dest.setGtin("GTIN-999");
            dest.setSku("SKU-999");
            dest.setNombre("ProdDestino");
            dest.setCuentaId(1);
            dest.setRelacionesOrigen(Collections.emptySet());
            dest.setRelacionesDestino(Collections.emptySet());
            dest.setAtributos(Collections.emptySet());
            productoRepo.save(dest);

            Relacion tipo = new Relacion();
            tipo.setNombre("TIPO");
            tipo.setCuentaId(1);
            relacionRepo.save(tipo);

            // 2) crea la relación bidireccional inicial A->dest y dest->A
            RelacionProducto relAB = new RelacionProducto();
            relAB.setProductoOrigen(prod);
            relAB.setProductoDestino(dest);
            relAB.setTipoRelacion(tipo);
            relacionProductoRepo.save(relAB);

            RelacionProducto relBA = new RelacionProducto();
            relBA.setProductoOrigen(dest);
            relBA.setProductoDestino(prod);
            relBA.setTipoRelacion(tipo);
            relacionProductoRepo.save(relBA);

            // comprueba que existía
            assertThat(relacionProductoRepo.findByProductoOrigen(prod)).hasSize(1);

            // 3) lanza la petición de actualización con DTO.relaciones vacío
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            catDto.setId(cat.getId());
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            restTemplate.exchange(
            RequestEntity.put(endpoint(port, "/producto/" + prod.getId()))
                .header(AUTH_HEADER, TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada),
            ProductoDTO.class
            );

            // 4) comprueba que ya no hay ni A->dest ni dest->A
            //assertThat(relacionProductoRepo.findByProductoOrigen(prod)).isEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigen(dest)).isEmpty();
        }

        @Test @DisplayName("DELETE eliminarProducto → 200 + sin entidad en BD")
        void eliminarProducto() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/producto/" + prod.getId()))
                    .header(AUTH_HEADER, TOKEN)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(productoRepo.findById(5)).isEmpty();
        }
    }
}