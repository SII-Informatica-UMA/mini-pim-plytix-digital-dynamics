package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;
import jakarta.persistence.*;
import java.util.Objects;
@Entity
public class RelacionProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "producto_origen_id", foreignKey = @ForeignKey(name = "FK_relacion_producto_origen"), nullable = true)
    private Producto productoOrigen;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "producto_destino_id", foreignKey = @ForeignKey(name = "FK_relacion_producto_destino"), nullable = true)
    private Producto productoDestino;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_relacion_id", foreignKey = @ForeignKey(name = "FK_relacion_producto_tipo"), nullable = true)
    private Relacion tipoRelacion;

    // Equals y HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelacionProducto that = (RelacionProducto) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    // toString
    @Override
    public String toString() {
        return "RelacionProducto{" +
                "id=" + id +
                ", productoOrigen=" + productoOrigen +
                ", productoDestino=" + productoDestino +
                ", tipoRelacion=" + tipoRelacion +
                '}';
    }
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Producto getProductoOrigen() { return productoOrigen; }
    public void setProductoOrigen(Producto productoOrigen) { this.productoOrigen = productoOrigen; }
    public Producto getProductoDestino() { return productoDestino; }
    public void setProductoDestino(Producto productoDestino) { this.productoDestino = productoDestino; }
    public Relacion getTipoRelacion() { return tipoRelacion; }
    public void setTipoRelacion(Relacion tipoRelacion) { this.tipoRelacion = tipoRelacion;}
    public RelacionProducto() { }
}
