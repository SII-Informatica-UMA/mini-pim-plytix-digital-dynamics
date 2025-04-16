package uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers;

import org.springframework.stereotype.Component;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.AtributoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Atributo;

@Component
public class AtributoMapper {
    public static AtributoDTO toDTO(Atributo atributo) {
        AtributoDTO dto = new AtributoDTO();
        dto.setNombre(atributo.getNombre());
        dto.setValor(atributo.getValor());
        return dto;
    }

    public static Atributo toEntity(AtributoDTO dto) {
        Atributo atributo = new Atributo();
        atributo.setNombre(dto.getNombre());
        atributo.setValor(dto.getValor());
        return atributo;
    }
}
