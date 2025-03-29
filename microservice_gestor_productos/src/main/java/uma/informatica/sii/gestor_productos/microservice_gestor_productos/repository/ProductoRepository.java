package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;

import java.util.List;
import org.springframework.data.jpa.repository.*;


public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //Obtener Producto por id
    List<Producto> findById(Integer id);

	//Crear Producto y modificarlo
    Producto save(Producto p);
    
	//Eliminar Producto por id
    void deleteById(Integer id);

    // Seleccionar Producto por cuentaId
    List<Producto> findByCuentaId(Integer cuentaId);
} 
