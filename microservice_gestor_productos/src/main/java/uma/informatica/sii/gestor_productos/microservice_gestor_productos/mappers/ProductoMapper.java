package uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
@Component
public class ProductoMapper {
    
    private final RelacionMapper relacionMapper;

    public ProductoMapper(RelacionMapper relacionMapper) {
        this.relacionMapper = relacionMapper;
    }
    
        public ProductoDTO toDTO(Producto producto) {
            ProductoDTO dto = new ProductoDTO();
            dto.setId(producto.getId());
            dto.setGtin(producto.getGtin());
            dto.setSku(producto.getSku());
            dto.setNombre(producto.getNombre());
            dto.setTextoCorto(producto.getTextoCorto());
            dto.setCreado(producto.getCreado());
            dto.setModificado(producto.getModificado());
            dto.setMiniatura(producto.getMiniatura());
    
            // Categorías
            dto.setCategorias(
                producto.getCategorias().stream()
                    .map(c -> {
                        CategoriaDTO cdto = new CategoriaDTO();
                        cdto.setId(c.getId());
                        cdto.setNombre(c.getNombre());
                        return cdto;
                    }).collect(Collectors.toSet())
            );
    
            dto.setRelaciones(
                Stream.concat(
                    producto.getRelacionesOrigen().stream(),
                    producto.getRelacionesDestino().stream()
                )
                .map(rp -> {
                    RelacionProductoDTO rpDto = new RelacionProductoDTO();
            
                    // Tipo de relación
                    RelacionDTO relDto = new RelacionDTO();
                    relDto.setId(rp.getTipoRelacion().getId());
                    relDto.setNombre(rp.getTipoRelacion().getNombre());
                    relDto.setDescripcion(rp.getTipoRelacion().getDescripcion());
            
                    rpDto.setRelacion(relDto);
                    rpDto.setIdProductoOrigen(rp.getProductoOrigen().getId());
                    rpDto.setIdProductoDestino(rp.getProductoDestino().getId());
            
                    return rpDto;
                })
                .collect(Collectors.toSet())
            );
            
            dto.setAtributos(producto.getAtributos().stream()
            .map(attr -> {
                AtributoDTO a = new AtributoDTO();
                a.setNombre(attr.getNombre());
                a.setValor(attr.getValor());
                return a;
            }).collect(Collectors.toSet()));
    
            dto.setCuentaId(producto.getCuentaId());
    
            return dto;
    
        }
        
        public Producto toEntity(ProductoDTO dto) {
            if (dto == null) return null;
    
            Producto producto = new Producto();
    
            producto.setId(dto.getId());
            producto.setGtin(dto.getGtin());
            producto.setSku(dto.getSku());
            producto.setNombre(dto.getNombre());
            producto.setTextoCorto(dto.getTextoCorto());
            producto.setModificado(dto.getModificado());
            producto.setMiniatura(dto.getMiniatura());
    
            // Mapear atributos
            Set<Atributo> atributos = dto.getAtributos().stream()
                .map(attrDto -> {
                    Atributo atributo = new Atributo();
                    atributo.setNombre(attrDto.getNombre());
                    atributo.setValor(attrDto.getValor());
                    return atributo;
                })
                .collect(Collectors.toSet());
            producto.setAtributos(atributos);
    
            // Mapear categorías
            Set<Categoria> categorias = dto.getCategorias().stream()
                .map(catDto -> {
                    Categoria categoria = new Categoria();
                    categoria.setId(catDto.getId());
                    categoria.setNombre(catDto.getNombre());
                    return categoria;
                })
                .collect(Collectors.toSet());
            producto.setCategorias(categorias);
    
            // Mapear las relaciones (relacionesOrigen y relacionesDestino)
            producto.setRelacionesOrigen(
                dto.getRelaciones().stream()
                .map(rpDto -> {
                    RelacionProducto rp = new RelacionProducto();
                    // Convertir la relación tipo
                    Relacion tipoRelacion = relacionMapper.toEntity(rpDto.getRelacion());

                rp.setTipoRelacion(tipoRelacion);

                if (rpDto.getIdProductoOrigen() != null && rpDto.getIdProductoOrigen() > 0) {
                    Producto productoOrigen = new Producto();
                    productoOrigen.setId(rpDto.getIdProductoOrigen());
                    rp.setProductoOrigen(productoOrigen);
                } else {
                    rp.setProductoOrigen(null); // Por si acaso
                }
                
                if (rpDto.getIdProductoDestino() != null && rpDto.getIdProductoDestino() > 0) {
                    Producto productoDestino = new Producto();
                    productoDestino.setId(rpDto.getIdProductoDestino());
                    rp.setProductoDestino(productoDestino);
                } else {
                    rp.setProductoDestino(null); // ← aquí está el problema más común
                }

                return rp;
            })
            .collect(Collectors.toSet())
        );

        producto.setRelacionesDestino(
            dto.getRelaciones().stream()
            .map(rpDto -> {
                RelacionProducto rp = new RelacionProducto();
                // Convertir la relación tipo
                Relacion tipoRelacion = relacionMapper.toEntity(rpDto.getRelacion());

                rp.setTipoRelacion(tipoRelacion);

                // Setear los productos de origen y destino
                Producto productoOrigen = new Producto();
                productoOrigen.setId(rpDto.getIdProductoDestino());  // Aquí se invierten los productos para destino

                Producto productoDestino = new Producto();
                productoDestino.setId(rpDto.getIdProductoOrigen());

                rp.setProductoOrigen(productoOrigen);
                rp.setProductoDestino(productoDestino);

                return rp;
            })
            .collect(Collectors.toSet())
        );
        
        producto.setCuentaId(dto.getCuentaId());
        
        return producto;
    }
}
