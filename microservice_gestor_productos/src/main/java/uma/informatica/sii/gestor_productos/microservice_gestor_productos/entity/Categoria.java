package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @ManyToMany(mappedBy = "categorias")
    private Set<Producto> productos;

    @Column(nullable = false)
    private Integer cuentaId;

    // HashCode, equals y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return Objects.equals(id, categoria.id) && Objects.equals(nombre, categoria.nombre) && Objects.equals(productos, categoria.productos) && Objects.equals(cuentaId, categoria.cuentaId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, productos, cuentaId);
    }
    
    @Override
    public String toString() {
        return "Categoria{id=" + id + ", nombre='" + nombre + "'}";
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Producto> getProductos() { return productos; }
    public void setProductos(Set<Producto> productos) { this.productos = productos; }
    public Integer getCuentaId() { return cuentaId; }
    public void setCuentaId(Integer cuentaId) { this.cuentaId = cuentaId; }
}