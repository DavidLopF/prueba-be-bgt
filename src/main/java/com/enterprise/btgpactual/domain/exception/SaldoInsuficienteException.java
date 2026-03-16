package com.enterprise.btgpactual.domain.exception;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String fondoNombre) {
        super("No tiene saldo disponible para vincularse al fondo " + fondoNombre);
    }
}
