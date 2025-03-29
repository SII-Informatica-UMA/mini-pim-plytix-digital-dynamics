package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

public interface RelacionRepository extends JpaRepository<Relacion, Long> {
	// Buscar por id la relacion
	// // @Query("SELECT r FROM Relacion r WHERE r.id = :id")
	List<Relacion> findById(Integer id);
	// Crear una nueva relacion y modificarla
	Relacion save(Relacion r);
	// Eliminar una relacion
	// // @Query("DELETE FROM Relacion r WHERE r.id = :id")
	void deleteById(int id);


	/*
	@Query("INSERT INTO Relacion r (r.nombre, r.descripcion) VALUES (:nombre, :descripcion)")
	Relacion insert(Relacion r);
	// Modificar una relacion
	@Query("UPDATE Relacion r SET r.nombre = :nombre, r.descripcion = :descripcion WHERE r.id = :id")
	Relacion modifyById(Relacion r);
	 */
}