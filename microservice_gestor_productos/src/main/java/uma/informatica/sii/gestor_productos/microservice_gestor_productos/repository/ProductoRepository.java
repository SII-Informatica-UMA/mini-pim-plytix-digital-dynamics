package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.*;


public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Seleccionar Producto por cuentaId
    Set<Producto> findByCuentaId(Integer cuentaId);
    // Obtener productos de una categoría
    @Query("SELECT p FROM Producto p JOIN p.categorias c WHERE c.id = :categoriaId") // Ambos cuentaId deben coincidir, pero nos aseguramos de que el producto y categoria tengan la cuentaId correcta
    Set<Producto> findProductosByCategoriaId(Integer categoriaId);
    // Seleccionar Producto por gtin 
    Optional<Producto> findByGtin(String gtin);
} 
