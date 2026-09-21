package com.ronda.backend.maintenance;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Repara referencias heredadas sin cargar entidades JPA que pueden estar rotas. */
@Component
public class LegacyOfferSellerRepair {
    private static final String INVALID_SELLER =
            "NOT EXISTS (SELECT 1 FROM users u WHERE u.id = offers.seller_id)";
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public LegacyOfferSellerRepair(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    public int repair() {
        return transaction.execute(status -> {
            int repaired = jdbc.update("""
                    UPDATE offers SET seller_id =
                        (SELECT p.seller_id FROM publications p WHERE p.id = offers.publication_id)
                    """ + " WHERE " + INVALID_SELLER + " AND EXISTS (" + """
                        SELECT 1 FROM publications p JOIN users u ON u.id = p.seller_id
                        WHERE p.id = offers.publication_id)
                    """);
            Long remaining = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM offers WHERE " + INVALID_SELLER, Long.class);
            if (remaining != null && remaining > 0) {
                throw new IllegalStateException("Hay " + remaining
                        + " ofertas sin vendedor recuperable desde su publicacion. "
                        + "Se cancela la reparacion completa; revisar los datos sin eliminarlos.");
            }
            return repaired;
        });
    }
}
