package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Atributo;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.ProductoMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.AtributoMapper;

@Service
public class ProductoService {

    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;
    
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;
    private final CuentaService cuentaService;
    private final RelacionRepository relacionRepository;
    private final RelacionProductoRepository relacionProductoRepository;
    public ProductoService(ProductoRepository productoRepository, 
    UsuarioService usuarioService, CategoriaRepository categoriaRepository, 
    ProductoMapper productoMapper, CuentaService cuentaService, 
    RelacionRepository relacionRepository,
    RelacionProductoRepository relacionProductoRepository) {
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
        this.cuentaService = cuentaService;
        this.relacionRepository = relacionRepository;
        this.relacionProductoRepository = relacionProductoRepository;
    }


    public ProductoDTO getProductoPorId(Integer idProducto, String jwtToken) {
        Optional<Producto> producto = productoRepository.findById(idProducto);
        if (producto.isEmpty()) {
            throw new EntidadNoExistente();
        }
        Producto productoExistente = producto.get();
        Integer idCuenta = productoExistente.getCuentaId();
    
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(() -> new CredencialesNoValidas());
    
        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
        }
    
        return productoMapper.toDTO(productoExistente);
    }
    
    public ProductoDTO getProductoPorGtin(String gtin, String jwtToken) {
        Optional<Producto> producto = productoRepository.findByGtin(gtin);
        if (producto.isEmpty()) {
            throw new EntidadNoExistente();
        }
        Producto productoExistente = producto.get();
        return productoMapper.toDTO(productoExistente);
    }
    

    public Set<ProductoDTO> getProductosPorIdCuenta(Integer idCuenta, String jwtToken) {
        Long usuarioId = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(CredencialesNoValidas::new);
        UsuarioDTO usuario = usuarioService.getUsuario(usuarioId, jwtToken)
                .orElseThrow(() -> new SinPermisosSuficientes());
    
        if (!usuario.getRole().equals(Usuario.Rol.ADMINISTRADOR)) {
            boolean pertenece = usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken);
            if (!pertenece) {
                throw new SinPermisosSuficientes();
            }
        }
        return productoRepository.findByCuentaId(idCuenta).stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toSet());
    }

    public Set<ProductoDTO> getProductosPorIdCategoria(Integer idcategoria,String jwtToken) {

        Optional<Categoria> categoria = categoriaRepository.findById(idcategoria);
        if (categoria.isEmpty()) {
            throw new EntidadNoExistente();
        }

        UsuarioDTO idUsuario = usuarioService.getUsuarioConectado(jwtToken).get();
        boolean idValido=usuarioService.usuarioPerteneceACuenta(categoria.get().getCuentaId(), idUsuario.getId(), jwtToken);
            if(!idValido) throw new SinPermisosSuficientes();

        Set<Producto> productos = productoRepository.findProductosByCategoriaId(idcategoria);
        if (productos.isEmpty()) {
            throw new EntidadNoExistente();
        }
        // Filtrar productos a los que el usuario tiene acceso
        Set<Producto> productosFiltrados = productos.stream()
                .filter(p -> usuarioService.usuarioPerteneceACuenta(p.getCuentaId(), idUsuario.getId(), jwtToken))
                .collect(Collectors.toSet());

        if (productosFiltrados.isEmpty()) {
            throw new SinPermisosSuficientes();
        }
        Set<ProductoDTO> productosDTO = productosFiltrados.stream()
        .map(productoMapper::toDTO)
        .collect(Collectors.toSet());
        return productosDTO;
    }
    

    public ProductoDTO actualizarProducto(Integer idProducto,
        ProductoEntradaDTO productoDTO, String jwtToken) {
        
        // 1) Validar token y usuario
        Long usuarioId = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(CredencialesNoValidas::new);
        usuarioService.getUsuario(usuarioId, jwtToken)
            .orElseThrow(EntidadNoExistente::new);

        // 2) Recuperar entidad
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(EntidadNoExistente::new);

        // 3) Comprobar permisos sobre la cuenta
        if (!usuarioService.usuarioPerteneceACuenta(
                producto.getCuentaId(), usuarioId, jwtToken)) {
            throw new SinPermisosSuficientes();
        }

        // 4) Validar unicidad de GTIN
        productoRepository.findByGtin(productoDTO.getGtin())
            .filter(p -> !p.getId().equals(idProducto))
            .ifPresent(p -> { throw new SinPermisosSuficientes(); });

        // 5) Actualizar campos básicos
        producto.setNombre(productoDTO.getNombre());
        producto.setTextoCorto(productoDTO.getTextoCorto());
        producto.setMiniatura(productoDTO.getMiniatura());
        producto.setModificado(OffsetDateTime.now());

        // 6) Actualizar categorías
        Set<Categoria> categorias = productoDTO.getCategorias().stream()
            .map(dto -> categoriaRepository.findById(dto.getId())
                .orElseThrow(EntidadNoExistente::new))
            .collect(Collectors.toSet());
        producto.getCategorias().clear();
        producto.getCategorias().addAll(categorias);

        // 7) Eliminar relaciones obsoletas y añadir nuevas, en parejo
        // Destinos que el cliente quiere mantener
        Set<Integer> destinosNuevos = productoDTO.getRelaciones() != null
            ? productoDTO.getRelaciones().stream()
                .map(RelacionProductoDTO::getIdProductoDestino)
                .collect(Collectors.toSet())
            : Collections.emptySet();

        // Relaciones actuales A→X
        List<RelacionProducto> actuales = relacionProductoRepository.findByProductoOrigen(producto);

        // 7a) Borrar las que ya no están en destinosNuevos (tanto A→B como B→A)
        for (RelacionProducto relActual : actuales) {
            Integer destId = relActual.getProductoDestino().getId();
            if (!destinosNuevos.contains(destId)) {
                // A→B
                relacionProductoRepository.delete(relActual);
                // B→A
                relacionProductoRepository
                    .findByProductoOrigenAndProductoDestino(
                        productoRepository.getReferenceById(destId), producto)
                    .ifPresent(relacionProductoRepository::delete);
            }
        }

        // 7b) Crear las nuevas en ambos sentidos
        if (productoDTO.getRelaciones() != null) {
            for (RelacionProductoDTO dto : productoDTO.getRelaciones()) {
                Integer destId = dto.getIdProductoDestino();
                if (destId.equals(producto.getId())) continue; // skip self

                boolean existe = actuales.stream()
                    .anyMatch(r -> r.getProductoDestino().getId().equals(destId));
                if (!existe) {
                    // recuperar destino y tipo de relación
                    Producto destino = productoRepository.findById(destId)
                        .orElseThrow(EntidadNoExistente::new);
                    Relacion tipo = relacionRepository.findById(dto.getRelacion().getId())
                        .orElseThrow(EntidadNoExistente::new);

                    // A→B
                    RelacionProducto relAB = new RelacionProducto();
                    relAB.setProductoOrigen(producto);
                    relAB.setProductoDestino(destino);
                    relAB.setTipoRelacion(tipo);
                    relacionProductoRepository.save(relAB);

                    // B→A
                    RelacionProducto relBA = new RelacionProducto();
                    relBA.setProductoOrigen(destino);
                    relBA.setProductoDestino(producto);
                    relBA.setTipoRelacion(tipo);
                    relacionProductoRepository.save(relBA);
                }
            }
        }

        // 8) Actualizar atributos
        Set<Atributo> atributos = productoDTO.getAtributos().stream()
            .map(AtributoMapper::toEntity)
            .collect(Collectors.toSet());
        producto.setAtributos(atributos);

        // 9) Guardar y devolver DTO
        Producto guardado = productoRepository.save(producto);
        return productoMapper.toDTO(guardado);
    }
    

    public ProductoDTO crearProducto(ProductoEntradaDTO productoDTO, Integer idCuenta, String jwtToken) {
        // comprobar que el usuario tiene permisos para crear el producto
        Long usuarioId = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(CredencialesNoValidas::new);
    
        UsuarioDTO usuario = usuarioService.getUsuario(usuarioId, jwtToken)
            .orElseThrow(() -> new SinPermisosSuficientes());
        System.out.println("Usuario: ");
        if(!usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken)){
            System.out.println("Usuario no pertenece a la cuenta");
            throw new SinPermisosSuficientes();
        }
    
        Producto producto = productoMapper.toEntityEntrada(productoDTO);
        // comprobar que incluye categorías
        if (productoDTO.getCategorias() == null || productoDTO.getCategorias().isEmpty()) {
            throw new EntidadNoExistente();
        }
        
        // 	Sin permisos suficientes si ya hay otro producto con el mismo GTIN.
        if (productoRepository.findByGtin(productoDTO.getGtin()).isPresent()) {
            System.out.println("El GTIN ya existe");
            throw new SinPermisosSuficientes();
        }
        // comprobar que no se exceden los límites fijados por el plan de la cuenta
        int productosActuales = productoRepository.findByCuentaId(idCuenta).size();
        if (!cuentaService.puedeCrearProducto(Long.valueOf(idCuenta), productosActuales, usuario)) {
            System.out.println("No se puede crear el producto");
            throw new SinPermisosSuficientes();
        }
        // crear el producto
        producto.setGtin(productoDTO.getGtin());
        producto.setSku(productoDTO.getSku());
        producto.setNombre(productoDTO.getNombre());
        producto.setTextoCorto(productoDTO.getTextoCorto());
        producto.setMiniatura(productoDTO.getMiniatura());
        producto.setModificado(null); // no modificado al crear
        producto.setCuentaId(idCuenta);

        //añadir las categorías
        Set<Categoria> categorias = productoDTO.getCategorias().stream()
                .map(dto -> categoriaRepository.findById(dto.getId())
                        .orElseThrow(() -> new EntidadNoExistente()))
                .collect(Collectors.toSet());
        producto.setCategorias(categorias);

        producto.setRelacionesOrigen(Collections.emptySet());
        producto.setRelacionesDestino(Collections.emptySet());

        
        // añadir los atributos
        Set<Atributo> atributos = productoDTO.getAtributos().stream()
                .map(AtributoMapper::toEntity)
                .collect(Collectors.toSet());
        producto.setAtributos(atributos);
        
        Producto nuevoProducto = productoRepository.save(producto);
        return productoMapper.toDTO(nuevoProducto);
    }

    public void eliminarProducto(Integer id, String jwtToken) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isPresent()) {
            UsuarioDTO usuario = usuarioService.getUsuarioConectado(jwtToken)
                    .orElseThrow(CredencialesNoValidas::new);
            Integer idCuenta = productoOptional.get().getCuentaId();
            if(!usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken)){
                throw new SinPermisosSuficientes();
            }
            productoRepository.deleteById(id);
        } else {
            throw new EntidadNoExistente();
        }
    }
}
