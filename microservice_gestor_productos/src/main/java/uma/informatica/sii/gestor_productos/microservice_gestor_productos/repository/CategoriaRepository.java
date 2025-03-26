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

    // Crear una categoría
    @Query("INSERT INTO Categoria c (c.id, c.nombre) VALUES (:id, :nombre)")
    Categoria insert(Categoria c);
    // Modificar una categoría
    @Query("UPDATE Categoria c SET c.nombre = :nombre WHERE c.id = :id")
    Categoria update(Categoria c);
    // Eliminar una categoría
    @Query("DELETE FROM Categoria c WHERE c.id = :id")
    void deleteById(Integer id);

    // Obtener categorías de un producto
    // @Query("SELECT c FROM Categoria c JOIN Producto p ON c.id = p.categorias WHERE p.id = :id")
    // List<Categoria> findCategoriasByProductoId(Integer id);
}
