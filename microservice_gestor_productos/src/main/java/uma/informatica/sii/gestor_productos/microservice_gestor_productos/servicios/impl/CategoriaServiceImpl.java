package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.impl;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.CategoriaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores.CategoriaMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CategoriaMapper categoriaMapper;

    @Override
    public List<CategoriaDTO> findAll() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaDTO findById(Long id) {
        return categoriaRepository.findById(id)
                .map(CategoriaMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    @Override
    public CategoriaDTO create(CategoriaDTO categoriaDTO) {
        Categoria categoria = categoriaMapper.toEntity(categoriaDTO);
        return CategoriaMapper.toDTO(categoriaRepository.save(categoria));
    }

    @Override
    public CategoriaDTO update(Long id, CategoriaDTO categoriaDTO) {
        Categoria existing = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        existing.setNombre(categoriaDTO.getNombre()); // adapta los campos
        return CategoriaMapper.toDTO(categoriaRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        categoriaRepository.deleteById(id);
    }
}
