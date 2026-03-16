package com.enterprise.btgpactual.infrastructure.security;

import com.enterprise.btgpactual.infrastructure.adapter.persistence.ClienteDocument;
import com.enterprise.btgpactual.infrastructure.adapter.persistence.ClienteMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ClienteMongoRepository clienteMongoRepository;

    /**
     * Carga el usuario por email. Retorna UserDetails con el clienteId como username
     * para que el token JWT lleve el clienteId como subject.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ClienteDocument cliente = clienteMongoRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Cliente no encontrado con email: " + email));

        return new User(
                cliente.getId(),        // username = clienteId (usado como subject en JWT)
                cliente.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );
    }
}
