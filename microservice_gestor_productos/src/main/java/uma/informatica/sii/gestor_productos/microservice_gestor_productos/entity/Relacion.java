package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Relacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Integer cuentaId;

    // Equals y HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relacion relacion = (Relacion) o;
        return Objects.equals(id, relacion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Relacion{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
    
    // Getters y Setters
    public Integer getId() { return id; }    
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }    
    public void setNombre(String nombre) { this.nombre = nombre; }    
    public String getDescripcion() { return descripcion; }    
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }    
    public Integer getCuentaId() { return cuentaId; }
    public void setCuentaId(Integer cuentaId) { this.cuentaId = cuentaId; }
}
