package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //Obtener Producto
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Producto findById(Integer id);
    @Query("DELETE FROM Producto p WHERE p.id = :id")
    Producto deleteById(Integer id);
    @Query("INSERT INTO Producto p (p.id,p.gtin,p.sku,p.nombre,p.textoCorto,p.creado,p.modificado,p.miniatura,p.categorias,p.relacionesOrigen,p.relacionesDestino) VALUES (:id,:gtin,:sku,:nombre,:textoCorto,:creado,:modificado,:miniatura,:categorias,:relacionesOrigen,:relacionesDestino)")
	Producto insert(Producto r);
    @Query("UPDATE Producto p SET p.gtin = :gtin, p.sku = :sku, p.nombre = :nombre, p.textoCorto = :textoCorto, p.creado = :creado, p.modificado = :modificado, p.miniatura = :miniatura, p.categorias = :categorias, p.relacionesOrigen = :relacionesOrigen, p.relacionesDestino = :relacionesDestino WHERE p.id = :id")
    Producto update(Producto r);
} 
