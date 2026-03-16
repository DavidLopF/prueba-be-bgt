package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.exception.ClienteNoEncontradoException;
import com.enterprise.btgpactual.domain.exception.FondoNoEncontradoException;
import com.enterprise.btgpactual.domain.exception.SaldoInsuficienteException;
import com.enterprise.btgpactual.domain.exception.SuscripcionExistenteException;
import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.Fondo;
import com.enterprise.btgpactual.domain.model.TipoTransaccion;
import com.enterprise.btgpactual.domain.model.Transaccion;
import com.enterprise.btgpactual.domain.port.in.SuscribirFondoUseCase;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.FondoRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.NotificacionPort;
import com.enterprise.btgpactual.domain.port.out.TransaccionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SuscribirFondoUseCaseImpl implements SuscribirFondoUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final FondoRepositoryPort fondoRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final NotificacionPort notificacionPort;

    @Override
    public Transaccion ejecutar(String clienteId, String fondoId) {
        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));

        Fondo fondo = fondoRepository.buscarPorId(fondoId)
                .orElseThrow(() -> new FondoNoEncontradoException(fondoId));

        if (cliente.estaSuscritoA(fondoId)) {
            throw new SuscripcionExistenteException(fondo.getNombre());
        }

        if (!cliente.tieneSaldoSuficiente(fondo.getMontoMinimo())) {
            throw new SaldoInsuficienteException(fondo.getNombre());
        }

        Cliente clienteActualizado = cliente.suscribirFondo(fondoId, fondo.getMontoMinimo());
        clienteRepository.guardar(clienteActualizado);

        Transaccion transaccion = Transaccion.builder()
                .clienteId(clienteId)
                .fondoId(fondoId)
                .fondoNombre(fondo.getNombre())
                .tipo(TipoTransaccion.APERTURA)
                .monto(fondo.getMontoMinimo())
                .fecha(LocalDateTime.now())
                .build();

        Transaccion transaccionGuardada = transaccionRepository.guardar(transaccion);


        notificacionPort.notificarSuscripcion(clienteActualizado, transaccionGuardada);

        return transaccionGuardada;
    }
}
