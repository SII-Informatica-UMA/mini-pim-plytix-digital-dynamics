package uma.informatica.sii.gestor_productos.microservice_gestor_productos.CategoriaTests;

import static org.assertj.core.api.Assertions.assertThat;


import java.net.URI;
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


import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DisplayName("Tests de Categorías -")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CategoriaApplicationTests {

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


    

    @BeforeEach
    void init(){
        // Limpiamos la base de datos antes de cada test
        productoRepo.deleteAll();
        categoriaRepo.deleteAll();
        relacionProductoRepo.deleteAll();
        relacionRepo.deleteAll();
    }

    @Nested
    @DisplayName("Cuando NO hay categorías")
    class SinCategorias {
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
        private void stubUsuarioCliente() {
            URI uriRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));

            URI uriById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
        }
        private void stubCuentaPlan(int maxCategorias) {
            URI uriCuenta = UriComponentsBuilder.fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", 3)
                .build().toUri();
            mockServer.expect(ExpectedCount.once(), requestTo(uriCuenta))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"plan\":{\"maxProductos\":" + 0 + "}}]",
                        MediaType.APPLICATION_JSON
                    ));
        }

        @Test @DisplayName("GET sin parametros devuelve 400")
        void getSinParams() {
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/categoria-producto?"),
                String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET cuenta inexistente devuelve []")
        void getCuentaNoExiste() {
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/categoria-producto?idCuenta=9999"),
                String.class);
        
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEqualTo("[]");
            
        }
        
        @Test @DisplayName("GET cuenta válida pero sin categorías devuelve []")
        void getCuentaSinCategorias() {
            ResponseEntity<CategoriaDTO[]> resp = testRestTemplate.exchange(
                getRequest("/categoria-producto?idCuenta=2"),
                CategoriaDTO[].class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test @DisplayName("POST crearCategoria válida devuelve 201 [ERROR]")
        void crearCategoria() {
            
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("NuevaCategoria");
            
            ResponseEntity<CategoriaDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=2"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                CategoriaDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("NuevaCategoria");
            
        }

        @Test @DisplayName("POST crearCategoria válido pero Location ERROR [ERROR EN EL CONTROLADOR]")
        void crearCategoriaLocationError() {
            
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("NuevaCategoria");
            
            ResponseEntity<CategoriaDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=2"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                CategoriaDTO.class);

            URI location = resp.getHeaders().getLocation();

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("NuevaCategoria");

            // Verificamos que la Location no es correcta
            assertThat(location.getPath()).isEqualTo("/categoria-producto/" + resp.getBody().getId());
            
        }

        @Test
        @DisplayName("GET por cuenta sin permiso devuelve 403")
        void getCategoriasSinPermiso() {
            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
            stubUsuarioPerteneceCuenta(4, false);
            Categoria c = new Categoria();
            c.setNombre("CatInvalida");
            c.setCuentaId(4);
            categoriaRepo.save(c);

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria-producto?idCuenta=" + c.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }

        @Test
        @DisplayName("POST crearCategoria devuelve FORBIDDEN")
        void crearCategoriaNoPermitida() {
            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
            stubUsuarioPerteneceCuenta(4, false);
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("NoPermitida");

            ResponseEntity<CategoriaDTO> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=4")) // cuenta NO autorizada
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                CategoriaDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }

        @Test @DisplayName("POST crearCategoria sin permisos suficientes por maxCateg devuelve 403")
        void crearCategoriaSinPermisos() {
            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
            stubUsuarioPerteneceCuenta(3, true);
            stubCuentaPlan(0);
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("OtraCategoria");
    
            ResponseEntity<String> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=3"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                String.class);
    
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
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
            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioAdmin();
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

        @Test @DisplayName("GET por idCategoria devuelve OK [ERROR]")
        void getPorIdCategoria() {

            ResponseEntity<CategoriaDTO> resp = testRestTemplate.exchange(
                getRequest("/categoria-producto?idCategoria=" + cat.getId()),
                CategoriaDTO.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Existente");
        }

        @Test @DisplayName("GET por idCategoria devuelve OK, pero devuelve una Categoria en vez de una lista [ERROR EN EL SERVICIO]")
        void getPorIdCategoriaError() {

            ResponseEntity<Set<CategoriaDTO>> resp = testRestTemplate.exchange(
                getRequest("/categoria-producto?idCategoria=" + cat.getId()),
                new ParameterizedTypeReference<Set<CategoriaDTO>>() {});
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().size()).isEqualTo(1);
        }

        @Test @DisplayName("GET por idCuenta devuelve OK")
        void getPorIdCuenta() {
            
            ResponseEntity<Set<CategoriaDTO>> resp = testRestTemplate.exchange(
                getRequest("/categoria-producto?idCuenta=" + cat.getCuentaId()),
                new ParameterizedTypeReference<Set<CategoriaDTO>>() {});

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().size()).isEqualTo(1);
            assertThat(resp.getBody().iterator().next().getNombre()).isEqualTo("Existente");
        }

        @Test @DisplayName("PUT actualizarCategoria devuelve 200")
        void actualizarCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("Renombrada");

            ResponseEntity<CategoriaDTO> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/categoria-producto/" + cat.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                CategoriaDTO.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("Renombrada");
        }

        @Test @DisplayName("DELETE eliminarCategoria devuelve 200")
        void eliminarCategoria() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/categoria-producto/" + cat.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(categoriaRepo.findById(cat.getId())).isEmpty();
        }


        @Test 
        @DisplayName("POST crearCategoria nombre existente devuelve 403 [ERROR EN EL SERVICIO]")
        // En el servicio se lanza una excepción incorrecta al existir el nombre
        void crearCategoriaNombreExistente() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre(cat.getNombre());

            ResponseEntity<String> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=" + cat.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                String.class); 

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }     
    }

    @Nested
    @DisplayName("Usuario no pertenece a cuenta")
    class UsuarioNoPertenece {

        private Categoria cat;
        
        @BeforeEach
        void init() {
            cat = new Categoria();
            cat.setNombre("CatPrivada");
            cat.setCuentaId(1);
            categoriaRepo.save(cat);
            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
            stubUsuarioPerteneceCuenta(cat.getCuentaId(), false);
        }
        private void stubUsuarioCliente() {
            URI uriRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));

            URI uriById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
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

        @Test @DisplayName("GET por idCategoria devuelve FORBIDDEN")
        void getPorIdCategoria() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria-producto?idCategoria=" + cat.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }

        @Test @DisplayName("GET por idCuenta devuelve FORBIDDEN")
        void getPorCuenta() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria-producto?idCuenta=" + cat.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();

        }

        @Test @DisplayName("POST crearCategoria devuelve FORBIDDEN")
        void crearCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("NoPermitida");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=" + cat.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarCategoria devuelve FORBIDDEN")
        void actualizarCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("IntentoEditar");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/categoria-producto/" + cat.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarCategoria devuelve FORBIDDEN")
        void eliminarCategoria() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/categoria-producto/" + cat.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("Token no válido")
    class TokenNoValido {
        @Test @DisplayName("GET por idCategoria devuelve 403")
        void getPorIdCategoria() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria-producto?idCategoria=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("POST crearCategoria devuelve 403")
        void crearCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("NoPermitida");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("PUT actualizarCategoria devuelve 403")
        void actualizarCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("IntentoEditar");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/categoria-producto/1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("DELETE eliminarCategoria devuelve 403")
        void eliminarCategoria() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/categoria-producto/1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("GET por idCuenta devuelve 403")
        void getPorIdCuenta() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria-producto?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }
    }

    @Nested
    @DisplayName("Credenciales no válidas")
    class CredencialesNoValidas {

        private Categoria cat;

        @BeforeEach
        void initMockAndDatos() {
            // Inicializamos MockRestServiceServer sobre el RestTemplate interno
            mockServer = MockRestServiceServer.createServer(restTemplate);

            // Creamos una categoría de prueba
            cat = new Categoria();
            cat.setNombre("CatPrivada");
            cat.setCuentaId(1);
            categoriaRepo.save(cat);

            stubUsuarioNoValido();
        }

        private void stubUsuarioNoValido() {
            URI uriById = UriComponentsBuilder.fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(requestTo(uriById))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withUnauthorizedRequest()   
                );
        }


        @Test @DisplayName("GET por idCategoria devuelve 401")
        void getPorIdCategoria() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria-producto?idCategoria=1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("POST crearCategoria devuelve 401")
        void crearCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("NoPermitida");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/categoria-producto?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("PUT actualizarCategoria devuelve 401")
        void actualizarCategoria() {
            CategoriaEntradaDTO entrada = new CategoriaEntradaDTO();
            entrada.setNombre("IntentoEditar");

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/categoria-producto/1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("DELETE eliminarCategoria devuelve 401")
        void eliminarCategoria() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.delete(endpoint(port, "/categoria-producto/1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("GET por idCuenta devuelve 401")
        void getPorIdCuenta() {
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/categoria-producto?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();
        }
    }
}