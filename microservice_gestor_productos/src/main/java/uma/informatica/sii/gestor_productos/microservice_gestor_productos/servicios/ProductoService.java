package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;


import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.*;
@Service
public class ProductoService {

    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;
    
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, UsuarioService usuarioService, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
    }

    public Producto getProductoPorId(Integer idProducto, String jwtToken) {
        Optional<Producto> producto = productoRepository.findById(idProducto);
        if (producto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        Producto productoExistente = producto.get();
        Integer idCuenta = productoExistente.getCuentaId();
    
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    
        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    
        return productoExistente;
    }
    

    public Producto getProductoPorGtin(String gtin, String jwtToken) {
        List<Producto> productos = productoRepository.findByGtin(gtin);
    
        if (productos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
    
        Producto producto = productos.get(0);
    
        Integer idCuenta = producto.getCuentaId();
    
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    
        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    
        return producto;
    }
    

    public List<Producto> getProductosPorIdCuenta(Integer idCuenta, String jwtToken) {
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }

        return productoRepository.findByCuentaId(idCuenta);
    }

    public List<Producto> getProductosPorIdCategoria(Integer idCategoria, String jwtToken) {
        List<Producto> productos = productoRepository.findProductosByCategoriaId(idCategoria);
    
        if (productos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría sin productos");
        }
        
        Integer idCuenta = productos.get(0).getCuentaId();
    
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    
        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    
        return productos;
    }
    

    public Producto modificarProducto(Producto producto) {
        if (producto.getId() == null) {
            throw new EntidadNoExistente();
        }
        Optional<Producto> optionalProducto = productoRepository.findById(producto.getId());
        if (optionalProducto.isEmpty()) {
            throw new EntidadNoExistente();
        }
        Producto productoExistente = optionalProducto.get();
        if (!producto.getCuentaId().equals(productoExistente.getCuentaId())) {
            throw new SinPermisosSuficientes();
        }
        return productoRepository.save(producto);
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
