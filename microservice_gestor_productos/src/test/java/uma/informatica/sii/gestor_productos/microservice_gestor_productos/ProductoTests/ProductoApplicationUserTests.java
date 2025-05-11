package uma.informatica.sii.gestor_productos.microservice_gestor_productos.ProductoTests;

/*@Nested
    @DisplayName("Cuando usuario no pertenece a cuenta")
    @SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
            "spring.main.allow-bean-definition-overriding=true",
        }
    )   
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    public class SinPermisos {
        @TestConfiguration
        static class StubsConfig {
            @Bean @Primary
            UsuarioService usuarioService() {
                return new UsuarioService(null, null) {
                    @Override
                    public boolean usuarioPerteneceACuenta(Integer idCuenta, Long idUsuario, String jwt) {
                        return false;
                    }
                };
            }

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
                ResponseEntity<ProductoDTO> resp = restTemplate.exchange(
                    RequestEntity.get(endpoint(port, "/producto?idProducto=" + prod.getId()))
                        .header(AUTH_HEADER, TOKEN).build(),
                    ProductoDTO.class);
                assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }

            @Test @DisplayName("GET por idCategoria → FORBIDDEN")
            void getPorCategoria() {
                ResponseEntity<Set<ProductoDTO>> resp = restTemplate.exchange(
                    RequestEntity.get(endpoint(port, "/producto?idCategoria=" + cat.getId()))
                        .header(AUTH_HEADER, TOKEN).build(),
                    new org.springframework.core.ParameterizedTypeReference<Set<ProductoDTO>>() {});
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

                ResponseEntity<ProductoDTO> resp = restTemplate.exchange(
                    RequestEntity.put(endpoint(port, "/producto/" + prod.getId()))
                        .header(AUTH_HEADER, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(entrada),
                    ProductoDTO.class);

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
    }*/