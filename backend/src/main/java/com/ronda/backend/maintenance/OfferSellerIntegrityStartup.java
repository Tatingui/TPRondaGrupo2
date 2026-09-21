package com.ronda.backend.maintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Compatibilidad con bases MySQL previas a la incorporacion de offers.seller_id. */
@Component
@ConditionalOnProperty(name = "ronda.maintenance.offer-seller-repair", havingValue = "true", matchIfMissing = true)
public class OfferSellerIntegrityStartup implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(OfferSellerIntegrityStartup.class);
    private final LegacyOfferSellerRepair repair;
    private final JdbcTemplate jdbc;

    public OfferSellerIntegrityStartup(LegacyOfferSellerRepair repair, JdbcTemplate jdbc) {
        this.repair = repair;
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        int repaired = repair.repair();
        // Hibernate intenta crear la FK antes de este runner y puede fallar por datos antiguos.
        // Comprobamos la relacion, no el nombre generado, para no duplicar restricciones.
        Integer constraints = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'offers'
                  AND COLUMN_NAME = 'seller_id' AND REFERENCED_TABLE_SCHEMA = DATABASE()
                  AND REFERENCED_TABLE_NAME = 'users' AND REFERENCED_COLUMN_NAME = 'id'
                """, Integer.class);
        if (constraints == null || constraints == 0) {
            jdbc.execute("ALTER TABLE offers ADD CONSTRAINT fk_offers_seller_integrity "
                    + "FOREIGN KEY (seller_id) REFERENCES users(id)");
        }
        LOG.info("Integridad de vendedores de ofertas verificada. Ofertas reparadas: {}", repaired);
    }
}
