package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

public interface RelacionRepository extends JpaRepository<Relacion, Integer> {
	// Buscar por cuentaId la relacion
	List<Relacion> findAllByCuentaId(Integer cuentaId);
}