package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores.CategoriaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<CategoriaDTO> findAll() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO findById(Integer id) {
        return categoriaRepository.findById(id)
                .map(CategoriaMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    public CategoriaDTO create(CategoriaDTO dto) {
        Categoria entity = CategoriaMapper.toEntity(dto);
        Categoria saved = categoriaRepository.save(entity);
        return CategoriaMapper.toDTO(saved);
    }

    public CategoriaDTO update(Integer id, CategoriaDTO dto) {
        Categoria existing = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        existing.setNombre(dto.getNombre()); // update only what's allowed
        Categoria updated = categoriaRepository.save(existing);
        return CategoriaMapper.toDTO(updated);
    }

    public void delete(Integer id) {
        if (!categoriaRepository.existsById(id.longValue())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
        }
        categoriaRepository.deleteById(id.longValue());
    }
}
