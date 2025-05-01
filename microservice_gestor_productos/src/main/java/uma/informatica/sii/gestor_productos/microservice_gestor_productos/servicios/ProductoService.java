package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoEntradaDTO;
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

    public ProductoService(ProductoRepository productoRepository, 
    UsuarioService usuarioService, CategoriaRepository categoriaRepository, 
    ProductoMapper productoMapper, CuentaService cuentaService, 
    RelacionRepository relacionRepository) {
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
        this.cuentaService = cuentaService;
        this.relacionRepository = relacionRepository;
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

    public List<Producto> getProductosPorIdCategoria(Integer idCategoria, String jwtToken) {
        List<Producto> productos = productoRepository.findProductosByCategoriaId(idCategoria);
        if (productos.isEmpty()) {
            throw new EntidadNoExistente();
        }
        
        Integer idCuenta = productos.get(0).getCuentaId();
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());
        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
        }
        return productos;
    }
    

    public ProductoDTO actualizarProducto(Integer idProducto,
        ProductoEntradaDTO productoDTO, String jwtToken) {
        Long usuarioId = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(CredencialesNoValidas::new);
    
        UsuarioDTO usuario = usuarioService.getUsuario(usuarioId, jwtToken)
            .orElseThrow(() -> new SinPermisosSuficientes());
    
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new EntidadNoExistente());
    
        if(!usuarioService.usuarioPerteneceACuenta(producto.getCuentaId(), usuario.getId(), jwtToken)){
            throw new SinPermisosSuficientes();
        }
    
        producto.setNombre(productoDTO.getNombre());
        producto.setTextoCorto(productoDTO.getTextoCorto());
        producto.setMiniatura(productoDTO.getMiniatura());
        producto.setModificado(LocalDateTime.now());
        
        Set<Categoria> categorias = productoDTO.getCategorias().stream()
        .map(dto -> categoriaRepository.findById(dto.getId())
            .orElseThrow(() -> new EntidadNoExistente()))
            .collect(Collectors.toSet());
        producto.getCategorias().clear();
        producto.setCategorias(categorias);

        if (productoDTO.getRelaciones() != null && !productoDTO.getRelaciones().isEmpty()) {
            Set<RelacionProducto> relaciones = productoDTO.getRelaciones().stream()
                .map(dto -> {
                    RelacionProducto rel = new RelacionProducto();
    
                    Relacion tipoRelacion = relacionRepository.findById(dto.getRelacion().getId())
                        .orElseThrow(() -> new EntidadNoExistente());
                    rel.setTipoRelacion(tipoRelacion);
    
                    rel.setProductoOrigen(producto);
    
                    Producto destino = productoRepository.findById(dto.getIdProductoDestino())
                        .orElseThrow(() -> new EntidadNoExistente());
                    rel.setProductoDestino(destino);
    
                    return rel;
                })
                .collect(Collectors.toSet());
    
            producto.getRelacionesOrigen().clear();
            producto.getRelacionesOrigen().addAll(relaciones);
        }

        // añadir los atributos
        Set<Atributo> atributos = productoDTO.getAtributos().stream()
                .map(AtributoMapper::toEntity)
                .collect(Collectors.toSet());
        producto.setAtributos(atributos);
        Producto actualizado = productoRepository.save(producto);
        return productoMapper.toDTO(actualizado);
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
