package uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.*;

@Data
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProductoEntradaDTO {
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