package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

public interface RelacionRepository extends JpaRepository<Relacion, Long> {
	// Buscar por id la relacion
	List<Relacion> findById(Integer id);
	// Buscar por cuentaId la relacion
	@Query("SELECT r FROM Relacion r WHERE r.productoOrigen.cuentaId = :cuentaId OR r.productoDestino.cuentaId = :cuentaId")
	List<Relacion> findByCuentaId(Integer cuentaId);

	// Crear una nueva relacion y modificarla
	Relacion save(Relacion r);
	// Eliminar una relacion
	void deleteById(int id);
}