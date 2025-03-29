package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Obtener categorías de un producto
    @Query("SELECT c FROM Categoria c JOIN c.productos p WHERE p.id = :productoId")
    List<Categoria> findCategoriasByProductoId(Long productoId);

    // Crear y modificar una categoría
    Categoria save(Categoria c);

    // Eliminar una categoría
    void deleteById(Integer id);

    //  Contar el total de categorías
    long count();
    //  Buscar categorías por nombre
    List<Categoria> findByNombre(String nombre);
}
