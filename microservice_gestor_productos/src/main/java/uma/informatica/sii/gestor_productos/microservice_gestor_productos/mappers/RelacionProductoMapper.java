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
        

        if (dto.getIdProductoOrigen() != null && dto.getIdProductoOrigen() > 0) {
            Producto productoOrigen = new Producto();
            productoOrigen.setId(dto.getIdProductoOrigen());
            entity.setProductoOrigen(productoOrigen);
        } else {
            entity.setProductoOrigen(null);
        }
        
        if (dto.getIdProductoDestino() != null && dto.getIdProductoDestino() > 0) {
            Producto productoDestino = new Producto();
            productoDestino.setId(dto.getIdProductoDestino());
            entity.setProductoDestino(productoDestino);
        } else {
            entity.setProductoDestino(null);
        }

        // Asignamos la relación
        Relacion tipoRelacion = new Relacion();
        tipoRelacion.setId(dto.getRelacion().getId());
        tipoRelacion.setNombre(dto.getRelacion().getNombre());
        tipoRelacion.setDescripcion(dto.getRelacion().getDescripcion());

        entity.setTipoRelacion(tipoRelacion);

        return entity;
    }
}

