package uma.informatica.sii.gestor_productos.microservice_gestor_productos.excepciones;

public class EntidadNoExistente extends RuntimeException {
    public EntidadNoExistente() {
        super("Entidad no encontrada");
    }
}
