package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import org.springframework.stereotype.Service;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.*;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores.CategoriaMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoriaService {



    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaDTO> buscarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO buscarPorId(Integer id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        if (categoria.isEmpty()) {
            throw new EntidadNoExistente();
        }
        return CategoriaMapper.toDTO(categoria.get());
    }

    public CategoriaDTO crearCategoria(CategoriaDTO dto) {
        Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(dto.getNombre());
        if (categoriaExistente.isPresent()) {
            throw new IllegalArgumentException("La categoría '" + dto.getNombre() + "' ya existe.");
        }

        Categoria nueva = CategoriaMapper.toEntity(dto);
        nueva.setCuentaId(1); // o adaptar si viene de otro sitio
        Categoria guardada = categoriaRepository.save(nueva);
        return CategoriaMapper.toDTO(guardada);
    }

    public CategoriaDTO modificarCategoria(Integer id, CategoriaDTO dto) {
        Optional<Categoria> existente = categoriaRepository.findById(id);
        if (existente.isEmpty()) {
            throw new EntidadNoExistente();
        }

        Categoria categoria = existente.get();
        categoria.setNombre(dto.getNombre());

        Categoria actualizada = categoriaRepository.save(categoria);
        return CategoriaMapper.toDTO(actualizada);
    }

    public void eliminarCategoria(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(EntidadNoExistente::new);
        categoriaRepository.delete(categoria);
    }
    
    
}
