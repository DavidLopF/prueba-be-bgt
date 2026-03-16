package com.enterprise.btgpactual.domain.exception;

public class SuscripcionExistenteException extends RuntimeException {

    public SuscripcionExistenteException(String fondoNombre) {
        super("Ya se encuentra suscrito al fondo " + fondoNombre);
    }
}
