package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
public interface RelacionProductoRepository extends JpaRepository<RelacionProducto, Integer>{
    List<RelacionProducto> findByProductoOrigen(Producto producto);
    
    // findByProductoOrigenAndProductoDestino
    Optional<RelacionProducto> findByProductoOrigenAndProductoDestino(
            Producto productoOrigen,
            Producto productoDestino
    );
}
