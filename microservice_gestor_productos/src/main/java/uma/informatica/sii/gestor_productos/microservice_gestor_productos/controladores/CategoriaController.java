package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaDTO> getAll() {
        return categoriaService.buscarTodas();
    }

    @GetMapping("/{id}")
    public CategoriaDTO getById(@PathVariable Integer id) {
        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    public CategoriaDTO create(@RequestBody CategoriaDTO dto) {
        return categoriaService.crearCategoria(dto);
    }

    @PutMapping("/{id}")
    public CategoriaDTO update(@PathVariable Integer id, @RequestBody CategoriaDTO dto) {
        return categoriaService.modificarCategoria(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
    }
}
