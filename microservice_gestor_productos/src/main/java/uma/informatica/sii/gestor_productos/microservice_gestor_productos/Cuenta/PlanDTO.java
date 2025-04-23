package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta;


import lombok.*;

@Data
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PlanDTO {
    private Long id;
    private String nombre;
    private int maxProductos;
    private int maxActivos;
    private int maxAlmacenamiento;
    private int maxCategoriasProductos;
    private int maxCategoriasActivos;
    private int maxRelaciones;
    private double precio;
}
