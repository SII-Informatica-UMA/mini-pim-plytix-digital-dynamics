package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import org.springframework.stereotype.Service;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.Usuario;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.UsuarioService;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.CategoriaEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Categoria;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.CredencialesNoValidas;
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
    private final CuentaService cuentaService;

    public CategoriaService(CategoriaRepository categoriaRepository, UsuarioService usuarioService, CuentaService cuentaService) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
        this.cuentaService = cuentaService;
    }

    public CategoriaDTO getCategoriaById(Integer idCategoria, String jwtToken) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(EntidadNoExistente::new);

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());

        if (!usuarioService.usuarioPerteneceACuenta(categoria.getId(), idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
        }

        return CategoriaMapper.toDTO(categoria);
    }

    public List<CategoriaDTO> getCategoriasByidCuenta(Integer idCuenta, String jwtToken) {
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());

        UsuarioDTO usuario = usuarioService.getUsuario(idUsuario, jwtToken)
                .orElseThrow(() -> new SinPermisosSuficientes());

        if (!usuario.getRole().equals(Usuario.Rol.ADMINISTRADOR)) {
                    boolean pertenece = usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken);
                    if (!pertenece) {
                        throw new SinPermisosSuficientes();
                    }
                }

        return categoriaRepository.findAll().stream()
                .filter(cat -> cat.getCuentaId().equals(idCuenta))
                .map(CategoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO crearCategoria(CategoriaEntradaDTO dto, Integer idCuenta, String jwtToken) {
        Optional<Categoria> existente = categoriaRepository.findByNombre(dto.getNombre());
        if (existente.isPresent()) {
            throw new CredencialesNoValidas();
        }
        
        Categoria nueva = CategoriaMapper.toEntityEntrada(dto);

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());

        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
        }

        UsuarioDTO usuario = usuarioService.getUsuario(idUsuario, jwtToken)
                .orElseThrow(() -> new SinPermisosSuficientes());

        int categoriasActuales = categoriaRepository.findByCuentaId(idCuenta).size();
        if (!cuentaService.puedeCrearCategoria(idCuenta, categoriasActuales, usuario)) {
            throw new SinPermisosSuficientes();
        }
        nueva.setCuentaId(idCuenta);
        nueva.setNombre(dto.getNombre());
        
        Categoria guardada = categoriaRepository.save(nueva);
        return CategoriaMapper.toDTO(guardada);
    }

    public CategoriaDTO modificarCategoria(Integer idCategoria, CategoriaEntradaDTO dto, String jwtToken) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(EntidadNoExistente::new);

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());

        if (!usuarioService.usuarioPerteneceACuenta(categoria.getId(), idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
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
                .orElseThrow(() -> new CredencialesNoValidas());

        if (!usuarioService.usuarioPerteneceACuenta(categoria.getId(), idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
        }

        categoriaRepository.delete(categoria);
    }
}
