package uma.informatica.sii.gestor_productos.microservice_gestor_productos.controladores;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios.CategoriaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

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
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build(); 
    }
    
    

    @ExceptionHandler(EntidadNoExistente.class)
    public ResponseEntity<String> handleEntidadNoExistente(EntidadNoExistente ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
