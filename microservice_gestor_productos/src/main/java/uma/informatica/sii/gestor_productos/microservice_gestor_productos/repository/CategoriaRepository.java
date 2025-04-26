package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    // Obtener categorías por cuentaId
    List<Categoria> findByCuentaId(Integer cuentaId);

    //  Buscar categorías por nombre
    Optional<Categoria> findByNombre(String nombre);
}
