package uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers;

import org.springframework.stereotype.Component;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
//import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

@Component
public class RelacionMapper {
    public RelacionDTO toDTO(Relacion relacion) {
        if (relacion == null) return null;
        RelacionDTO dto = new RelacionDTO();
        dto.setId(relacion.getId());
        dto.setNombre(relacion.getNombre());
        dto.setDescripcion(relacion.getDescripcion());
        return dto;
    }

    public Relacion toEntity(RelacionDTO dto) {
        Relacion relacion = new Relacion();
        relacion.setId(dto.getId());
        relacion.setNombre(dto.getNombre());
        relacion.setDescripcion(dto.getDescripcion());
        return relacion;
    }
}
