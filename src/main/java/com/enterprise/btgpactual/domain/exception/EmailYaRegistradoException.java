package com.enterprise.btgpactual.domain.exception;

public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("Ya existe un cliente registrado con el email: " + email);
    }
}
