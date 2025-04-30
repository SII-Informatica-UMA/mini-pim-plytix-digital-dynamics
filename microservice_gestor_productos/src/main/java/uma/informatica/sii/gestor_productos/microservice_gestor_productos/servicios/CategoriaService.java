package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.CategoriaMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.CategoriaRepository;

import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    public CategoriaService(CategoriaRepository categoriaRepository, UsuarioService usuarioService) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
    }

    public CategoriaDTO getCategoriaByIdAndCuenta(Integer idCategoria, Integer cuentaId, String jwtToken) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(EntidadNoExistente::new);

        if (!categoria.getCuentaId().equals(cuentaId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: la categoría no pertenece a la cuenta proporcionada.");
        }

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        if (!usuarioService.usuarioPerteneceACuenta(cuentaId, idUsuario, jwtToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: el usuario no pertenece a la cuenta.");
        }

        return CategoriaMapper.toDTO(categoria);
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

        Integer cuentaId = usuarioService.usuarioPerteneceACuenta(dto.getId(), idUsuario, jwtToken) ? dto.getId() : null;
        nueva.setCuentaId(cuentaId);

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