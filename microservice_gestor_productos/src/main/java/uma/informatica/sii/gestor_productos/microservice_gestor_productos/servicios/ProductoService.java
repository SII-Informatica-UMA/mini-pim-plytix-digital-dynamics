package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Atributo;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.CategoriaMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.ProductoMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.RelacionMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.RelacionProductoMapper;
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

    @Autowired
    public ProductoService(ProductoRepository productoRepository, UsuarioService usuarioService, CategoriaRepository categoriaRepository, ProductoMapper productoMapper, CuentaService cuentaService, RelacionRepository relacionRepository) {
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
    
        Integer idCuenta = productoExistente.getCuentaId();
    
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());
    
        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
        }
    
        return productoMapper.toDTO(productoExistente);
    }
    

    public List<Producto> getProductosPorIdCuenta(Integer idCuenta, String jwtToken) {
        Long usuarioId = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(CredencialesNoValidas::new);
        UsuarioDTO usuario = usuarioService.getUsuario(usuarioId, jwtToken)
                .orElseThrow(() -> new EntidadNoExistente());
    
        if (!usuario.getRole().equals(Usuario.Rol.ADMINISTRADOR)) {
            boolean pertenece = usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken);
            if (!pertenece) {
                throw new SinPermisosSuficientes();
            }
        }
        return productoRepository.findByCuentaId(idCuenta);
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
    

    public ProductoDTO actualizarProducto(Integer idProducto, ProductoDTO productoDTO, String jwtToken) {
        Long usuarioId = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(CredencialesNoValidas::new);
    
        UsuarioDTO usuario = usuarioService.getUsuario(usuarioId, jwtToken)
            .orElseThrow(() -> new EntidadNoExistente());
    
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

        Set<RelacionProducto> relaciones = productoDTO.getRelaciones().stream()
                .map(RelacionProductoMapper::toEntity)
                .collect(Collectors.toSet());

        producto.getRelacionesDestino().clear();
        producto.getRelacionesDestino().addAll(relaciones);

        // añadir los atributos
        Set<Atributo> atributos = productoDTO.getAtributos().stream()
                .map(AtributoMapper::toEntity)
                .collect(Collectors.toSet());
        producto.setAtributos(atributos);

    
        Producto actualizado = productoRepository.save(producto);
    
        return productoMapper.toDTO(actualizado);
    }
    

    public Producto crearProducto(ProductoDTO productoDTO, Integer idCuenta, String jwtToken) {
        // 1. Usuarios con acceso a la cuenta pueden crear productos. 
        // 2. El producto incluye las categorías y también las relaciones. 
        // Esta operación debe comprobar que no se exceden los límites fijados por el plan de la cuenta.
        Long usuarioId = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(CredencialesNoValidas::new);
    
        UsuarioDTO usuario = usuarioService.getUsuario(usuarioId, jwtToken)
            .orElseThrow(() -> new EntidadNoExistente());
        System.out.println("Usuario: ");
        if(!usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken)){
            System.out.println("Usuario no pertenece a la cuenta");
            throw new SinPermisosSuficientes();
        }
    
        Producto producto = productoMapper.toEntity(productoDTO);
        // comprobar que incluye categorías
        if (productoDTO.getCategorias() == null || productoDTO.getCategorias().isEmpty()) {
            throw new CredencialesNoValidas();
        }
        // comprobar que incluye relaciones
        if (productoDTO.getRelaciones() == null || productoDTO.getRelaciones().isEmpty()) {
            throw new CredencialesNoValidas();
        }
        // 	Sin permisos suficientes. También se puede dar este código si ya hay otro producto con el mismo GTIN.
        
        if (productoRepository.findByGtin(productoDTO.getGtin()).isPresent()) {
            System.out.println("El GTIN ya existe");
            throw new SinPermisosSuficientes();
        }
        // comprobar que no se exceden los límites fijados por el plan de la cuenta
        int productosActuales = productoRepository.findByCuentaId(idCuenta).size();
        if (!cuentaService.puedeCrearProducto(Long.valueOf(idCuenta), productosActuales)) {
            System.out.println("No se puede crear el producto");
            throw new SinPermisosSuficientes();
        }
        // crear el producto
        producto.setId(productoDTO.getId());
        producto.setGtin(productoDTO.getGtin());
        producto.setSku(productoDTO.getSku());
        producto.setNombre(productoDTO.getNombre());
        producto.setTextoCorto(productoDTO.getTextoCorto());
        producto.setMiniatura(productoDTO.getMiniatura());
        producto.setModificado(LocalDateTime.now());
        producto.setCuentaId(idCuenta);

        // añadir las categorías
        Set<Categoria> categorias = productoDTO.getCategorias().stream()
            .map(dto -> categoriaRepository.findById(dto.getId())
            .orElseThrow(() -> new EntidadNoExistente()))
            .collect(Collectors.toSet());
        System.out.println("Categorias");
        producto.setCategorias(categorias);
        // añadir las relaciones origen y destino
        for (RelacionProductoDTO relacionDTO : productoDTO.getRelaciones()) {
            Integer tipoRelacionId = relacionDTO.getRelacion().getId(); // o lo que uses en el DTO
                if (!relacionRepository.existsById(tipoRelacionId)) {
                    System.out.println("La relación no existe");
                    throw new EntidadNoExistente();
                }
        }
        Set<RelacionProducto> relaciones = productoDTO.getRelaciones().stream()
                .map(RelacionProductoMapper::toEntity)
                .collect(Collectors.toSet());
        producto.setRelacionesDestino(relaciones);
        
        // añadir los atributos
        Set<Atributo> atributos = productoDTO.getAtributos().stream()
                .map(AtributoMapper::toEntity)
                .collect(Collectors.toSet());
        producto.setAtributos(atributos);

        // guardar el producto
        Producto nuevoProducto = productoRepository.save(producto);
        // devolver el producto creado
        return nuevoProducto;
    }

    public void eliminarProducto(Integer id, String jwtToken) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isPresent()) {
            // Solo lo puede hacer un usuario que tenga acceso a la cuenta donde se encuentra el producto.
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
