package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

public interface RelacionRepository extends JpaRepository<Relacion, Integer> {
	// Buscar por id la relacion
	Optional<Relacion> findById(Integer id);
	// Buscar por cuentaId la relacion
	List<Relacion> findAllByCuentaId(Integer cuentaId);

	// Crear una nueva relacion y modificarla
	Relacion save(Relacion r);
	// Eliminar una relacion
	void deleteById(int id);
}