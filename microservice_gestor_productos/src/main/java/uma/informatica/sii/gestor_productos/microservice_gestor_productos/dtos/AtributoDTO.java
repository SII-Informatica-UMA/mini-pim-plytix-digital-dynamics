package uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos;
import lombok.*;

@Data
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AtributoDTO {
    private String nombre;
    private String valor;
}
