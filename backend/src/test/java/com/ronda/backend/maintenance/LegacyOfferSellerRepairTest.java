package com.ronda.backend.maintenance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LegacyOfferSellerRepairTest {
    private JdbcTemplate jdbc;
    private LegacyOfferSellerRepair repair;

    @BeforeEach
    void setup() {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(ds);
        repair = new LegacyOfferSellerRepair(jdbc, new DataSourceTransactionManager(ds));
        jdbc.execute("CREATE TABLE users (id BIGINT PRIMARY KEY)");
        jdbc.execute("CREATE TABLE publications (id BIGINT PRIMARY KEY, seller_id BIGINT)");
        jdbc.execute("CREATE TABLE offers (id BIGINT PRIMARY KEY, publication_id BIGINT, seller_id BIGINT, status VARCHAR(30), amount DECIMAL)");
        jdbc.update("INSERT INTO users VALUES (1), (2)");
        jdbc.update("INSERT INTO publications VALUES (10, 1)");
    }

    @Test
    void repairsMissingSellersPreservesValidOnesAndIsIdempotent() {
        jdbc.update("INSERT INTO offers VALUES (1,10,0,'ACCEPTED',80000), (2,10,99,'PENDING',200), (3,10,NULL,'REJECTED',300), (4,10,2,'ACCEPTED',400)");
        assertEquals(3, repair.repair());
        assertEquals(0, repair.repair());
        assertEquals(1L, seller(1));
        assertEquals(1L, seller(2));
        assertEquals(1L, seller(3));
        assertEquals(2L, seller(4));
        assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM offers", Integer.class));
        assertEquals("ACCEPTED", jdbc.queryForObject("SELECT status FROM offers WHERE id=1", String.class));
        assertEquals(80000, jdbc.queryForObject("SELECT amount FROM offers WHERE id=1", Integer.class));
    }

    @Test
    void rollsBackAllRepairsWhenPublicationCannotSupplyAValidSeller() {
        jdbc.update("INSERT INTO publications VALUES (20, 99)");
        jdbc.update("INSERT INTO offers VALUES (1,10,0,'ACCEPTED',80000), (2,20,0,'PENDING',200)");
        assertThrows(IllegalStateException.class, repair::repair);
        assertEquals(0L, seller(1));
        assertEquals(0L, seller(2));
    }

    private Long seller(int id) {
        return jdbc.queryForObject("SELECT seller_id FROM offers WHERE id=?", Long.class, id);
    }
}
