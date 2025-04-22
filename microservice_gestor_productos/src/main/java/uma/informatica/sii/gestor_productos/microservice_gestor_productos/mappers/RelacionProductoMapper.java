package uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers;

import org.springframework.stereotype.Component;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionProductoDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Producto;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.RelacionProducto;
@Component
public class RelacionProductoMapper {

    public static RelacionProducto toEntity(RelacionProductoDTO dto) {
        RelacionProducto entity = new RelacionProducto();
        
        // Asignamos las entidades productoOrigen y productoDestino
        Producto productoOrigen = new Producto();
        productoOrigen.setId(dto.getIdProductoOrigen());

        Producto productoDestino = new Producto();
        productoDestino.setId(dto.getIdProductoDestino());

        entity.setProductoOrigen(productoOrigen);
        entity.setProductoDestino(productoDestino);

        // Asignamos la relación
        Relacion tipoRelacion = new Relacion();
        tipoRelacion.setId(dto.getRelacion().getId());
        tipoRelacion.setNombre(dto.getRelacion().getNombre());
        tipoRelacion.setDescripcion(dto.getRelacion().getDescripcion());

        entity.setTipoRelacion(tipoRelacion);

        return entity;
    }
}

