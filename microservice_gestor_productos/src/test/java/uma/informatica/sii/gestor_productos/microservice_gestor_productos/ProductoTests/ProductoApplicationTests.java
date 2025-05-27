package uma.informatica.sii.gestor_productos.microservice_gestor_productos.ProductoTests;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Tests del microservicio de productos -> ")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductoApplicationTests {

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
    void init(){
        // Limpiamos la base de datos antes de cada test
        productoRepo.deleteAll();
        categoriaRepo.deleteAll();
        relacionProductoRepo.deleteAll();
        relacionRepo.deleteAll();
    }
    
    @Nested
    @DisplayName("Cuando NO hay productos")
    class SinProductos {

        @Test @DisplayName("GET sin params da 400")
        void getSinParams() {
            //Arrange vacio

            //Act
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/producto?"),
                String.class
            );

            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test @DisplayName("GET con >1 params da 400")
        void getMultiplesParams() {
            //Arrange vacio

            //Act
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/producto?idProducto=1&idCuenta=1"),
                String.class
            );

            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody()).contains("Debe proporcionar exactamente un parámetro de consulta.");
        }

        @Test @DisplayName("GET idProducto inexistente da 404")
        void getIdNoExiste() {
            //Arrange vacio

            //Act
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/producto?idProducto=999"),
                String.class
            );

            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("GET gtin inexistente da 404")
        void getGtinNoExiste() {
            //Arrange vacio

            //Act
            ResponseEntity<String> resp = testRestTemplate.exchange(
                getRequest("/producto?gtin=XX"),
                String.class
            );

            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test @DisplayName("GET idCuenta sin productos devuelve []")
        void getPorCuentaVacia() {
            //Arrange vacio

            //Act
            ResponseEntity<Set<ProductoDTO>> resp = testRestTemplate.exchange(
                getRequest("/producto?idCuenta=1"),
                new ParameterizedTypeReference<Set<ProductoDTO>>() {}
            );

            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test @DisplayName("GET idCategoria inexistente da 404")
        void getCategoriaNoExiste() {
            //Arrange vacio

            //Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCategoria=50"),
                Void.class
            );

            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("GET idCategoria sin productos da 404")
        void getCategoriaSinProductos() {
            // Creamos una categoría para la cuenta (Arrange)
            Categoria c = new Categoria();
            c.setNombre("C1");
            c.setCuentaId(1);
            categoriaRepo.save(c);

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCategoria=" + c.getId()),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("DELETE idProducto inexistente da 404")
        void deleteIdNoExiste() {
            //Arrange vacio

            //Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                deleteRequest("/producto/999"),
                Void.class
            );

            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test 
        @DisplayName("PUT actualizar producto inexistente da 404")
        void putIdNoExiste() {
            //Arrange 
            ProductoEntradaDTO dummy = new ProductoEntradaDTO();
            //Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/producto/999"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dummy),
                Void.class);
            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("POST crear producto sin idCuenta da 400 [ERROR EN EL CONTROLADOR]")
        // El controlador de POST no tiene Required = True en idCuenta
        void postSinIdCuenta() {
            //Arrange 
            ProductoEntradaDTO dummy = new ProductoEntradaDTO();
            //Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/producto"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dummy),
                Void.class);
            //Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

    }

    @Nested
    @DisplayName("Con productos existentes")
    class ConProductos {

        private Categoria cat;
        private Producto prod;

        @BeforeEach
        void datos() {
            // Arrange general para los tests cuando hay productos
            cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(1);

            prod = new Producto();
            prod.setGtin("GTIN-123");
            prod.setSku("SKU-123");
            prod.setNombre("ProdA");
            prod.setCuentaId(1);
            prod.getCategorias().add(cat);
            prod.setRelacionesOrigen(Collections.emptySet());
            prod.setRelacionesDestino(Collections.emptySet());
            prod.setAtributos(Collections.emptySet());
            productoRepo.save(prod);

            mockServer = MockRestServiceServer.createServer(restTemplate);
        }

        // Funciones para mocks
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

        private void stubCuentaPlan(int maxProductos) {
            URI uriCuenta = UriComponentsBuilder.fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", prod.getCuentaId())
                .build().toUri();
            mockServer.expect(ExpectedCount.once(), requestTo(uriCuenta))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"plan\":{\"maxProductos\":" + maxProductos + "}}]",
                        MediaType.APPLICATION_JSON
                    ));
        }

        @Test @DisplayName("GET por idProducto da OK [ERROR]")
        void getPorId() {
            

            stubUsuarioAdmin();

            // Act
            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(
                getRequest("/producto?idProducto=" + prod.getId()),
                ProductoDTO.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA");
            assertThat(resp.getBody().getGtin()).isEqualTo("GTIN-123");
            assertThat(resp.getBody().getCategorias())
                .extracting(CategoriaDTO::getNombre)
                .containsExactly("CatX");

            mockServer.verify();
        }

        @Test @DisplayName("GET por idProducto devuelve un unico producto en vez de lista [ERROR EN EL SERVICIO]")
        void getPorIdError() {
            

            stubUsuarioAdmin();

            // Act
            ResponseEntity<Set<ProductoDTO>> resp = testRestTemplate.exchange(
                getRequest("/producto?idProducto=" + prod.getId()),
                new ParameterizedTypeReference<Set<ProductoDTO>>() {}
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().size()).isEqualTo(1);

            mockServer.verify();
        }

        @Test @DisplayName("GET por gtin ERROR devuelve un unico producto en vez de lista [ERROR EN EL SERVICIO]")
        void getPorGtinError() {
            // No se valida usuario

            // Act
            ResponseEntity<Set<ProductoDTO>> resp = testRestTemplate.exchange(
                getRequest("/producto?gtin=" + prod.getGtin()),
                new ParameterizedTypeReference<Set<ProductoDTO>>() {}
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().size()).isEqualTo(1);
        }

        @Test @DisplayName("GET por gtin OK devuelve un unico producto [ERROR]")
        void getPorGtin() {
            // No se valida usuario

            // Act
            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(
                getRequest("/producto?gtin=" + prod.getGtin()),
                ProductoDTO.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA");
        }

        @Test @DisplayName("GET por idCuenta devuelve lista con 1 elemento")
        void getPorCuenta() {

            stubUsuarioAdmin();

            // Act
            ResponseEntity<Set<ProductoDTO>> resp = testRestTemplate.exchange(
                getRequest("/producto?idCuenta=1"),
                new ParameterizedTypeReference<Set<ProductoDTO>>() {}
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);

            mockServer.verify();
        }

        @Test @DisplayName("GET por idCategoria devuelve lista con 1 elemento")
        void getPorCategoria() {

            stubUsuarioAdmin();

            // Act
            ResponseEntity<Set<ProductoDTO>> resp = testRestTemplate.exchange(
                getRequest("/producto?idCategoria=" + cat.getId()),
                new ParameterizedTypeReference<Set<ProductoDTO>>() {}
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).hasSize(1);

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto devuelve 201 con DTO correcto")
        void crearProducto() {
            stubUsuarioAdmin();
            stubCuentaPlan(1000);

            // Crear la entrada del POST
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(req, ProductoDTO.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getHeaders().getLocation()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("NuevoProd");
            assertThat(resp.getBody().getGtin()).isEqualTo("NEW-GTIN");
            assertThat(resp.getBody().getTextoCorto()).isEqualTo("T1");
            assertThat(resp.getBody().getMiniatura()).isEqualTo("img.png");
            assertThat(resp.getBody().getCategorias())
                .extracting(CategoriaDTO::getNombre)
                .containsExactly("CatX");
            assertThat(resp.getBody().getRelaciones()).isEmpty();
            assertThat(resp.getBody().getAtributos()).isEmpty();
            // Comprobamos que se ha guardado en la BD
            assertThat(productoRepo.findById(resp.getBody().getId())).isPresent();
            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto devuelve 201 con DTO correcto, pero location error [ERROR EN EL CONTROLADOR]")
        //Creación erronea de la URI en el controlador
        void crearProductoLocationError() {
            stubUsuarioAdmin();
            stubCuentaPlan(1000);

            // Crear la entrada del POST
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(req, ProductoDTO.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            URI location = resp.getHeaders().getLocation();
            assertThat(location).isNotNull();
            assertThat(location.getPath()).isEqualTo("/producto/" + resp.getBody().getId()); 
            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto sin datos da 403")
        void crearProductoSinDatos() {
            
            stubUsuarioAdmin();
            
            // Arrange: entrada vacía
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto sin categoría devuelve 201 [ERROR EN EL SERVICIO]")
        // Deberia dejar crear un producto sin categoría, pero el servicio no lo permite
        void crearProductoSinCategoria() {

            stubUsuarioAdmin();

            // Arrange: entrada sin categoría
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            entrada.setCategorias(Collections.emptySet());
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto con GTIN existente devuelve 403")
        void crearProductoConGtinExistente() {

            stubUsuarioAdmin();

            // Arrange: crear un producto con el mismo GTIN
            Producto prod2 = new Producto();
            prod2.setGtin("GTIN-456");
            prod2.setSku("SKU-456");
            prod2.setNombre("ProdB");
            prod2.setCuentaId(prod.getCuentaId());
            prod2.setRelacionesOrigen(Collections.emptySet());
            prod2.setRelacionesDestino(Collections.emptySet());
            prod2.setAtributos(Collections.emptySet());
            productoRepo.save(prod2);

            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku("SKU-123");
            entrada.setNombre("ProdB");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto da 200 con los cambios aplicados")
        void actualizarProducto() {

            stubUsuarioAdmin();

            // Crear la entrada del PUT
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("GTIN-123");
            entrada.setSku("SKU-123");
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(req, ProductoDTO.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA-Edit");
            assertThat(resp.getBody().getGtin()).isEqualTo("GTIN-123");
            assertThat(resp.getBody().getTextoCorto()).isEqualTo("TE");
            assertThat(resp.getBody().getMiniatura()).isEqualTo("img2.png");
            assertThat(resp.getBody().getCategorias())
                .extracting(CategoriaDTO::getNombre)
                .containsExactly("CatX");
            assertThat(resp.getBody().getRelaciones()).isEmpty();
            assertThat(resp.getBody().getAtributos()).isEmpty();

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto con GTIN existente devuelve 403")
        void actualizarProductoConGtinExistente() {

            stubUsuarioAdmin();

            // Arrange: crear un segundo producto con el mismo GTIN
            Producto prod2 = new Producto();
            prod2.setGtin("GTIN-456");
            prod2.setSku("SKU-456");
            prod2.setNombre("ProdB");
            prod2.setCuentaId(prod.getCuentaId());
            prod2.setRelacionesOrigen(Collections.emptySet());
            prod2.setRelacionesDestino(Collections.emptySet());
            prod2.setAtributos(Collections.emptySet());
            productoRepo.save(prod2);

            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod2.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            // Comprobamos que el producto no se ha actualizado
            assertThat(productoRepo.findById(prod.getId()).get().getNombre()).isEqualTo("ProdA");

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto elimina relaciones obsoletas en ambos sentidos")
        void actualizarProductoEliminarRelaciones() {

            stubUsuarioAdmin();

            // Arrange: Creamos un producto destino y una relación entre ambos
            Producto dest = new Producto();
            dest.setGtin("GTIN-999");
            dest.setSku("SKU-999");
            dest.setNombre("ProdDestino");
            dest.setCuentaId(prod.getCuentaId());
            dest.setRelacionesOrigen(Collections.emptySet());
            dest.setRelacionesDestino(Collections.emptySet());
            dest.setAtributos(Collections.emptySet());
            productoRepo.save(dest);

            Relacion tipo = new Relacion();
            tipo.setNombre("TIPO");
            tipo.setCuentaId(prod.getCuentaId());
            relacionRepo.save(tipo);

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

            // Comprobamos que las relaciones existen
            assertThat(relacionProductoRepo.findByProductoOrigen(prod)).hasSize(1);
            assertThat(relacionProductoRepo.findByProductoOrigen(dest)).hasSize(1);

            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(req, ProductoDTO.class);

            // Assert: Comprobamos que las relaciones se han eliminado
            assertThat(relacionProductoRepo.findByProductoOrigen(prod)).isEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigen(dest)).isEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigenAndProductoDestino(prod, dest)).isEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigenAndProductoDestino(dest, prod)).isEmpty();

            // Comprobamos que el resto del producto se ha actualizado
            assertThat(productoRepo.findById(prod.getId()).get().getNombre()).isEqualTo("ProdA-Edit");
            assertThat(productoRepo.findById(prod.getId()).get().getTextoCorto()).isEqualTo("TE");
            assertThat(productoRepo.findById(prod.getId()).get().getMiniatura()).isEqualTo("img2.png");

            // Verificamos que la respuesta es correcta
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA-Edit");
            assertThat(resp.getBody().getRelaciones()).isEmpty();

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto añade nuevas relaciones en ambos sentidos")
        void actualizarProductoAgregarRelaciones() {

            stubUsuarioAdmin();
            // Arrange: Creamos un producto destino y una relación entre ambos
            Producto dest2 = new Producto();
            dest2.setGtin("GTIN-888");
            dest2.setSku("SKU-888");
            dest2.setNombre("OtroDestino");
            dest2.setCuentaId(prod.getCuentaId());
            dest2.setRelacionesOrigen(Collections.emptySet());
            dest2.setRelacionesDestino(Collections.emptySet());
            dest2.setAtributos(Collections.emptySet());
            productoRepo.save(dest2);

            Relacion tipo2 = new Relacion();
            tipo2.setNombre("TIPO2");
            tipo2.setCuentaId(prod.getCuentaId());
            relacionRepo.save(tipo2);

            assertThat(relacionProductoRepo.findByProductoOrigen(prod)).isEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigen(dest2)).isEmpty();

            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit2");
            entrada.setTextoCorto("TE2");
            entrada.setMiniatura("img3.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());

            RelacionProductoDTO relDto = new RelacionProductoDTO();
            relDto.setIdProductoDestino(dest2.getId());
            RelacionDTO rel = new RelacionDTO();
            rel.setId(tipo2.getId());
            rel.setNombre(tipo2.getNombre());
            relDto.setRelacion(rel);
            entrada.setRelaciones(Collections.singleton(relDto));

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(req, ProductoDTO.class);

            // Assert
            assertThat(relacionProductoRepo.findByProductoOrigen(prod))
                .extracting(r -> r.getProductoDestino().getId())
                .containsExactly(dest2.getId());
            assertThat(relacionProductoRepo.findByProductoOrigen(dest2))
                .extracting(r -> r.getProductoDestino().getId())
                .containsExactly(prod.getId());
            assertThat(relacionProductoRepo.findByProductoOrigenAndProductoDestino(prod, dest2)).isNotEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigenAndProductoDestino(dest2, prod)).isNotEmpty();

            // Comprobamos que el resto del producto se ha actualizado
            assertThat(productoRepo.findById(prod.getId()).get().getNombre()).isEqualTo("ProdA-Edit2");
            assertThat(productoRepo.findById(prod.getId()).get().getTextoCorto()).isEqualTo("TE2");
            assertThat(productoRepo.findById(prod.getId()).get().getMiniatura()).isEqualTo("img3.png");

            // Verificamos que la respuesta es correcta
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA-Edit2");
            assertThat(resp.getBody().getRelaciones()).hasSize(2);

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto elimina **solo** la relación indicada (asimétrico) [ERROR EN EL SERVICIO]")
        void actualizarProductoEliminaSoloR1() {
            // Creamos A->B con tipo R1 y B->A con tipo R2
            Producto destino = new Producto();
            destino.setGtin("GTIN-888");
            destino.setSku("SKU-888");
            destino.setNombre("OtroDestino");
            destino.setCuentaId(prod.getCuentaId());
            destino.setRelacionesOrigen(Collections.emptySet());
            destino.setRelacionesDestino(Collections.emptySet());
            destino.setAtributos(Collections.emptySet());
            productoRepo.save(destino);

            // A->B (R1)
            Relacion R1 = new Relacion(); 
            R1.setNombre("R1");
            R1.setCuentaId(prod.getCuentaId()); 
            relacionRepo.save(R1);

            // B->A (R2)
            Relacion R2 = new Relacion(); 
            R2.setNombre("R2");
            R2.setCuentaId(prod.getCuentaId()); 
            relacionRepo.save(R2);

            RelacionProducto relAB = new RelacionProducto();
            relAB.setProductoOrigen(prod);
            relAB.setProductoDestino(destino);
            relAB.setTipoRelacion(R1);
            relacionProductoRepo.save(relAB);

            RelacionProducto relBA = new RelacionProducto();
            relBA.setProductoOrigen(destino);
            relBA.setProductoDestino(prod);
            relBA.setTipoRelacion(R2);
            relacionProductoRepo.save(relBA);
            
            assertThat(relacionProductoRepo.findByProductoOrigen(prod)).extracting(r->r.getTipoRelacion().getNombre()).containsExactly("R1");
            assertThat(relacionProductoRepo.findByProductoOrigen(destino)).extracting(r->r.getTipoRelacion().getNombre()).containsExactly("R2");

            //Hacemos PUT dejando **solo R2** en el DTO
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre("CatX");
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());

            RelacionProductoDTO relDto = new RelacionProductoDTO();
            relDto.setIdProductoDestino(destino.getId());
            RelacionDTO rel = new RelacionDTO();
            rel.setId(R2.getId());
            rel.setNombre(R2.getNombre());
            relDto.setRelacion(rel);
            entrada.setRelaciones(Collections.singleton(relDto));

            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);
            testRestTemplate.exchange(req, ProductoDTO.class);

            //Esto debería dejar solo R2 (B->A), pero eliminará ambas:
            assertThat(relacionProductoRepo.findByProductoOrigen(prod))
                .extracting(r->r.getTipoRelacion().getNombre())
                .containsExactly("R2"); 
        }


        @Test @DisplayName("DELETE eliminarProducto devuelve 200 y sin entidad en BD")
        void eliminarProducto() {

            stubUsuarioAdmin();

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                deleteRequest("/producto/" + prod.getId()),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(productoRepo.findById(prod.getId())).isEmpty();

            mockServer.verify();
        }
    }

    

    @Nested
    @DisplayName("Cuando el usuario no pertenece a la cuenta del producto")
    class noPerteneceCuenta {

        private Categoria cat;
        private Producto prod;

        // Mock del servidor para simular el servicio de usuarios
        // Arrange general para los tests cuando el usuario no pertenece a la cuenta
        @BeforeEach
        void initMockAndDatos() {
            cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(1);

            prod = new Producto();
            prod.setGtin("GTIN-123");
            prod.setSku("SKU-123");
            prod.setNombre("ProdA");
            prod.setCuentaId(1);
            prod.getCategorias().add(cat);
            prod.setRelacionesOrigen(Collections.emptySet());
            prod.setRelacionesDestino(Collections.emptySet());
            prod.setAtributos(Collections.emptySet());
            productoRepo.save(prod);

            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
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

        @Test @DisplayName("GET por idProducto devuelve FORBIDDEN")
        void getPorId() {
            stubUsuarioPerteneceCuenta(prod.getCuentaId(), false);

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idProducto=" + prod.getId()),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("GET por idCategoria devuelve FORBIDDEN")
        void getPorCategoriaDevuelveForbidden() {
            stubUsuarioPerteneceCuenta(cat.getCuentaId(), false);

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCategoria=" + cat.getId()),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("GET por idCuenta devuelve FORBIDDEN")
        void getPorCuentaDevuelveForbidden() {
            // comprobamos con otra cuenta
            stubUsuarioPerteneceCuenta(2, false);

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCuenta=2"),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("GET por idCuenta inexistente devuelve 403")
        void getPorCuentaInexistenteDevuelve403() {
            // restauramos el mock para ADMIN
            stubUsuarioPerteneceCuenta(999, false);

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCuenta=999"),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto devuelve FORBIDDEN")
        void crearProductoDevuelveForbidden() {
            stubUsuarioPerteneceCuenta(cat.getCuentaId(), false);

            // Creamos la entrada del POST
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre(cat.getNombre());
            entrada.setCategorias(Set.of(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.post(endpoint(port, "/producto?idCuenta=" + cat.getCuentaId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            // Comprobamos que el producto no se ha creado
            assertThat(productoRepo.findByGtin("NEW-GTIN")).isEmpty();

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto con categoría de otra cuenta devuelve 403")
        void crearProductoConCategoriaOtraCuentaDa404() {
            stubUsuarioPerteneceCuenta(cat.getCuentaId(), true);
            
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            entrada.setCategorias(Collections.emptySet());
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            Categoria otra = new Categoria();
            otra.setNombre("X"); 
            otra.setCuentaId(10);
            categoriaRepo.save(otra);
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(otra.getId()); 
            catDto.setNombre("X");
            entrada.setCategorias(Set.of(catDto));

            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }


        @Test @DisplayName("PUT actualizarProducto devuelve FORBIDDEN")
        void actualizarProductoDevuelveForbidden() {
            stubUsuarioPerteneceCuenta(prod.getCuentaId(), false);

            // Creamos la entrada del PUT
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre(cat.getNombre());
            entrada.setCategorias(Set.of(catDto));
            entrada.setAtributos(Collections.emptySet());

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.put(endpoint(port, "/producto/" + prod.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(entrada),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            // Comprobamos que el producto no se ha actualizado
            assertThat(productoRepo.findById(prod.getId()).get().getNombre()).isEqualTo("ProdA");

            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarProducto devuelve FORBIDDEN")
        void eliminarProductoDevuelveForbidden() {
            stubUsuarioPerteneceCuenta(prod.getCuentaId(), false);

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                deleteRequest("/producto/" + prod.getId()),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            // Comprobamos que el producto no se ha eliminado
            assertThat(productoRepo.findById(prod.getId())).isPresent();

            mockServer.verify();
        }
    }

    
    @Nested
    @DisplayName("Cuando el usuario no puede crear producto")
    class UsuarioNoPuedeCrearProducto {

        private Categoria cat;

        // Mock del servidor para simular el servicio de usuarios
        // Arrange general para los tests cuando el usuario no puede crear producto
        @BeforeEach
        void initMockAndDatos() {
            cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(1);
            categoriaRepo.save(cat);

            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
        }

        private void stubUsuarioCliente() {
            // GET /usuario → CLIENTE
            URI root = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(requestTo(root))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
            // GET /usuario?id=1 → CLIENTE
            URI byId = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1).build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(byId))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
        }

        private void stubUsuarioAdmin() {
            // GET /usuario → ADMINISTRADOR
            URI root = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(requestTo(root))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]",
                        MediaType.APPLICATION_JSON
                    ));
        }

        private void stubUsuarioEmpty() {
            // GET /usuario?id=1 → []
            URI byId = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1).build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(byId))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
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

        private void stubCuentaPlan(int maxProductos) {
            URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", cat.getCuentaId())
                .build().toUri();
            mockServer.expect(requestTo(uri))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"plan\":{\"maxProductos\":" + maxProductos + "}}]",
                        MediaType.APPLICATION_JSON
                    ));
        }

        @Test @DisplayName("POST crearProducto devuelve FORBIDDEN por plan lleno")
        void crearProductoDevuelveForbiddenPorPlan() {
            // el usuario pertenece a la cuenta…
            stubUsuarioPerteneceCuenta(cat.getCuentaId(), true);
            // …pero su plan ya no admite productos
            stubCuentaPlan(0);

            // Creamos la entrada del POST
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("NEW-GTIN");
            entrada.setSku("SKU1");
            entrada.setNombre("NuevoProd");
            entrada.setTextoCorto("T1");
            entrada.setMiniatura("img.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre(cat.getNombre());
            entrada.setCategorias(Set.of(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=" + cat.getCuentaId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            // Comprobamos que el producto no se ha creado
            assertThat(productoRepo.findByGtin("NEW-GTIN")).isEmpty();

            mockServer.verify();
        }

        @Test @DisplayName("POST devuelve 403 si getUsuario(id) devuelve vacío")
        void crearProductoSinUsuarioValidoDa403() {
            // Reinicializamos el mock para simular que el usuario no existe
            mockServer.reset();
            // Simular que GET /usuario?id=1 retorna []
            stubUsuarioAdmin();
            stubUsuarioEmpty();

            // Creamos la entrada del POST
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin("X");
            entrada.setSku("Y");
            entrada.setNombre("Z");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre(cat.getNombre());
            entrada.setCategorias(Set.of(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=" + cat.getCuentaId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
            // Comprobamos que el producto no se ha creado
            assertThat(productoRepo.findByGtin("X")).isEmpty();

            mockServer.verify();
        }
    }


    @Nested
    @DisplayName("Cuando los productos no son accesibles por el usuario")
    class productoNoAccesible{
        
        // Mock del servidor para simular el servicio de usuarios
        @BeforeEach
        void initMockAndDatos() {
            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioCliente();
        }
        private void stubUsuarioCliente() {
            URI uriUsuarioRoot = UriComponentsBuilder.fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
            URI uriUsuarioById = UriComponentsBuilder.fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1).build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
        }
        private void stubUsuarioPerteneceCuenta(int cuentaId, boolean pertenece) {
            URI uruCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + cuentaId + "/usuarios")
                .build().toUri();
            String body = pertenece
                ? "[{\"id\":1,\"role\":\"CLIENTE\"}]"
                : "[]";
            mockServer.expect(requestTo(uruCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        }
        @Test @DisplayName("GET productos por categoría devuelve FORBIDDEN si tras filtrar no hay acceso a ningún producto")
        void getProductosPorCategoriaDevuelveForbiddenPorFiltro() {

            // Arrange:
            // 1) Creamos categoría y producto en BD
            Categoria cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(1);
            //cat = categoriaRepo.save(cat);
    
            Producto p = new Producto();
            p.setGtin("GTIN-AAA");
            p.setSku("SKU-AAA");
            p.setNombre("ProdCategoria");
            p.setCuentaId(1);
            p.getCategorias().add(cat);
            p.setRelacionesOrigen(Collections.emptySet());
            p.setRelacionesDestino(Collections.emptySet());
            p.setAtributos(Collections.emptySet());
            productoRepo.save(p);
    
            stubUsuarioPerteneceCuenta(1, true);
            stubUsuarioPerteneceCuenta(1, false);

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCategoria=" + cat.getId()),
                Void.class
            );
    
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("Token inválido")
    class tokenInvalido {

        @Test @DisplayName("GET por idProducto Token no valido devuelve FORBIDDEN")
        void getPorIdTokenNoValido() {
            // Arrange vacío
            
            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idProducto=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class);
            
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("GET por idCategoria Token no valido devuelve FORBIDDEN")
        void getPorCategoriaTokenNoValido() {
            // Arrange vacío
            
            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCategoria=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class);
            
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("GET por idCuenta Token no valido devuelve FORBIDDEN")
        void getPorCuentaTokenNoValido() {
            
            // Arrange vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class);
            
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("GET por GTTIN Token no valido devuelve FORBIDDEN")
        void getPorGtinTokenNoValido() {
            
            // Arrange vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?gtin=1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                Void.class);
            
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("POST crearProducto Token no valido devuelve FORBIDDEN")
        void crearProductoTokenNoValido() {
            
            // Arrange vacío

            // Creamos la entrada del POST
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody()).isNull();
        }

        @Test @DisplayName("PUT actualizarProducto Token no valido devuelve FORBIDDEN")
        void actualizarProductoTokenNoValido() {
            

            // Creamos la entrada del PUT
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/1"))
                .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test @DisplayName("DELETE eliminarProducto Token no valido devuelve FORBIDDEN")
        void eliminarProductoTokenNoValido() {
            //Assert vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity
                    .delete(endpoint(port, "/producto/1"))
                    .header("Authorization", "Bearer " + JWT_NO_VALIDO)
                    .build(),
                    Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Credenciales no válidas")
    class credencialesNoValidas {

        private Producto prod;
        private Categoria cat;

        // Mock del servidor para simular el servicio de usuarios
        // Arrange general para los tests cuando las credenciales no son válidas
        @BeforeEach
        void initMockAndDatos() {
            // Crear una categoría
            cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(1);


            // Crear un producto
            prod = new Producto();
            prod.setGtin("GTIN-123");
            prod.setSku("SKU-123");
            prod.setNombre("ProdA");
            prod.setCuentaId(1);
            prod.getCategorias().add(cat);
            prod.setRelacionesOrigen(Collections.emptySet());
            prod.setRelacionesDestino(Collections.emptySet());
            prod.setAtributos(Collections.emptySet());
            productoRepo.save(prod);

            mockServer = MockRestServiceServer.createServer(restTemplate);
            stubUsuarioNoValido();
        }

        private void stubUsuarioNoValido() {
            URI uriRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(requestTo(uriRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withUnauthorizedRequest());
        }

        @Test @DisplayName("GET por idProducto devuelve UNAUTHORIZED")
        void getPorIdCredencialesInvalidas() {
            // Arrange vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idProducto=" + prod.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            mockServer.verify();
        }

        @Test 
        @DisplayName("GET por idCategoria devuelve UNAUTHORIZED [ERROR EN EL SERVICIO]")
        //No hay ningun CredencialesNoValidasException en el servicio getProductosPorIdCategoria
        void getPorCategoriaCredencialesInvalidas() {
            // Arrange vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCategoria=1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            mockServer.verify();
        }
        
        @Test @DisplayName("GET por idCuenta devuelve UNAUTHORIZED")
        void getPorCuentaCredencialesInvalidas() {
            // Arrange vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?idCuenta=1"))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            mockServer.verify();
        }

        @Test 
        @DisplayName("GET por GTIN devuelve UNAUTHORIZED [ERROR EN EL SERVICIO]")
        //No hay ningun CredencialesNoValidasException en el servicio getProductosPorGtin
        void getPorGtinCredencialesInvalidas() {
            // Arrange vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity.get(endpoint(port, "/producto?gtin=" + prod.getGtin()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto devuelve UNAUTHORIZED")
        void crearProductoCredencialesInvalidas() {
            // Arrange vacío

            // Creamos la entrada del POST
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody()).isNull();
            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto devuelve UNAUTHORIZED")
        void actualizarProductoCredencialesInvalidas() {
            // Arrange vacío

            // Creamos la entrada del PUT
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarProducto devuelve UNAUTHORIZED")
        void eliminarProductoCredencialesInvalidas() {
            // Arrange vacío

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                RequestEntity
                    .delete(endpoint(port, "/producto/" + prod.getId()))
                    .header("Authorization", "Bearer " + JWT_ADMIN)
                    .build(),
                    Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            mockServer.verify();
        }
    }
}