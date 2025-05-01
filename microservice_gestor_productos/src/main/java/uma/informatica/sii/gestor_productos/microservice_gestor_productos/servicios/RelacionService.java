package uma.informatica.sii.gestor_productos.microservice_gestor_productos.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.repository.RelacionRepository;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.dtos.RelacionEntradaDTO;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.entity.Relacion;

import java.util.Optional;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.CredencialesNoValidas;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.EntidadNoExistente;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones.SinPermisosSuficientes;

import uma.informatica.sii.gestor_productos.microservice_gestor_productos.mappers.RelacionMapper;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario.*;
import uma.informatica.sii.gestor_productos.microservice_gestor_productos.Cuenta.CuentaService;

@Service
public class RelacionService {

    @Value("${servicio.usuarios.baseurl}")
    private String baseUrl;

    private final RelacionRepository relacionRepository;
    private final UsuarioService usuarioService;
    private final CuentaService cuentaService;
    private final RelacionMapper relacionMapper;

    public RelacionService(RelacionRepository relacionRepository, UsuarioService usuarioService, CuentaService cuentaService, RelacionMapper relacionMapper) {
        this.relacionRepository = relacionRepository;
        this.usuarioService = usuarioService;
        this.cuentaService = cuentaService;
        this.relacionMapper = relacionMapper;
    }

    public RelacionDTO getRelacionPorId(Integer idRelacion, String jwtToken) {
        Optional<Relacion> relacion = relacionRepository.findById(idRelacion);
        if (relacion.isEmpty()) {
            throw new EntidadNoExistente();
        }
        Relacion relacionExistente = relacion.get();
        Integer idCuenta = relacionExistente.getCuentaId();

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());

        if (!usuarioService.usuarioPerteneceACuenta(idCuenta, idUsuario, jwtToken)) {
            throw new SinPermisosSuficientes();
        }

        return relacionMapper.toDTO(relacionExistente);
    }

    public List<RelacionDTO> getRelacionesPorIdCuenta(Integer idCuenta, String jwtToken) {
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(CredencialesNoValidas::new);

        UsuarioDTO usuario = usuarioService.getUsuario(idUsuario, jwtToken)
                .orElseThrow(() -> new SinPermisosSuficientes());

        if (!usuario.getRole().equals(Usuario.Rol.ADMINISTRADOR)) {
            boolean pertenece = usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken);
            if (!pertenece) {
                throw new SinPermisosSuficientes();
            }
        }

        return relacionRepository.findAllByCuentaId(idCuenta)
                        .stream()
                        .map(relacionMapper::toDTO)
                        .toList()
        ;
    }

    public RelacionDTO crearRelacion(RelacionEntradaDTO relacionDTO, Integer idCuenta, String jwtToken) {

        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(CredencialesNoValidas::new);
    
        UsuarioDTO usuario = usuarioService.getUsuario(idUsuario, jwtToken)
            .orElseThrow(() -> new EntidadNoExistente());
        if(!usuarioService.usuarioPerteneceACuenta(idCuenta, usuario.getId(), jwtToken)){
            throw new SinPermisosSuficientes();
        }
    
        Relacion relacion = relacionMapper.toEntityEntrada(relacionDTO);
        

        int relacionesActuales = relacionRepository.findAllByCuentaId(idCuenta).size();
        if (!cuentaService.puedeCrearRelacion(Long.valueOf(idCuenta), relacionesActuales)) {
            throw new SinPermisosSuficientes();
        }
        
        relacion.setNombre(relacionDTO.getNombre());
        relacion.setDescripcion(relacionDTO.getDescripcion());
        relacion.setCuentaId(idCuenta);

        Relacion nuevaRelacion = relacionRepository.save(relacion);

        return relacionMapper.toDTO(nuevaRelacion);
    }

    public RelacionDTO actualizarRelacion(Integer idRelacion, RelacionDTO relacionDTO, String jwtToken) {
        Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
            .map(UsuarioDTO::getId)
            .orElseThrow(CredencialesNoValidas::new);
    
        UsuarioDTO usuario = usuarioService.getUsuario(idUsuario, jwtToken)
            .orElseThrow(() -> new SinPermisosSuficientes());
    
        Relacion relacion = relacionRepository.findById(idRelacion)
            .orElseThrow(() -> new EntidadNoExistente());
    
        if(!usuarioService.usuarioPerteneceACuenta(relacion.getCuentaId(), usuario.getId(), jwtToken)){
            throw new SinPermisosSuficientes();
        }
    
        relacion.setNombre(relacionDTO.getNombre());
        relacion.setDescripcion(relacionDTO.getDescripcion());

        Relacion actualizado = relacionRepository.save(relacion);
        
        return relacionMapper.toDTO(actualizado);
    }

    public void eliminarRelacion(Integer idRelacion, String jwtToken) {
        Optional<Relacion> relacionOptional = relacionRepository.findById(idRelacion);
        if (relacionOptional.isPresent()) {
            Relacion relacion = relacionOptional.get();
            Long idUsuario = usuarioService.getUsuarioConectado(jwtToken)
                .map(UsuarioDTO::getId)
                .orElseThrow(() -> new CredencialesNoValidas());
    
            if (!usuarioService.usuarioPerteneceACuenta(relacion.getCuentaId(), idUsuario, jwtToken)) {
                throw new SinPermisosSuficientes();
            }

            relacionRepository.deleteById(idRelacion);
        } else {
            throw new EntidadNoExistente();
        }
    }
}
