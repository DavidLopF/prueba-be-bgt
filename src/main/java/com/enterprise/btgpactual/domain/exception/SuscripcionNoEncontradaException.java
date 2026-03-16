package com.enterprise.btgpactual.domain.exception;

public class SuscripcionNoEncontradaException extends RuntimeException {

    public SuscripcionNoEncontradaException(String fondoNombre) {
        super("No tiene suscripción activa al fondo " + fondoNombre);
    }
}
