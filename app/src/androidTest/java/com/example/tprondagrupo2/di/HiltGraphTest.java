package com.example.tprondagrupo2.di;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import dagger.hilt.android.EntryPointAccessors;
import org.junit.Test;
import static org.junit.Assert.*;

/** No inicia pantallas, no hace requests, no lee tokens ni escribe en la base. */
public class HiltGraphTest {
    @Test public void singletonComponentComparteSesionRedYBaseLocal() {
        Context app = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        HiltGraphInspection first = EntryPointAccessors.fromApplication(app, HiltGraphInspection.class);
        HiltGraphInspection second = EntryPointAccessors.fromApplication(app, HiltGraphInspection.class);
        assertSame(first.tokenManager(), second.tokenManager());
        assertSame(first.sessionManager(), second.sessionManager());
        assertSame(first.httpClient(), second.httpClient());
        assertSame(first.database(), second.database());
        assertSame(first.publicacionDao(), second.publicacionDao());
        assertSame(first.database().publicacionDao(), first.publicacionDao());
        assertSame(first.publicationRepository(), second.publicationRepository());
        assertSame(first.publicationRepository(), second.publicationDetailSource());
    }
}
