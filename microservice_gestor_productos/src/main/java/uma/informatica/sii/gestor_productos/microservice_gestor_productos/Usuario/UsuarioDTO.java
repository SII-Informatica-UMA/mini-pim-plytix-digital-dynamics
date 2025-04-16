package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String email;
    private Usuario.Rol role;
}
