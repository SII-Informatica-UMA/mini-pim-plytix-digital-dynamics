package uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos;
import lombok.*;
@Data
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RelacionProductoDTO {
    private RelacionDTO relacion;
    private Integer idProductoOrigen;
    private Integer idProductoDestino;
}
