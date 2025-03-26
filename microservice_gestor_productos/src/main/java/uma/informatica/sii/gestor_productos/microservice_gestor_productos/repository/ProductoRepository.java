package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //Obtener Producto por id
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    List<Producto> findById(Integer id);

	//Obtener Producto por cuenta
    @Query("SELECT p FROM Producto p WHERE p.cuentaId = :cuentaId")
    Producto findByCuentaId(Integer cuentaId);

	//Eliminar Producto por id
    @Query("DELETE FROM Producto p WHERE p.id = :id")
    Producto deleteById(Integer id);

	//Insertar Producto
    @Query("INSERT INTO Producto p (p.id,p.gtin,p.sku,p.nombre,p.textoCorto,p.creado,p.modificado,p.miniatura,p.categorias,p.relacionesOrigen,p.relacionesDestino,p.cuentaId) VALUES (:id,:gtin,:sku,:nombre,:textoCorto,:creado,:modificado,:miniatura,:categorias,:relacionesOrigen,:relacionesDestino,:cuentaId)")
	Producto insert(Producto r);

	//Modificar Producto
    @Query("UPDATE Producto p SET p.gtin = :gtin, p.sku = :sku, p.nombre = :nombre, p.textoCorto = :textoCorto, p.creado = :creado, p.modificado = :modificado, p.miniatura = :miniatura, p.categorias = :categorias, p.relacionesOrigen = :relacionesOrigen, p.relacionesDestino = :relacionesDestino, p.cuentaId = :cuentaId WHERE p.id = :id")
    Producto update(Producto r);
} 
