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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiQ0xJRU5URSIsInN1YiI6IjIiLCJpYXQiOjE3NDQ5MTQ2NTMsImV4cCI6MTgwNzk4NjY1M30.vTQEIGffqIwqWRbbxihuplJhLfXi6Flhs_zXKOtxjQJoJipIaSxSqPBrqurDu9u296vo7qwpHLvisf3yQHa--w";

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

    @TestConfiguration
    static class StubServicesConfig {
        @Bean
        @Primary
        public UsuarioService usuarioServiceStub(RestTemplate restTemplate, JwtUtil jwtUtil) {
            return new UsuarioService(restTemplate, jwtUtil) {
                @Override
                public Optional<UsuarioDTO> getUsuarioConectado(String jwtToken) {
                    if (VALID_TOKEN.equals(jwtToken)) {
                        return Optional.of(new UsuarioDTO());
                    }
                    return Optional.empty();
                }

                @Override
                public boolean usuarioPerteneceACuenta(Integer idCuenta, Long usuarioId, String jwtTokenDelUsuario) {
                    return VALID_TOKEN.equals(jwtTokenDelUsuario)
                        && idCuenta.equals(1)
                        && usuarioId.equals(1L);
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
                    return usuario.getRole() == Rol.CLIENTE;
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
                .header("Authorization", VALID_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .build();

            ResponseEntity<Set<ProductoDTO>> response = restTemplate.exchange(request,
                new org.springframework.core.ParameterizedTypeReference<Set<ProductoDTO>>() {});

            assertThat(response.getStatusCodeValue()).isEqualTo(200);
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
