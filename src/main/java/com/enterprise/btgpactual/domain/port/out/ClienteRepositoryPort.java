package com.enterprise.btgpactual.domain.port.out;

import com.enterprise.btgpactual.domain.model.Cliente;

import java.util.Optional;

public interface ClienteRepositoryPort {

    Optional<Cliente> buscarPorId(String id);

    Optional<Cliente> buscarPorEmail(String email);

    Cliente guardar(Cliente cliente);

    Cliente registrar(Cliente cliente, String passwordHash);
}
