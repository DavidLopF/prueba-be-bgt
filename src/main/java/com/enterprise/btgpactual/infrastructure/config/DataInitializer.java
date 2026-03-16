package com.enterprise.btgpactual.infrastructure.config;

import com.enterprise.btgpactual.domain.model.CategoriaFondo;
import com.enterprise.btgpactual.infrastructure.adapter.persistence.FondoDocument;
import com.enterprise.btgpactual.infrastructure.adapter.persistence.FondoMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga los 5 fondos del enunciado en MongoDB al iniciar la aplicación.
 * Solo inserta si la colección está vacía (idempotente).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final FondoMongoRepository fondoRepository;

    @Override
    public void run(String... args) {
        if (fondoRepository.count() > 0) {
            log.info("Fondos ya inicializados — omitiendo seed.");
            return;
        }

        List<FondoDocument> fondos = List.of(
                FondoDocument.builder()
                        .id("1")
                        .nombre("FPV_BTG_PACTUAL_RECAUDADORA")
                        .montoMinimo(new BigDecimal("75000"))
                        .categoria(CategoriaFondo.FPV)
                        .build(),
                FondoDocument.builder()
                        .id("2")
                        .nombre("FPV_BTG_PACTUAL_ECOPETROL")
                        .montoMinimo(new BigDecimal("125000"))
                        .categoria(CategoriaFondo.FPV)
                        .build(),
                FondoDocument.builder()
                        .id("3")
                        .nombre("DEUDAPRIVADA")
                        .montoMinimo(new BigDecimal("50000"))
                        .categoria(CategoriaFondo.FIC)
                        .build(),
                FondoDocument.builder()
                        .id("4")
                        .nombre("FDO-ACCIONES")
                        .montoMinimo(new BigDecimal("250000"))
                        .categoria(CategoriaFondo.FIC)
                        .build(),
                FondoDocument.builder()
                        .id("5")
                        .nombre("FPV_BTG_PACTUAL_DINAMICA")
                        .montoMinimo(new BigDecimal("100000"))
                        .categoria(CategoriaFondo.FPV)
                        .build()
        );

        fondoRepository.saveAll(fondos);
        log.info("Se insertaron {} fondos en MongoDB.", fondos.size());
    }
}
