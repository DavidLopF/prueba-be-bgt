package com.enterprise.btgpactual.domain.exception;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(String clienteId) {
        super("Cliente no encontrado con id: " + clienteId);
    }
}
