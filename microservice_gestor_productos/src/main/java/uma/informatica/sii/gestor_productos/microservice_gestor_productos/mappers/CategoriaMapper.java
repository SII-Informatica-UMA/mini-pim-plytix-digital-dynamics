package uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.*;

import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public static CategoriaDTO toDTO(Categoria categoria) {
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId(categoria.getId());
        categoriaDTO.setNombre(categoria.getNombre());
        return categoriaDTO;
    }
    public static Categoria toEntity(CategoriaDTO categoriaDTO) {
        Categoria categoria = new Categoria();
        categoria.setId(categoriaDTO.getId());
        categoria.setNombre(categoriaDTO.getNombre());
        return categoria;
    }

    public static Categoria toEntityEntrada(CategoriaEntradaDTO categoriaEntradaDTO) {
        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaEntradaDTO.getNombre());
        return categoria;
    }

}