package uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Data
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProductoDTO {
    private Integer id;
    private String gtin;
    private String sku;
    private String nombre;
    private String textoCorto;
    private LocalDateTime creado;
    private LocalDateTime modificado;
    private String miniatura;
    private Set<AtributoDTO> atributos;
    private Set<CategoriaDTO> categorias;
    private Set<RelacionProductoDTO> relaciones;
}
