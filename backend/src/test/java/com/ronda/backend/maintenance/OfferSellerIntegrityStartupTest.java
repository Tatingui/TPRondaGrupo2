package com.ronda.backend.maintenance;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OfferSellerIntegrityStartupTest {
    @Test
    void restoresConstraintOnlyWhenMissingAfterRepair() {
        var repair = mock(LegacyOfferSellerRepair.class);
        var jdbc = mock(JdbcTemplate.class);
        when(repair.repair()).thenReturn(2);
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(0, 1);
        var startup = new OfferSellerIntegrityStartup(repair, jdbc);
        startup.run(null);
        startup.run(null);
        var order = inOrder(repair, jdbc);
        order.verify(repair).repair();
        order.verify(jdbc).queryForObject(anyString(), eq(Integer.class));
        order.verify(jdbc).execute(contains("FOREIGN KEY (seller_id) REFERENCES users(id)"));
        verify(jdbc, times(1)).execute(anyString());
    }

    @Test
    void abortsStartupIfRepairCannotRecoverAllReferences() {
        var repair = mock(LegacyOfferSellerRepair.class);
        var jdbc = mock(JdbcTemplate.class);
        when(repair.repair()).thenThrow(new IllegalStateException("Invalid publication"));
        assertThrows(IllegalStateException.class,
                () -> new OfferSellerIntegrityStartup(repair, jdbc).run(null));
        verifyNoInteractions(jdbc);
    }
}
