package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;
import jakarta.persistence.*;
import java.util.*;

@Embeddable
public class Atributo {
    @Column(name = "nombre_atributo", nullable = false)
    private String nombre;
    @Column(name = "valor_atributo", nullable = false)
    private String valor;

    
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
    
    // Getters y Setters
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

}