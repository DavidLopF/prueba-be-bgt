package com.enterprise.btgpactual.shared.config;

import com.enterprise.btgpactual.domain.model.CategoriaFondo;
import com.enterprise.btgpactual.infrastructure.adapter.persistence.FondoDocument;
import com.enterprise.btgpactual.infrastructure.adapter.persistence.FondoMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final FondoMongoRepository fondoMongoRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (fondoMongoRepository.count() == 0) {
            List<FondoDocument> fondos = List.of(
                    FondoDocument.builder()
                            .id("1").nombre("FPV_BTG_PACTUAL_RECAUDADORA")
                            .montoMinimo(new BigDecimal("75000")).categoria(CategoriaFondo.FPV).build(),
                    FondoDocument.builder()
                            .id("2").nombre("FPV_BTG_PACTUAL_ECOPETROL")
                            .montoMinimo(new BigDecimal("125000")).categoria(CategoriaFondo.FPV).build(),
                    FondoDocument.builder()
                            .id("3").nombre("DEUDAPRIVADA")
                            .montoMinimo(new BigDecimal("50000")).categoria(CategoriaFondo.FIC).build(),
                    FondoDocument.builder()
                            .id("4").nombre("FDO-ACCIONES")
                            .montoMinimo(new BigDecimal("250000")).categoria(CategoriaFondo.FIC).build(),
                    FondoDocument.builder()
                            .id("5").nombre("FPV_BTG_PACTUAL_DINAMICA")
                            .montoMinimo(new BigDecimal("100000")).categoria(CategoriaFondo.FPV).build()
            );
            fondoMongoRepository.saveAll(fondos);
            log.info("✓ Data seeder: {} fondos inicializados correctamente", fondos.size());
        } else {
            log.info("✓ Data seeder: los fondos ya existen, no se requiere inicialización");
        }
    }
}
