package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import java.util.List;


    

public interface CategoriaService {
    List<CategoriaDTO> findAll();
    CategoriaDTO findById(Integer id);
    CategoriaDTO create(CategoriaDTO categoriaDTO);
    CategoriaDTO update(Integer id, CategoriaDTO categoriaDTO);
    void delete(Integer id);
}
