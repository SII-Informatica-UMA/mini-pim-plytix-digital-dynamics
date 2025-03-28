package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class Atributo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String nombre;
    private String valor;

    @ManyToMany(mappedBy = "atributos")
    private Set<Producto> productos = new HashSet<>();
    // Getters y Setters
    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getValor() {
        return valor;
    }
    public void setValor(String valor) {
        this.valor = valor;
    }
    public Set<Producto> getProductos() {
        return productos;
    }
    public void setProductos(Set<Producto> productos) {
        this.productos = productos;
    }

    // Implementación de hashCode, equals y toString
    @Override
    public int hashCode() {
        return Objects.hash(nombre, valor);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atributo atributo = (Atributo) o;
        return Objects.equals(nombre, atributo.nombre) && Objects.equals(valor, atributo.valor);
    }
    @Override
    public String toString() {
        return "Atributo{" +
                "nombre='" + nombre + '\'' +
                ", valor='" + valor + '\'' +
                '}';
    }
}