package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
  // Buscar categoría por ID
    @Query("SELECT c FROM CategoriaProducto c WHERE c.id = :id")
    List<CategoriaProducto> findById(Long id);

    // Buscar categorías por nombre
    @Query("SELECT c FROM CategoriaProducto c WHERE c.nombre = :nombre")
    List<CategoriaProducto> findByNombre(String nombre);
}
