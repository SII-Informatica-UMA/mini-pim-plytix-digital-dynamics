package uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

public interface RelacionRepository extends JpaRepository<Relacion, Long> {
	@Query("SELECT r FROM Relacion r WHERE r.id = :id")
	List<Relacion> findById(Integer id);
	@Query("INSERT INTO Relacion r (r.nombre, r.descripcion) VALUES (:nombre, :descripcion)")
	Relacion insert(Relacion r);
	@Query("UPDATE Relacion r SET r.nombre = :nombre, r.descripcion = :descripcion WHERE r.id = :id")
	Relacion modifyById(Relacion r);
	@Query("DELETE FROM Relacion r WHERE r.id = :id")
	void deleteById(int id);
}