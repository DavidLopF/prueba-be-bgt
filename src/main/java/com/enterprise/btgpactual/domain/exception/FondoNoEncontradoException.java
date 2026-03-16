package com.enterprise.btgpactual.domain.exception;

public class FondoNoEncontradoException extends RuntimeException {

    public FondoNoEncontradoException(String fondoId) {
        super("Fondo no encontrado con id: " + fondoId);
    }
}
