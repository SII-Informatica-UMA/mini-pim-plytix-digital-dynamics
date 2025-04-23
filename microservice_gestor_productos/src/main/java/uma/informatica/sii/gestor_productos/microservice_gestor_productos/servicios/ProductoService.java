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
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.ProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.CategoriaMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.ProductoMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.RelacionMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.RelacionProductoMapper;
@Service
public class ProductoService {

    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;
    
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;

    @Autowired
    public ProductoService(ProductoRepository productoRepository, UsuarioService usuarioService, CategoriaRepository categoriaRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
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
                .map(CategoriaMapper::toEntity) 
                .collect(Collectors.toSet());
        producto.setCategorias(categorias);

        Set<RelacionProducto> relaciones = productoDTO.getRelaciones().stream()
                .map(RelacionProductoMapper::toEntity)
                .collect(Collectors.toSet());

        producto.setRelacionesDestino(relaciones);

    
        Producto actualizado = productoRepository.save(producto);
    
        return productoMapper.toDTO(actualizado);
    }
    

    public Producto crearProducto(Producto producto, Integer idCuenta) {
        producto.setCuentaId(1);
        Set<Categoria> categoriasNuevas = new HashSet<>();
        if (producto.getCategorias() != null) {
            for (Categoria cat : producto.getCategorias()) {
                Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(cat.getNombre());
                if (categoriaExistente.isPresent()) {
                    throw new IllegalArgumentException("La categoría '" + cat.getNombre() + "' ya existe.");
                } else {
                    Categoria nuevaCategoria = new Categoria();
                    nuevaCategoria.setNombre(cat.getNombre());
                    nuevaCategoria.setCuentaId(1);
                    Categoria categoriaGuardada = categoriaRepository.save(nuevaCategoria);
                    categoriasNuevas.add(categoriaGuardada);
                }
            }
        }
        producto.setCategorias(categoriasNuevas);
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Integer id) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isPresent()) {
            productoRepository.deleteById(id);
        } else {
            throw new EntidadNoExistente();
        }
    }

}
