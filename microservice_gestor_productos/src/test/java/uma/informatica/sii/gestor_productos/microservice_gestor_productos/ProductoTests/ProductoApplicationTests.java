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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Tests de Productos- ")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductoApplicationTests {

    public static final String JWT_ADMIN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiQURNSU5JU1RSQURPUiIsInN1YiI6IjEiLCJpYXQiOjE3NDQ5MTQ3MDQsImV4cCI6MTgwNzk4NjcwNH0.YIXpA6aXXJ6q8tKjAAnVKT_uumuTdbhkLVieaCGf4vFtOMcYoNOH-FarolDduIQ3ulN-Gxy4TWBymK3ypZ38bQ";
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
    }


    @Nested 
    @DisplayName("Con productos existentes")
    class ConProductos {

        private Categoria cat;
        private Producto prod;

        @BeforeEach
        void datos() {

            //Arrange general para los tests cuando hay productos
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

        @Test @DisplayName("GET por idProducto da OK con DTO correcto")
        void getPorId() {
            // 1) getUsuarioConectado(), devolvemos un ADMINISTRADOR
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();

            mockServer
            .expect(requestTo(uriUsuarioRoot))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]",
                MediaType.APPLICATION_JSON
                )
            );

            // 2) stub para getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();

            mockServer
            .expect(requestTo(uriUsuarioById))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]",
                MediaType.APPLICATION_JSON
                )
            );

            // Act
            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(getRequest("/producto?idProducto=" + prod.getId()),
                ProductoDTO.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA");
            assertThat(resp.getBody().getGtin()).isEqualTo("GTIN-123");
            assertThat(resp.getBody().getCategorias())
                .extracting(CategoriaDTO::getNombre)
                .containsExactly("CatX");

            mockServer.verify();
        }

        @Test @DisplayName("GET por gtin da OK con DTO correcto")
        void getPorGtin() {
            // Act
            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(
                getRequest("/producto?gtin=" + prod.getGtin()),
                ProductoDTO.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getId()).isEqualTo(prod.getId());
        }

        @Test @DisplayName("GET por idCuenta devuelve lista con 1 elemento")
        void getPorCuenta() {

            // 1) getUsuarioConectado()
                URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();

            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();

            mockServer.expect(requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

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

            // 1) getUsuarioConectado()
                URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]",
                        MediaType.APPLICATION_JSON
                    ));

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

            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) getCuentaPorId (para comprobar límites de plan)
            URI uriCuenta = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", prod.getCuentaId())
                .build().toUri();
            mockServer.expect(requestTo(uriCuenta))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"plan\":{\"maxProductos\":1000}}]",
                        MediaType.APPLICATION_JSON
                    ));

            // Arrange de la entrada del POST
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

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(
                req,
                ProductoDTO.class
            );
            
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resp.getHeaders().getLocation()).isNotNull();
            assertThat(resp.getBody().getNombre()).isEqualTo("NuevoProd");

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto sin categoría da 404")
        void crearProductoSinCategoria() {

            // Sólo necesitamos validar al usuario (no llegamos a llamar a CuentaService)
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Arrange 
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

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                req,
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto con GTIN existente devuelve 403")
        void crearProductoConGtinExistente() {

            // Usuario ok
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));
            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Arrange: creamos un producto con el GTIN
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

            // Act: intentamos crear otro producto con el mismo GTIN
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=1"))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(
                req,
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto da 200 con los cambios aplicados")
        void actualizarProducto() {

            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));
            // 2) getUsuario(id)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Arrange
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

            // Act
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<ProductoDTO> resp = testRestTemplate.exchange(
                req,
                ProductoDTO.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody().getNombre()).isEqualTo("ProdA-Edit");

            mockServer.verify();
        }

        @Test
        @DisplayName("PUT actualizarProducto elimina relaciones obsoletas en ambos sentidos")
        void actualizarProductoEliminarRelaciones() {

            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));
            // 2) getUsuario(id)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Arrange
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
            tipo.setCuentaId(prod.getCuentaId());
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
            assertThat(relacionProductoRepo.findByProductoOrigen(dest)).hasSize(1);

            // 3) lanza la petición de actualización con DTO.relaciones vacío
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit");
            entrada.setTextoCorto("TE");
            entrada.setMiniatura("img2.png");
            
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre(cat.getNombre());
            catDto.setId(cat.getId());
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());
            entrada.setRelaciones(Collections.emptySet());

            // Act: envía el PUT con DTO.relaciones vacío
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            testRestTemplate.exchange(req, ProductoDTO.class);

            // Assert: comprueba que se eliminaron A->dest y dest->A
            assertThat(relacionProductoRepo.findByProductoOrigen(prod)).isEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigen(dest)).isEmpty();

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto añade nuevas relaciones en ambos sentidos")
        void actualizarProductoAgregarRelaciones() {

            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));
            // 2) getUsuario(id)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Arrange
            // 1) crea un segundo producto y un tipo de relación
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

            // sanity check: al principio no hay ninguna relación
            assertThat(relacionProductoRepo.findByProductoOrigen(prod)).isEmpty();
            assertThat(relacionProductoRepo.findByProductoOrigen(dest2)).isEmpty();

            // 2) envía el PUT con DTO.relaciones conteniendo dest2
            ProductoEntradaDTO entrada = new ProductoEntradaDTO();
            entrada.setGtin(prod.getGtin());
            entrada.setSku(prod.getSku());
            entrada.setNombre("ProdA-Edit2");
            entrada.setTextoCorto("TE2");
            entrada.setMiniatura("img3.png");
            CategoriaDTO catDto = new CategoriaDTO();
            catDto.setId(cat.getId());
            catDto.setNombre(cat.getNombre());
            entrada.setCategorias(Collections.singleton(catDto));
            entrada.setAtributos(Collections.emptySet());

            // construye el DTO de relación
            RelacionProductoDTO relDto = new RelacionProductoDTO();
            relDto.setIdProductoDestino(dest2.getId());
            RelacionDTO rel = new RelacionDTO();
            rel.setId(tipo2.getId());
            rel.setNombre(tipo2.getNombre());
            relDto.setRelacion(rel);
            entrada.setRelaciones(Collections.singleton(relDto));

            // Act: envía el PUT con DTO.relaciones conteniendo dest2
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            testRestTemplate.exchange(req, ProductoDTO.class);

            // Assert: comprueba que se creó A->dest2 y dest2->A
            assertThat(relacionProductoRepo.findByProductoOrigen(prod))
                .extracting(r -> r.getProductoDestino().getId())
                .containsExactly(dest2.getId());

            assertThat(relacionProductoRepo.findByProductoOrigen(dest2))
                .extracting(r -> r.getProductoDestino().getId())
                .containsExactly(prod.getId());

            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarProducto devuelve 200 y sin entidad en BD")
        void eliminarProducto() {

            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));
            // 2) getUsuario(id)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"ADMINISTRADOR\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

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
    @DisplayName("usuario no pertenece a cuenta")
    class noPeteneceCuenta {

        private Categoria cat;
        private Producto prod;

        // Arrange común para los tests de este grupo
        @BeforeEach
        void initMockAndDatos(){
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

        @Test @DisplayName("GET por idProducto devuelve FORBIDDEN")
        void getPorId() {

            // 1) getUsuarioConectado(), devolvemos un CLIENTE
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1), también CLIENTE
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) llamada a /cuenta/{idCuenta}/usuarios
            //    como prod.getCuentaId()==1, montamos /cuenta/1/usuarios
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + prod.getCuentaId() + "/usuarios")
                .build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    // devolvemos un array que NO contiene al usuario 1
                    .andRespond(withSuccess(
                        "[{\"id\":2,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(getRequest("/producto?idProducto=" + prod.getId()),
                Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("GET por idCategoria devuelve FORBIDDEN")
        void getPorCategoriaDevuelveForbidden() {
            // 1) getUsuarioConectado(), CLIENTE
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1), CLIENTE (múltiples veces si hace falta)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) usuarioPerteneceACuenta, GET /cuenta/{cat.getCuentaId()}/usuarios
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + cat.getCuentaId() + "/usuarios")
                .build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    // devolvemos un cliente distinto para simular “no pertenece”
                    .andRespond(withSuccess(
                        "[{\"id\":2,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Act: llamada a /producto?idCategoria=cat.getId()
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
            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) usuarioPerteneceACuenta, GET /cuenta/{idCuenta}/usuarios
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/2/usuarios")
                .build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":2,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Act
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCuenta=2"),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("POST crearProducto devuelve FORBIDDEN")
        void crearProductoDevuelveForbidden() {
            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) usuarioPerteneceACuenta, GET /cuenta/{idCuenta}/usuarios
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + cat.getId() + "/usuarios")
                .build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":2,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 4) Arrange para la entrada del POST
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

            // Act: intentamos crear un producto
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=" + cat.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("PUT actualizarProducto devuelve FORBIDDEN")
        void actualizarProductoDevuelveForbidden() {
            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) usuarioPerteneceACuenta, GET /cuenta/{prod.getCuentaId()}/usuarios
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + prod.getCuentaId() + "/usuarios")
                .build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":2,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 4) Arrange para preparar entrada
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

            // Act: envía el PUT con DTO.relaciones vacío
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .put(endpoint(port, "/producto/" + prod.getId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);
            
            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);
            
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }

        @Test @DisplayName("DELETE eliminarProducto devuelve FORBIDDEN")
        void eliminarProductoDevuelveForbidden() {
            // 1) getUsuarioConectado()
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) usuarioPerteneceACuenta, GET /cuenta/{prod.getCuentaId()}/usuarios
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + prod.getCuentaId() + "/usuarios")
                .build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":2,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // Act: envía el DELETE
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                deleteRequest("/producto/" + prod.getId()),
                Void.class
            );

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("Usuario no puede crear producto")
    class UsuarioNoPuedeCrearProducto {
    
        private Categoria cat;
        
        // Arrange común para los tests de este grupo
        @BeforeEach
        void initMockAndDatos() {
            cat = new Categoria();
            cat.setNombre("CatX");
            cat.setCuentaId(1);
            categoriaRepo.save(cat);
    
            mockServer = MockRestServiceServer.createServer(restTemplate);
        }
    
        @Test @DisplayName("POST crearProducto devuelve FORBIDDEN por plan lleno")
        void crearProductoDevuelveForbiddenPorPlan() {
            // 1) getUsuarioConectado(), CLIENTE
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));

            // 2) getUsuario(id=1), CLIENTE (múltiples veces)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 3) usuarioPerteneceACuenta, pertenencia verdadera
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/" + cat.getCuentaId() + "/usuarios")
                .build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]", 
                        MediaType.APPLICATION_JSON
                    ));

            // 4) getCuentaPorId, plan con maxProductos = 0
            URI uriCuenta = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta")
                .queryParam("idCuenta", cat.getCuentaId())
                .build().toUri();
            mockServer.expect(requestTo(uriCuenta))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"plan\":{\"maxProductos\":0}}]",
                        MediaType.APPLICATION_JSON
                    ));

            // Arrange para la entrada del POST
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

            // Act: intentamos crear un producto
            RequestEntity<ProductoEntradaDTO> req = RequestEntity
                .post(endpoint(port, "/producto?idCuenta=" + cat.getCuentaId()))
                .header("Authorization", "Bearer " + JWT_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(entrada);

            ResponseEntity<Void> resp = testRestTemplate.exchange(req, Void.class);
            
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("los productos no son accesibles por el usuario")
    class productoNoAccesible{
    
        @BeforeEach
        void initMockAndDatos() {

            mockServer = MockRestServiceServer.createServer(restTemplate);
        }
        @Test @DisplayName("GET productos por categoría → FORBIDDEN si tras filtrar no hay acceso a ningún producto")
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
    
            // Montamos el MockRestServiceServer
            mockServer = MockRestServiceServer.createServer(restTemplate);
    
            // 2) getUsuarioConectado(), CLIENTE
            URI uriUsuarioRoot = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario").build().toUri();
            mockServer.expect(requestTo(uriUsuarioRoot))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
    
            // 3) getUsuario(id=1), CLIENTE (varias veces)
            URI uriUsuarioById = UriComponentsBuilder
                .fromUriString(baseUrl + "/usuario")
                .queryParam("id", 1)
                .build().toUri();
            mockServer.expect(ExpectedCount.manyTimes(), requestTo(uriUsuarioById))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
    
            // 4) Initial usuarioPerteneceACuenta para categoría, devuelve OK (contiene usuario 1)
            URI uriCuentaUsuarios = UriComponentsBuilder
                .fromUriString(baseUrl + "/cuenta/1/usuarios").build().toUri();
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[{\"id\":1,\"role\":\"CLIENTE\"}]",
                        MediaType.APPLICATION_JSON
                    ));
    
            // 5) Segunda llamada a usuarioPerteneceACuenta dentro del filter, devuelve []
            mockServer.expect(requestTo(uriCuentaUsuarios))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON
                    ));
    
            // Act: llamada a /producto?idCategoria=cat.getId()
            ResponseEntity<Void> resp = testRestTemplate.exchange(
                getRequest("/producto?idCategoria=" + cat.getId()),
                Void.class
            );
    
            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    
            mockServer.verify();
        }
    }
}
