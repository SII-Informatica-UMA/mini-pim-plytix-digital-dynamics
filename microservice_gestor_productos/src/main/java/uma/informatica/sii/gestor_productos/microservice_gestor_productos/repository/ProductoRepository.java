package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;


public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //Obtener Producto por id
    Optional<Producto> findById(Integer id);
    // Seleccionar Producto por cuentaId
    List<Producto> findByCuentaId(Integer cuentaId);
    // Obtener productos de una categoría
    @Query("SELECT p FROM Producto p JOIN p.categorias c WHERE c.id = :categoriaId AND p.cuentaId = c.cuentaId") // Ambos cuentaId deben coincidir, pero nos aseguramos de que el producto y categoria tengan la cuentaId correcta
    List<Producto> findProductosByCategoriaId(Integer categoriaId);
    // Seleccionar Producto por gtin 
    List<Producto> findByGtin(String gtin);


    // Obtener los atributos de un producto
    @Query("SELECT p.atributos FROM Producto p WHERE p.id = :productoId")
    List<Producto> findAtributosByProductoId(Integer productoId);

	//Crear Producto y modificarlo
    <S extends Producto> S save(S producto);
    
	//Eliminar Producto por id
    void deleteById(Integer id);

} 
