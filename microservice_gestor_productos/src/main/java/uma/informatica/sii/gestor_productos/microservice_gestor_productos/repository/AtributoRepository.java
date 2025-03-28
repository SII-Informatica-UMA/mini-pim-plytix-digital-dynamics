package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Atributo;

@Repository
public interface AtributoRepository extends JpaRepository<Atributo, String> {
    
    // Buscar atributos por nombre
    List<Atributo> findByNombre(String nombre);

    // Buscar atributos por valor
    List<Atributo> findByValor(String valor);
}