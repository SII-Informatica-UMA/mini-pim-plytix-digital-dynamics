package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

public interface RelacionRepository extends JpaRepository<Relacion, Long> {
	// Buscar por id la relacion
	List<Relacion> findById(Integer id);
	// Crear una nueva relacion y modificarla
	Relacion save(Relacion r);
	// Eliminar una relacion
	void deleteById(int id);
}