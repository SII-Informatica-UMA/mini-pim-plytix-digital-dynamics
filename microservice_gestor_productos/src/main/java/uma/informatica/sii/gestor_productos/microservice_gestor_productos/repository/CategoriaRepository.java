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
    // @Query("DELETE FROM Categoria c WHERE c.id = :id")
    void deleteById(Integer id);



    /*

    // Crear una categoría
    @Query("INSERT INTO Categoria c (c.id, c.nombre) VALUES (:id, :nombre)")
    Categoria insert(Categoria c);

    // Modificar una categoría
    @Query("UPDATE Categoria c SET c.nombre = :nombre WHERE c.id = :id")
    Categoria update(Categoria c);

    // Eliminar una categoría
    @Query("DELETE FROM Categoria c WHERE c.id = :id")
    void deleteById(Integer id);
     */

    //! NO NECESARIO PERO SE PUEDE INCLUIR
    // // Buscar categoría por ID
    // @Query("SELECT c FROM Categoria c WHERE c.id = :id")
    // List<Categoria> findById(Integer id);
    // // Buscar categorías por nombre
    // // @Query("SELECT c FROM Categoria c WHERE c.nombre = :nombre")
    // List<Categoria> findByNombre(String nombre);
    // // Contar el total de categorías
    // // @Query("SELECT COUNT(c) FROM Categoria c")
    // long count();

}
