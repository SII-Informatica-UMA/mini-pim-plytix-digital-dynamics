package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.ProductoRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.*;
@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }
    public List<Producto> buscarProductos(Integer idProducto, Integer idCuenta, Integer idCategoria, String gtin) {
        List<Producto> resultado = new ArrayList<>();

        if (idProducto != null) {
            Optional<Producto> optionalProducto = productoRepository.findById(idProducto);
            if (optionalProducto.isEmpty()) {
                throw new EntidadNoExistente();
            }
            Producto producto = optionalProducto.get();
            // Si se pasó idCuenta, validamos que el producto pertenezca a esa cuenta
            if (idCuenta != null && !producto.getCuentaId().equals(idCuenta)) {
                throw new SinPermisosSuficientes();
            }
            resultado.add(producto);
            return resultado;
        }

        if (gtin != null && !gtin.isBlank()) {
            resultado = productoRepository.findByGtin(gtin);
        }
        
        if (idCategoria != null) {
            List<Producto> porCategoria = productoRepository.findProductosByCategoriaId(idCategoria);
            if (idCuenta != null) {
                porCategoria = porCategoria.stream()
                    .filter(p -> p.getCuentaId().equals(idCuenta))
                    .collect(Collectors.toList());
            }
            resultado = porCategoria;
        }

        if (idCuenta != null && resultado.isEmpty()) {
            resultado = productoRepository.findByCuentaId(idCuenta);
        }
        if (resultado.isEmpty()) {
            resultado = productoRepository.findAll();
        }
        return resultado;
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

    public Producto crearProducto(Producto producto, Integer cuentaId) {;
        producto.setCuentaId(cuentaId);
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Integer id) {
        // Verificar si el producto existe antes de eliminarlo
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isPresent()) {
            productoRepository.deleteById(id);
        } else {
            throw new EntidadNoExistente();
        }
    }

}
