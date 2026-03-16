package com.enterprise.btgpactual.infrastructure.adapter.persistence;

import com.enterprise.btgpactual.domain.model.PreferenciaNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clientes")
public class ClienteDocument {

    @Id
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private BigDecimal saldo;
    private PreferenciaNotificacion preferenciaNotificacion;

    @Builder.Default
    private Set<String> fondosSuscritos = new HashSet<>();

    private String passwordHash;
}
