package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "relaciones")
public class Relacion {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "producto_origen_id", foreignKey = @ForeignKey(name = "FK_PRODUCTO_RELACION"), nullable = false)
    private Producto productoOrigen;

    @ManyToOne
    @JoinColumn(name = "producto_destino_id", foreignKey = @ForeignKey(name = "FK_PRODUCTO_RELACION"), nullable = false)
    private Producto productoDestino;

    public Relacion() {}

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Producto getProductoOrigen() {
        return productoOrigen;
    }

    public void setProductoOrigen(Producto productoOrigen) {
        this.productoOrigen = productoOrigen;
    }

    public Producto getProductoDestino() {
        return productoDestino;
    }

    public void setProductoDestino(Producto productoDestino) {
        this.productoDestino = productoDestino;
    }

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
}
