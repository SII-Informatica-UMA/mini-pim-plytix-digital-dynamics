package uma.informatica.sii.gestor_productos.microservice_gestor_productos.ProductoTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;



import org.springframework.core.ParameterizedTypeReference;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario.Rol;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.security.JwtUtil;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.CategoriaMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductoApplicationTests {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiQURNSU5JU1RSQURPUiIsInN1YiI6IjEiLCJpYXQiOjE3NDQ5MTQ3MDQsImV4cCI6MTgwNzk4NjcwNH0.YIXpA6aXXJ6q8tKjAAnVKT_uumuTdbhkLVieaCGf4vFtOMcYoNOH-FarolDduIQ3ulN-Gxy4TWBymK3ypZ38bQ\r\n";

    private URI uri(String path) {
        UriBuilderFactory factory = new DefaultUriBuilderFactory();
        UriBuilder builder = factory.builder()
            .scheme("http").host("localhost").port(port).path(path);
        return builder.build();
    }

    @BeforeEach
    public void setupDatabase() {
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
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

    @TestConfiguration
    static class StubServicesConfig {
        @Bean
        @Primary
        public UsuarioService usuarioServiceStub(RestTemplate restTemplate, JwtUtil jwtUtil) {
            return new UsuarioService(restTemplate, jwtUtil) {
                @Override
                public Optional<UsuarioDTO> getUsuarioConectado(String jwtToken) {
                    if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                        jwtToken = jwtToken.substring(7);
                    }
                    if (VALID_TOKEN.equals(jwtToken)) {
                        UsuarioDTO usuario = new UsuarioDTO();
                        usuario.setId(1L);
                        usuario.setRole(Rol.ADMINISTRADOR);
                        return Optional.of(usuario);
                    }
                    return Optional.empty();
                }
                @Override
                public boolean usuarioPerteneceACuenta(Integer idCuenta, Long usuarioId, String jwtTokenDelUsuario) {
                    // return VALID_TOKEN.equals(jwtTokenDelUsuario) && idCuenta.equals(1) && usuarioId.equals(1L);
                    return true;
                }
                @Override
                public Optional<UsuarioDTO> getUsuario(Long usuarioId, String jwtToken) {
                    System.out.println("Stub getUsuario invocado con ID: " + usuarioId);
                    if (usuarioId == 1L) {
                        UsuarioDTO usuario = new UsuarioDTO();
                        usuario.setId(1L);
                        usuario.setRole(Rol.ADMINISTRADOR);
                        return Optional.of(usuario);
                    }
                    return Optional.empty();
                }

            };
        }

        @Bean
        @Primary
        public CuentaService cuentaServiceStub(RestTemplate restTemplate, JwtUtil jwtUtil) {
            return new CuentaService(restTemplate, jwtUtil) {
                @Override
                public boolean puedeCrearProducto(Integer cuentaId, int productosActuales, UsuarioDTO usuario) {
                    // Permitimos siempre si viene un usuario válido
                    return true;
                }
            };
        }
    }

    @Nested
    @DisplayName("Controlador /producto sin productos previos")
    class ControladorVacio {
        @Test
        @DisplayName("GET /producto?idCuenta=1 devuelve lista vacía")
        public void listaVacia() {
            URI endpoint = uri("/producto?idCuenta=1");
            RequestEntity<Void> request = RequestEntity.get(endpoint)
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .build();
        
            ResponseEntity<Set<ProductoDTO>> response = restTemplate.exchange(request,
                new org.springframework.core.ParameterizedTypeReference<Set<ProductoDTO>>() {});
            

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("POST /producto sin categorías devuelve 500")
        public void crearSinCategorias() {
            ProductoEntradaDTO dto = new ProductoEntradaDTO();
            dto.setGtin("0000000000000");
            dto.setSku("SKU0");
            dto.setNombre("SinCat");
            dto.setTextoCorto("Desc");
            dto.setMiniatura("url");
            dto.setCategorias(Collections.emptySet());

            URI endpoint = uri("/producto?idCuenta=1");
            RequestEntity<ProductoEntradaDTO> request = RequestEntity.post(endpoint)
                .header("Authorization", VALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto);

            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            assertThat(response.getStatusCodeValue()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("Controlador /producto con productos existentes")
    class ControladorConDatos {
        private Integer catId;
        private Integer prodId;

        @BeforeEach
        public void initDatos() {
            Categoria cat = new Categoria();
            cat.setId(1);
            cat.setNombre("Cat1");
            categoriaRepository.save(cat);
            catId = cat.getId();

            Producto prod = new Producto();
            prod.setGtin("1111111111111");
            prod.setSku("SKU1");
            prod.setNombre("Prod1");
            prod.setTextoCorto("Desc1");
            prod.setMiniatura("url1");
            prod.setCuentaId(1);
            prod.getCategorias().add(cat);
            productoRepository.save(prod);
            prodId = prod.getId();
        }

        @Test
        @DisplayName("GET /producto?idCuenta=1 devuelve lista con 1")
        public void listaConUno() {
            URI endpoint = uri("/producto?idCuenta=1");
            RequestEntity<Void> request = RequestEntity.get(endpoint)
                .header("Authorization", VALID_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .build();

            ResponseEntity<Set<ProductoDTO>> response = restTemplate.exchange(request,
                new org.springframework.core.ParameterizedTypeReference<Set<ProductoDTO>>() {});

            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("GET /producto/{id} devuelve 404 para id inválido")
        public void getNoExistente() {
            URI endpoint = uri("/producto/999");
            RequestEntity<Void> request = RequestEntity.get(endpoint)
                .header("Authorization", VALID_TOKEN)
                .build();

            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            assertThat(response.getStatusCodeValue()).isEqualTo(404);
        }

        @Test
        @DisplayName("GET /producto/{id} devuelve el producto existente")
        public void getExistente() {
            URI endpoint = uri("/producto/" + prodId);
            RequestEntity<Void> request = RequestEntity.get(endpoint)
                .header("Authorization", VALID_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .build();

            ResponseEntity<ProductoDTO> response = restTemplate.exchange(request, ProductoDTO.class);
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody().getSku()).isEqualTo("SKU1");
        }

        @Test
        @DisplayName("PUT /producto/{id} actualiza el nombre")
        public void actualizar() {
            ProductoEntradaDTO dto = new ProductoEntradaDTO();
            dto.setGtin("1111111111111");
            dto.setSku("SKU1");
            dto.setNombre("Modificado");
            dto.setTextoCorto("Desc1");
            dto.setMiniatura("url1");
            CategoriaDTO catDTO = CategoriaMapper.toDTO(categoriaRepository.findById(catId).get());
            dto.setCategorias(Set.of(catDTO));

            URI endpoint = uri("/producto/" + prodId);
            RequestEntity<ProductoEntradaDTO> request = RequestEntity.put(endpoint)
                .header("Authorization", VALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto);

            ResponseEntity<ProductoDTO> response = restTemplate.exchange(request, ProductoDTO.class);
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody().getNombre()).isEqualTo("Modificado");
        }

        @Test
        @DisplayName("DELETE /producto/{id} elimina correctamente")
        public void eliminar() {
            URI endpoint = uri("/producto/" + prodId);
            RequestEntity<Void> request = new RequestEntity<>(null, null, HttpMethod.DELETE, endpoint);
            ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(productoRepository.findById(prodId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Servicio Producto")
    class ServicioSinMocks {
        @Autowired
        private uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.ProductoService productoService;

        @Test
        @DisplayName("getProductoPorId lanza EntidadNoExistente para id inválido")
        public void servicioGetNoExistente() {
            assertThrows(EntidadNoExistente.class, () -> productoService.getProductoPorId(999, "Bearer valid-user-token"));
        }

        @Test
        @DisplayName("eliminarProducto lanza EntidadNoExistente para id inválido")
        public void servicioDeleteNoExistente() {
            assertThrows(EntidadNoExistente.class, () -> productoService.eliminarProducto(999, "Bearer valid-user-token"));
        }
    }
}
