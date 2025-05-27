package uma.informatica.sii.gestor_productos.microservice_gestor_productos.RelacionTests;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DisplayName("Test de relaciones -")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RelacionApplicationTests {

    public static final String JWT_ADMIN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiQURNSU5JU1RSQURPUiIsInN1YiI6IjEiLCJpYXQiOjE3NDQ5MTQ3MDQsImV4cCI6MTgwNzk4NjcwNH0.YIXpA6aXXJ6q8tKjAAnVKT_uumuTdbhkLVieaCGf4vFtOMcYoNOH-FarolDduIQ3ulN-Gxy4TWBymK3ypZ38bQ";
    public static final String JWT_NO_VALIDO = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzk5OTl9.vxJDTpV1xoLOEO7ddw3ebtJblMtEN1umy3czSkgn4mE";

    @Value(value = "${local.server.port}")
    private int port;
    
    @Autowired
    private TestRestTemplate testRestTemplate;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;
    
    
    private MockRestServiceServer mockServer;
    
    @Autowired
    private ProductoRepository productoRepo;
    
    @Autowired
    private CategoriaRepository categoriaRepo;

    @Autowired
    private RelacionProductoRepository relacionProductoRepo;

    @Autowired
    private RelacionRepository relacionRepo;
    
    // Helper para construir URIs
    private static URI endpoint(int port, String pathAndQuery) {
        return URI.create("http://localhost:" + port + pathAndQuery);
    }
    
    private RequestEntity<Void> getRequest(String path) {
        return RequestEntity.get(endpoint(port, path))
            .header("Authorization", "Bearer " + JWT_ADMIN)
            .build();
    }

    private RequestEntity<Void> deleteRequest(String path) {
        return RequestEntity.delete(endpoint(port, path))
            .header("Authorization", "Bearer " + JWT_ADMIN)
            .build();
    }

    @BeforeEach
    void setup() {
        productoRepo.deleteAll();
        categoriaRepo.deleteAll();
        relacionProductoRepo.deleteAll();
        relacionRepo.deleteAll();
    }


    @Nested 
    @DisplayName("Cuando NO hay relaciones")
    public class SinRelaciones {
        
        @Test @DisplayName("GET sin params devuelve 400")
        void getSinParams() {
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/relacion?"),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET con >1 params devuelve 400")
        void getMultiplesParams() {
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/relacion?idRelacion=1&idCuenta=1"),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET idRelacion inexistente devuelve 404")
        void getIdNoExiste() {
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/relacion?idRelacion=999"),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("GET idCuenta sin relaciones devuelve []")
        void getPorCuentaVacia() {
            ResponseEntity<Set<RelacionDTO>> resp = testRestTemplate.exchange(
                getRequest("/relacion?idCuenta=1"),
                new ParameterizedTypeReference<Set<RelacionDTO>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test @DisplayName("POST crearRelacion devuelve 403")
        void crearRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("PUT modificarRelacion devuelve 404")
        void modificarRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/relacion/999"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("DELETE idRelacion que no existe devuelve 404")
        void deleteIdNoExiste() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                deleteRequest("/relacion/999"),
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

             // Inicializamos MockRestServiceServer sobre el RestTemplate interno
            mockServer = MockRestServiceServer.createServer(restTemplate);
        }
        private void stubUsuarioAdmin() {
            URI uriRoot = UriComponentsBuilder.fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriRoot))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]",
                    MediaType.APPLICATION_JSON
                ));

            URI uriById = UriComponentsBuilder.fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1).build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriById))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]",
                    MediaType.APPLICATION_JSON
                ));
        }

        private void stubCuentaPlan(int maxRelaciones) {
            URI uriCuenta = UriComponentsBuilder.fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", rel.getCuentaId())
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriCuenta))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"plan\":{\"maxRelaciones\":" + maxRelaciones + "}}]",
                    MediaType.APPLICATION_JSON
                ));
        }

        @Test @DisplayName("GET por idRelacion devuelve OK + DTO correcto [ERROR]")
        void getPorId() {
            stubUsuarioAdmin();

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                getRequest("/relacion?idRelacion=" + rel.getId()),
                RelacionDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Relacion1");
            mockServer.verify();
        }

        @Test @DisplayName("GET por idRelacion devuelve OK + DTO correcto, pero devuelve una unica relacion en vez de una lista [ERROR EN EL SERVICIO]")
        void getPorIdError() {
            stubUsuarioAdmin();

            ResponseEntity<Set<RelacionDTO>> resp = testRestTemplate.exchange(
                getRequest("/relacion?idRelacion=" + rel.getId()),
                new ParameterizedTypeReference<Set<RelacionDTO>>() {});

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);
            mockServer.verify();
        }

        @Test @DisplayName("GET por idCuenta devuelve lista con 1 elemento")
        void getPorCuenta() {
            stubUsuarioAdmin();

            ResponseEntity<Set<RelacionDTO>> resp = testRestTemplate.exchange(
                getRequest("/relacion?idCuenta=" + rel.getCuentaId()),
                new ParameterizedTypeReference<Set<RelacionDTO>>() {});

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);
            mockServer.verify();
        }

        @Test @DisplayName("POST crearRelacion devuelve 201 + DTO")
        void crearRelacion() {
            stubUsuarioAdmin();
            stubCuentaPlan(1000);

            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=" + rel.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getHeaders().getLocation()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("Relacion Nueva");
            assertThat(resp.getBody().getDescripcion()).isEqualTo("Descripcion");

            mockServer.verify();
        }

        @Test @DisplayName("POST crearRelacion devuelve 201 pero Location Error [ERROR EN EL CONTROLADOR]")
        void crearRelacionLocationError() {
            stubUsuarioAdmin();
            stubCuentaPlan(1000);

            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=" + rel.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );

            URI location = resp.getHeaders().getLocation();


            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getHeaders().getLocation()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("Relacion Nueva");
            assertThat(resp.getBody().getDescripcion()).isEqualTo("Descripcion");

            assertThat(location.getPath()).isEqualTo("/relacion/" + resp.getBody().getId());

            mockServer.verify();
        }


        @Test @DisplayName("POST crearRelacion sin datos devuelve 403")
        void crearRelacionSinDatos() {
            stubUsuarioAdmin();
            stubCuentaPlan(1000);

            RelacionEntradaDTO entrada = new RelacionEntradaDTO();

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=" + rel.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarRelacion devuelve 200 + cambios aplicados")
        void actualizarRelacion() {
            stubUsuarioAdmin();

            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Editada");
            entrada.setDescripcion("Desc Editada");

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/relacion/" + rel.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Relacion Editada");
            assertThat(resp.getBody().getDescripcion()).isEqualTo("Desc Editada");

            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarRelacion devuelve 200 + sin entidad en BD")
        void eliminarRelacion() {
            stubUsuarioAdmin();

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/relacion/" + rel.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(relacionRepo.findById(rel.getId())).isEmpty();
            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarRelacion devuelve 403 si la relación está en uso entre productos")
        void eliminarRelacionConProductos() {
            // Stub de usuario Admin
            stubUsuarioAdmin();
            // (opcionalmente stubCuentaPlan si tu endpoint lo requiere)

            // Creamos producto origen
            Producto ori = new Producto();
            ori.setGtin("GTIN-998");
            ori.setSku("SKU-998");
            ori.setNombre("ProdOrigen");
            ori.setCuentaId(rel.getCuentaId());
            ori.setRelacionesOrigen(Collections.emptySet());
            ori.setRelacionesDestino(Collections.emptySet());
            ori.setAtributos(Collections.emptySet());
            productoRepo.save(ori);

            // Creamos producto destino
            Producto dest = new Producto();
            dest.setGtin("GTIN-999");
            dest.setSku("SKU-999");
            dest.setNombre("ProdDestino");
            dest.setCuentaId(rel.getCuentaId());
            dest.setRelacionesOrigen(Collections.emptySet());
            dest.setRelacionesDestino(Collections.emptySet());
            dest.setAtributos(Collections.emptySet());
            productoRepo.save(dest);

            // Asignamos la relación entre ambos
            RelacionProducto relProd = new RelacionProducto();
            relProd.setTipoRelacion(rel);
            relProd.setProductoOrigen(ori);
            relProd.setProductoDestino(dest);
            relacionProductoRepo.save(relProd);

            // Intentamos borrar la relación
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                deleteRequest("/relacion/" + rel.getId()),
                Void.class
            );

            // Comprobamos 403 Forbidden
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            // Comprobamos que la relación sigue en la BD
            assertThat(relacionRepo.findById(rel.getId())).isPresent();
            // Comprobamos que la relación entre productos sigue en la BD
            assertThat(relacionProductoRepo.findById(relProd.getId())).isPresent();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("Usuario no pertenece a cuenta")
    class UsuarioNoPerteneceCuenta {

        private Relacion rel;
        private MockRestServiceServer mockServer;

        @BeforeEach
        void datosYStubs() {
            // Preparamos datos
            rel = new Relacion();
            rel.setNombre("Relacion1");
            rel.setCuentaId(3);
            relacionRepo.save(rel);

            // Inicializamos el MockRestServiceServer
            mockServer = MockRestServiceServer.createServer(restTemplate);

            // Configuramos stubs
            stubUsuarioCliente();
            stubUsuarioPerteneceCuenta(rel.getCuentaId(), false);
        }

        private void stubUsuarioCliente() {
            URI uriRoot = UriComponentsBuilder.fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriRoot))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                    MediaType.APPLICATION_JSON
                ));

            URI uriById = UriComponentsBuilder.fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1).build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriById))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                    MediaType.APPLICATION_JSON
                ));
        }

        private void stubUsuarioPerteneceCuenta(int cuentaId, boolean pertenece) {
            URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + cuentaId + "/usuarios")
                .build().toUri();
            String body = pertenece
                ? "[{\"id\":1,\"role\":\"CLIENTE\"}]"
                : "[]";
            mockServer.expect(ExpectedCount.once(), requestTo(uri))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        }

        @Test @DisplayName("GET por idRelacion devuelve FORBIDDEN")
        void getPorId() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/relacion?idRelacion=" + rel.getId()),
                Void.class
            );
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }

        @Test @DisplayName("GET por idCuenta devuelve FORBIDDEN")
        void getPorCuenta() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/relacion?idCuenta=" + rel.getCuentaId()),
                Void.class
            );
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }

        @Test @DisplayName("POST crearRelacion devuelve FORBIDDEN")
        void crearRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=" + rel.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarRelacion devuelve FORBIDDEN")
        void actualizarRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Editada");
            entrada.setDescripcion("Desc");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/relacion/" + rel.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class
            );
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarRelacion devuelve FORBIDDEN")
        void eliminarRelacion() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                deleteRequest("/relacion/" + rel.getId()),
                Void.class
            );
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("Usuario no puede crear relación (plan lleno)")
    class UsuarioNoPuedeCrearRelacion {

        @BeforeEach
        void datos() {
                // Inicializamos MockRestServiceServer sobre el RestTemplate interno
            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
        }
        private void stubUsuarioCliente() {
            URI uriRoot = UriComponentsBuilder.fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriRoot))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                    MediaType.APPLICATION_JSON
                ));

            URI uriById = UriComponentsBuilder.fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1).build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriById))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                    MediaType.APPLICATION_JSON
                ));
        }
        private void stubUsuarioPerteneceCuenta(int cuentaId, boolean pertenece) {
            URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + cuentaId + "/usuarios")
                .build().toUri();
            String body = pertenece
                ? "[{\"id\":1,\"role\":\"CLIENTE\"}]"
                : "[]";
            mockServer.expect(requestTo(uri))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        }

        private void stubCuentaPlan(int maxRelaciones) {
            URI uriCuenta = UriComponentsBuilder.fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.once(), requestTo(uriCuenta))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "[{\"id\":1,\"plan\":{\"maxRelaciones\":0}}]",
                    MediaType.APPLICATION_JSON
                ));
        }

        @Test @DisplayName("POST crearRelacion devuelve FORBIDDEN por plan lleno")
        void crearRelacionPlanLleno() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            stubUsuarioPerteneceCuenta(1, true);
            stubCuentaPlan(0);

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            assertThat(relacionRepo.findAll().isEmpty());
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("Token no válido")
    class TokenNoValido {

        @Test @DisplayName("GET por idRelacion devuelve FORBIDDEN")
        void getPorId() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idRelacion=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("GET por idCuenta devuelve FORBIDDEN")
        void getPorCuenta() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("POST crearRelacion devuelve FORBIDDEN")
        void crearRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("PUT actualizarRelacion devuelve FORBIDDEN")
        void actualizarRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Editada");
            entrada.setDescripcion("Desc");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/relacion/1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("DELETE eliminarRelacion devuelve FORBIDDEN")
        void eliminarRelacion() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/relacion/1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }
    }

    @Nested
    @DisplayName("Credenciales no válidas")
    class CredencialesNoValidas {

        private Relacion rel;

        @BeforeEach
        void initMockAndDatos() {
            // Inicializamos MockRestServiceServer sobre el RestTemplate interno
            mockServer = MockRestServiceServer.createServer(restTemplate);

            // Creamos una relación de ejemplo
            rel = new Relacion();
            rel.setNombre("Relacion1");
            rel.setCuentaId(1);
            relacionRepo.save(rel);

            stubUsuarioNoValido();
        }

        private void stubUsuarioNoValido() {
            URI uriById = UriComponentsBuilder.fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(requestTo(uriById))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withUnauthorizedRequest()   
                );
        }

        @Test @DisplayName("GET por idRelacion devuelve UNAUTHORIZED")
        void getPorId() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idRelacion=" + rel.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("GET por idCuenta devuelve UNAUTHORIZED")
        void getPorCuenta() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/relacion?idCuenta=" + rel.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();

        }

        @Test @DisplayName("POST crearRelacion devuelve UNAUTHORIZED")
        void crearRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Nueva");
            entrada.setDescripcion("Descripcion");

            ResponseEntity<RelacionDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/relacion?idCuenta=" + rel.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                RelacionDTO.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarRelacion devuelve UNAUTHORIZED")
        void actualizarRelacion() {
            RelacionEntradaDTO entrada = new RelacionEntradaDTO();
            entrada.setNombre("Relacion Editada");
            entrada.setDescripcion("Desc");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/relacion/" + rel.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();

        }

        @Test @DisplayName("DELETE eliminarRelacion devuelve UNAUTHORIZED")
        void eliminarRelacion() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/relacion/" + rel.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class
            );

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }
    }
}