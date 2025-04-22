package uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity;

import java.util.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Producto{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false)
    private String gtin;
    private String sku;
    private String nombre;
    @Column(length = 255)
    private String textoCorto;
    @Column(nullable = false, updatable = false)
    private LocalDateTime creado;
    private LocalDateTime modificado;
    private String miniatura;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_atributos", joinColumns = @JoinColumn(name = "producto_id"))
    public Set<Atributo> atributos = new HashSet<>();


    @Column(nullable = false)
    private Integer cuentaId;

    @ManyToMany (fetch = FetchType.EAGER ,cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "producto_categoria",
        joinColumns = @JoinColumn(name = "producto_id", foreignKey = @ForeignKey(name = "FK_producto_categoria_producto")),
        inverseJoinColumns = @JoinColumn(name = "categoria_id", foreignKey = @ForeignKey(name = "FK_producto_categoria_categoria"))
    )
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "productoOrigen", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RelacionProducto> relacionesOrigen = new HashSet<>();

    @OneToMany(mappedBy = "productoDestino", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RelacionProducto> relacionesDestino = new HashSet<>();

    // HashCode y equals 
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

    // toString
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


    @PrePersist
    public void prePersist() {
        this.creado = LocalDateTime.now();
        this.modificado = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
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

    public Integer getCuentaId() { return cuentaId; }
    public void setCuentaId(Integer cuentaId) { this.cuentaId = cuentaId; }

    public Set<Categoria> getCategorias() { return categorias; }
    public void setCategorias(Set<Categoria> categorias) { this.categorias = categorias; }

    public Set<Atributo> getAtributos() { return atributos; }
    public void setAtributos(Set<Atributo> atributos) { this.atributos = atributos;}

    public Set<RelacionProducto> getRelacionesOrigen() { return relacionesOrigen; }
    public void setRelacionesOrigen(Set<RelacionProducto> relacionesOrigen) { this.relacionesOrigen = relacionesOrigen; }

    public Set<RelacionProducto> getRelacionesDestino() { return relacionesDestino; }
    public void setRelacionesDestino(Set<RelacionProducto> relacionesDestino) { this.relacionesDestino = relacionesDestino; }
}
