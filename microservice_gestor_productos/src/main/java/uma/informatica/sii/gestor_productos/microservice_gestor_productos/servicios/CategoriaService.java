package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;



public interface CategoriaService {
    List<CategoriaDTO> findAll();
    CategoriaDTO findById(Long id);
    CategoriaDTO create(CategoriaDTO categoriaDTO);
    CategoriaDTO update(Long id, CategoriaDTO categoriaDTO);
    void delete(Long id);
}
