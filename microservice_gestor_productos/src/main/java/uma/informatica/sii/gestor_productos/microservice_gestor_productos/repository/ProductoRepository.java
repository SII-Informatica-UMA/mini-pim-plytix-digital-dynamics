package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Obtener producto por id
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
	List<Producto> findById(Integer id);
	
    // Crear un producto
    @Query("INSERT INTO Producto p (p.gtin, p.sku, p.nombre, p.textoCorto, p.creado, p.modificado, p.miniatura) VALUES (:gtin, :sku, :nombre, :textoCorto, :creado, :modificado, :miniatura)")
    Producto insert(Producto p);
	
    // Modificar un producto
    @Query("UPDATE Producto p SET p.gtin = :gtin, p.sku = :sku, p.nombre = :nombre, p.textoCorto = :textoCorto, p.creado = :creado, p.modificado = :modificado, p.miniatura = :miniatura WHERE p.id = :id")
    Producto modifyById(Producto p);

    // Eliminar un producto
	@Query("DELETE FROM Producto p WHERE p.id = :id")
	void deleteById(Integer id);
} 
