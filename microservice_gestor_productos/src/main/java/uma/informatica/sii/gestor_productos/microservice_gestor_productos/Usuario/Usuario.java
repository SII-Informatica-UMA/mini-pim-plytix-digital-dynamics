package uma.informatica.sii.gestor_productos.microservice_gestor_productos.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    public enum Rol {
        CLIENTE,
        ADMINISTRADOR
    }

    private Long id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String email;
    private Rol role;
    private String password;
}
