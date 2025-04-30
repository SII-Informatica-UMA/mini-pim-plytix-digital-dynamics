package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.SinPermisosSuficientes;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.CategoriaMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    public CategoriaService(CategoriaRepository categoriaRepository, UsuarioService usuarioService) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
    }

    public CategoriaDTO getCategoriaById(Integer idCategoria, String jwtToken) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(EntidadNoExistente::new);

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        if (!usuarioService.usuarioPerteneceACuenta(categoria.getCuentaId(), idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: no puedes ver esta categoría.");
        }

        return CategoriaMapper.toDTO(categoria);
    }

    public List<CategoriaDTO> getCategoriasByCuentaId(Integer cuentaId, String jwtToken) {
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        if (!usuarioService.usuarioPerteneceACuenta(cuentaId, idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: no puedes ver las categorías de esta cuenta.");
        }

        return categoriaRepository.findAll().stream()
                .filter(cat -> cat.getCuentaId().equals(cuentaId))
                .map(CategoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO crearCategoria(CategoriaDTO dto, String jwtToken) {
        Optional<Categoria> existente = categoriaRepository.findByNombre(dto.getNombre());
        if (existente.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría '" + dto.getNombre() + "' ya existe.");
        }

        Categoria nueva = CategoriaMapper.toEntity(dto);

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        UsuarioDTO usuario = usuarioService.getUsuario(idUsuario, jwtToken)
                .orElseThrow(() -> new EntidadNoExistente());
        
        if(!usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken)){
            throw new SinPermisosSuficientes();
        }

        nueva.setId(dto.getId());
        nueva.setNombre(dto.getNombre());
        nueva.setCuentaId(idCuenta);
        
        Categoria guardada = categoriaRepository.save(nueva);
        return CategoriaMapper.toDTO(guardada);
    }
    public CategoriaDTO modificarCategoria(Integer idCategoria, CategoriaDTO dto, String jwtToken) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(EntidadNoExistente::new);

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        if (!usuarioService.usuarioPerteneceACuenta(categoria.getCuentaId(), idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: no puedes modificar esta categoría.");
        }

        categoria.setNombre(dto.getNombre());

        Categoria guardada = categoriaRepository.save(categoria);
        return CategoriaMapper.toDTO(guardada);
    }

    public void eliminarCategoria(Integer idCategoria, String jwtToken) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(EntidadNoExistente::new);

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        if (!usuarioService.usuarioPerteneceACuenta(categoria.getCuentaId(), idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: no puedes eliminar esta categoría.");
        }

        categoriaRepository.delete(categoria);
    }
}
