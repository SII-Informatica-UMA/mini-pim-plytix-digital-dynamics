package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;

import java.util.*;
import jakarta.persistence.*;
import java.io.*;
import java.time.LocalDateTime;

@Entity
@Table( uniqueConstraints = {@UniqueConstraint(columnNames = {"gtin"})})
public class Producto implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) // GTIN único y obligatorio
    private String gtin;
    @Column(nullable = false)
    private String sku;
    @Column(nullable = false)
    private String nombre;
    @Column(length = 255)
    private String textoCorto;
    @Column(nullable = false, updatable = false)
    private LocalDateTime creado;
    private LocalDateTime modificado;
    private String miniatura;

    // Implementación de hashCode y equals basada en GTIN
    @Override
    public int hashCode() {
        return Objects.hash(gtin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return Objects.equals(gtin, producto.gtin);
    }

    // Implementación de toString
    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", gtin='" + gtin + '\'' +
                ", sku='" + sku + '\'' +
                ", nombre='" + nombre + '\'' +
                ", textoCorto='" + textoCorto + '\'' +
                ", creado=" + creado +
                ", modificado=" + modificado +
                ", miniatura='" + miniatura + '\'' +
                '}';
    }

    @Column(nullable = false)
    private Long cuentaId;

    @ManyToMany
    @JoinTable(
        name = "producto_categoria",
        joinColumns = @JoinColumn(name = "producto_id"),
        foreignKey = @ForeignKey(name = "FK_PRODUCTO_CATEGORIA"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id"),
        inverseForeignKey = @ForeignKey(name = "FK_CATEGORIA_PRODUCTO")
    )
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "productoOrigen")
    private Set<Relacion> relacionesOrigen = new HashSet<>();

    @OneToMany(mappedBy = "productoDestino")
    private Set<Relacion> relacionesDestino = new HashSet<>();

    public Producto() {
        this.creado = LocalDateTime.now();
        this.modificado = LocalDateTime.now();
    }


    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTextoCorto() { return textoCorto; }
    public void setTextoCorto(String textoCorto) { this.textoCorto = textoCorto; }

    public LocalDateTime getCreado() { return creado; }

    public LocalDateTime getModificado() { return modificado; }
    public void setModificado(LocalDateTime modificado) { this.modificado = modificado; }

    public String getMiniatura() { return miniatura; }
    public void setMiniatura(String miniatura) { this.miniatura = miniatura; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public Set<Categoria> getCategorias() { return categorias; }
    public void setCategorias(Set<Categoria> categorias) { this.categorias = categorias; }

    public Set<Relacion> getRelacionesOrigen() { return relacionesOrigen; }
    public void setRelacionesOrigen(Set<Relacion> relacionesOrigen) { this.relacionesOrigen = relacionesOrigen; }

    public Set<Relacion> getRelacionesDestino() { return relacionesDestino; }
    public void setRelacionesDestino(Set<Relacion> relacionesDestino) { this.relacionesDestino = relacionesDestino; }
}
