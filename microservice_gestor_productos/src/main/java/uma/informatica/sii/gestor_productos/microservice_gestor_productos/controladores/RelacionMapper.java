package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import org.springframework.stereotype.Component;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
//import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

@Component
public class RelacionMapper {
    public RelacionDTO toDTO(Relacion relacion) {
        if (relacion == null) return null;
        // Relación básica
        RelacionDTO rel = new RelacionDTO();
        rel.setId(relacion.getId());
        rel.setNombre(relacion.getNombre());
        rel.setDescripcion(relacion.getDescripcion());
        // dto.setRelacion(rel);
        // Productos relacionados (solo ID)
        //dto.setIdProductoOrigen(relacion.getProductoOrigen() != null ? relacion.getProductoOrigen().getId() : null);
        //dto.setIdProductoDestino(relacion.getProductoDestino() != null ? relacion.getProductoDestino().getId() : null);

        return rel;
    }

    public Relacion toEntity(RelacionDTO dto) {
        Relacion relacion = new Relacion();
        relacion.setId(dto.getId());
        relacion.setNombre(dto.getNombre());
        relacion.setDescripcion(dto.getDescripcion());
        return relacion;
    }
}
