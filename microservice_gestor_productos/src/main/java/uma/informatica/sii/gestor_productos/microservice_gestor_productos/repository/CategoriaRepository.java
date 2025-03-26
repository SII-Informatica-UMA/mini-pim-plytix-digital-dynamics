package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
  // Buscar categoría por ID
    @Query("SELECT c FROM Categoria c WHERE c.id = :id")
    List<Categoria> findById(Integer id);
    // Buscar categorías por nombre
    @Query("SELECT c FROM Categoria c WHERE c.nombre = :nombre")
    List<Categoria> findByNombre(String nombre);
    // Contar el total de categorías
    @Query("SELECT COUNT(c) FROM Categoria c")
    long countCategorias();
    // Buscar categorías asociadas a un producto específico
    // @Query("SELECT c FROM Categoria c JOIN c.productos p WHERE p.id = :productoId")
    // List<Categoria> findByProductoId(Long productoId);
}
