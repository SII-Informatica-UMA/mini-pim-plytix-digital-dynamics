package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Obtener categorías de un producto
    @Query("SELECT c FROM Categoria c JOIN c.productos p WHERE p.id = :productoId AND p.cuentaId = c.cuentaId") // Ambos cuentaId deben coincidir, pero nos aseguramos de que el producto y categoria tengan la cuentaId correcta
    List<Categoria> findCategoriasByProductoId(Integer productoId);

    // Obtener categorías por cuentaId
    List<Categoria> findByCuentaId(Integer cuentaId);
    // Buscar categorías por id
    List<Categoria> findById(Integer id);
    //  Buscar categorías por nombre
    Optional<Categoria> findByNombre(String nombre);
    
    // Crear y modificar una categoría
    Categoria save(Categoria c);

    // Eliminar una categoría
    void deleteById(Integer id);

    //  Contar el total de categorías
    long count();
}
