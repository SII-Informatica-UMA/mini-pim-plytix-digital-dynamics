package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta;

import lombok.*;
import java.time.LocalDate;
@Data
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CuentaDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String nif;
    private LocalDate fechaAlta;
    private PlanDTO plan;
}
