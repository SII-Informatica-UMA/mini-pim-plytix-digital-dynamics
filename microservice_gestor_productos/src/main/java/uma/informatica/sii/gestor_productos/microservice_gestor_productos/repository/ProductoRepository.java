package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
	List<Producto> findById(Integer id);
	//@Query("INSERT INTO Producto p (p.id,p.gtin,p.sku,p.nombre,p.textoCorto,p.creado,p.modificado,p.miniatura,p.categorias,p.relaciones) VALUES (:id,:gtin,:sku,:nombre,:textoCorto,:creado,:modificado,:miniatura,:categorias,:relaciones)")
	//Producto insert(Producto r);
	// @Query("UPDATE Producto p SET p.gtin = :gtin, p.sku = :sku, p.nombre = :nombre, p.textoCorto = :textoCorto, p.creado = :creado, p.modificado = :modificado, p.miniatura = :miniatura, p.categorias = :categorias, p.relaciones = :relaciones WHERE p.id = :id")
	// Producto modifyById(Producto r);
	@Query("DELETE FROM Producto p WHERE p.id = :id")
	void deleteById(Integer id);
} 
