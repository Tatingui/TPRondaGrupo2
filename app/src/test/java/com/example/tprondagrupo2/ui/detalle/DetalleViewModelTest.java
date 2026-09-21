package com.example.tprondagrupo2.ui.detalle;

import static org.junit.Assert.*;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.support.ImmediateMainThreadRule;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetalleViewModelTest {
    @Rule public final ImmediateMainThreadRule mainThread = new ImmediateMainThreadRule();
    private FakeSource source;
    private DetalleViewModel vm;

    @Before
    public void setup() {
        source = new FakeSource();
        vm = new DetalleViewModel(source);
    }

    @Test
    public void muestraResumenMientrasCargaSinHabilitarAccionesAntesDelDetalle() {
        Publicacion resumen = publicacion("Resumen");
        vm.inicializar(resumen);

        assertSame(resumen, estado().getPublicacion());
        assertTrue(estado().isCargando());
        assertFalse(estado().isDetalleConfirmado());
        assertEquals(1, source.visitas.size());
        assertTrue(source.preguntas.isEmpty());
        assertSame(resumen, vm.consumirCachePendiente());
        assertNull(vm.consumirCachePendiente());
        assertTrue(vm.consumirVisitaLocal());
        assertFalse(vm.consumirVisitaLocal());
    }

    @Test
    public void recrearVistaConMismoStoreConservaDatosYNoRepiteSolicitudesNiVisita() {
        ViewModelStore store = new ViewModelStore();
        DetalleViewModelFactory factory = new DetalleViewModelFactory(source);
        DetalleViewModel primera = new ViewModelProvider(store, factory).get(DetalleViewModel.class);
        primera.inicializar(publicacion("Inicial"));
        source.detalles.get(0).success(publicacion("Actualizada"));
        source.preguntas.get(0).success(Collections.emptyList());
        primera.consumirCachePendiente();
        primera.consumirVisitaLocal();
        primera.seleccionarFoto(2);

        DetalleViewModel recreada = new ViewModelProvider(store, factory).get(DetalleViewModel.class);
        recreada.inicializar(publicacion("Bundle viejo"));

        assertSame(primera, recreada);
        assertEquals("Actualizada", recreada.getEstado().getValue().getPublicacion().getTitle());
        assertEquals(2, recreada.getFotoSeleccionada());
        assertEquals(1, source.detalles.size());
        assertEquals(1, source.visitas.size());
        assertNull(recreada.consumirCachePendiente());
        assertFalse(recreada.consumirVisitaLocal());
        store.clear();
    }

    @Test
    public void ultimaSolicitudGanaAunqueLaCanceladaEntregueRespuestaYError() {
        vm.inicializar(publicacion("Inicial"));
        vm.recargarDetalle();
        assertTrue(source.detalles.get(0).canceled);
        source.detalles.get(1).success(publicacion("Nueva"));
        source.detalles.get(0).success(publicacion("Vieja"));
        source.detalles.get(0).error(PublicationDetailSource.LoadError.NETWORK);

        assertEquals("Nueva", estado().getPublicacion().getTitle());
        assertNull(estado().getError());
        assertEquals(1, source.preguntas.size());
    }

    @Test
    public void errorDeRedConservaResumenYReintentoObtieneDetalleVendidoAutorizado() {
        vm.inicializar(publicacion("Resumen"));
        source.detalles.get(0).error(PublicationDetailSource.LoadError.NETWORK);
        assertFalse(estado().isCargando());
        assertFalse(estado().isDetalleConfirmado());
        assertEquals("Resumen", estado().getPublicacion().getTitle());

        vm.recargarDetalle();
        Publicacion vendida = new Gson().fromJson("{\"id\":\"1\",\"state\":\"SOLD\","
                + "\"addressVisible\":true,\"address\":\"Calle 1\"}", Publicacion.class);
        source.detalles.get(1).success(vendida);
        assertNull(estado().getError());
        assertTrue(estado().isDetalleConfirmado());
        assertEquals("Calle 1", new ComoLlegarResolver().resolver(estado().getPublicacion()));
        assertEquals(1, source.visitas.size());
    }

    @Test
    public void perdidaDeRedNoBorraUnDetalleYaConfirmado() {
        vm.inicializar(publicacion("Resumen"));
        source.detalles.get(0).success(publicacion("Completa"));
        vm.recargarDetalle();
        source.detalles.get(1).error(PublicationDetailSource.LoadError.NETWORK);
        assertTrue(estado().isDetalleConfirmado());
        assertEquals("Completa", estado().getPublicacion().getTitle());
    }

    @Test
    public void inexistenteOSinPermisoDeshabilitaElDetalleConfirmado() {
        for (PublicationDetailSource.LoadError error : new PublicationDetailSource.LoadError[] {
                PublicationDetailSource.LoadError.NOT_FOUND,
                PublicationDetailSource.LoadError.UNAUTHORIZED,
                PublicationDetailSource.LoadError.FORBIDDEN }) {
            FakeSource actual = new FakeSource();
            DetalleViewModel model = new DetalleViewModel(actual);
            model.inicializar(publicacion("Inicial"));
            actual.detalles.get(0).success(publicacion("Completa"));
            model.recargarDetalle();
            actual.detalles.get(1).error(error);
            assertFalse(model.getEstado().getValue().isDetalleConfirmado());
            assertTrue(model.getEstado().getValue().getPreguntas().isEmpty());
            assertEquals(error, model.getEstado().getValue().getError());
        }
    }

    @Test
    public void rechazaRespuestaDeOtraPublicacion() {
        vm.inicializar(publicacion("Inicial"));
        Publicacion otra = publicacion("Otra");
        otra.setId("2");
        source.detalles.get(0).success(otra);
        assertEquals(PublicationDetailSource.LoadError.SERVER, estado().getError());
        assertEquals("1", estado().getPublicacion().getId());
        assertTrue(source.preguntas.isEmpty());
    }

    @Test
    public void ignoraPreguntasDeLecturasAnteriores() {
        vm.inicializar(publicacion("Inicial"));
        source.detalles.get(0).success(publicacion("Primera"));
        vm.recargarDetalle();
        assertTrue(source.preguntas.get(0).canceled);
        source.detalles.get(1).success(publicacion("Segunda"));
        source.preguntas.get(1).success(Collections.emptyList());
        source.preguntas.get(0).success(Collections.singletonList(new Pregunta()));
        source.preguntas.get(0).error(PublicationDetailSource.LoadError.NETWORK);
        assertTrue(estado().getPreguntas().isEmpty());
        assertNull(estado().getErrorPreguntas());
    }

    @Test
    public void errorDePreguntasPermiteReintentarSinRepetirDetalle() {
        vm.inicializar(publicacion("Inicial"));
        source.detalles.get(0).success(publicacion("Completa"));
        source.preguntas.get(0).error(PublicationDetailSource.LoadError.NETWORK);
        assertNotNull(estado().getErrorPreguntas());
        vm.recargarPreguntas();
        source.preguntas.get(1).success(Collections.singletonList(new Pregunta()));
        assertEquals(1, estado().getPreguntas().size());
        assertNull(estado().getErrorPreguntas());
        assertEquals(1, source.detalles.size());
    }

    @Test
    public void respuestaEnVueloNoDeshaceFavoritoNiOfertaConfirmados() {
        vm.inicializar(publicacion("Inicial"));
        Offer oferta = new Gson().fromJson("{\"id\":4,\"status\":\"PENDING\",\"offeredPrice\":100}", Offer.class);
        vm.actualizarFavorito(true);
        vm.actualizarOferta(oferta);
        source.detalles.get(0).success(publicacion("Vieja en vuelo"));
        assertTrue(estado().getPublicacion().isFavorite());
        assertSame(oferta, estado().getPublicacion().getMyOffer());

        // Una consulta posterior vuelve a tomar al servidor como autoridad.
        vm.recargarDetalle();
        source.detalles.get(1).success(publicacion("Actual"));
        assertFalse(estado().getPublicacion().isFavorite());
        assertNull(estado().getPublicacion().getMyOffer());
    }

    @Test
    public void revalidaTrasEscrituraCanceladaSinRepetirVisita() {
        vm.inicializar(publicacion("Inicial"));
        vm.revalidarAlVolver();
        vm.inicializar(publicacion("Bundle"));
        assertEquals(2, source.detalles.size());
        assertEquals(1, source.visitas.size());
        assertTrue(source.detalles.get(0).canceled);
    }

    @Test
    public void cerrarViewModelCancelaTodoYDescartaResultadosTardios() {
        vm.inicializar(publicacion("Inicial"));
        source.detalles.get(0).success(publicacion("Completa"));
        DetalleUiState anterior = estado();
        vm.onCleared();
        source.detalles.get(0).success(publicacion("Tardía"));
        source.preguntas.get(0).success(Collections.singletonList(new Pregunta()));
        vm.recargarDetalle();
        assertSame(anterior, estado());
        assertTrue(source.detalles.get(0).canceled);
        assertTrue(source.preguntas.get(0).canceled);
        assertTrue(source.visitas.get(0).canceled);
        assertEquals(1, source.detalles.size());
    }

    @Test
    public void noConsultaNiRegistraUnaPublicacionSinId() {
        vm.inicializar(new Publicacion());
        assertEquals(PublicationDetailSource.LoadError.NOT_FOUND, estado().getError());
        assertTrue(source.detalles.isEmpty());
        assertTrue(source.visitas.isEmpty());
    }

    private DetalleUiState estado() { return vm.getEstado().getValue(); }

    private static Publicacion publicacion(String titulo) {
        Publicacion p = new Publicacion();
        p.setId("1");
        p.setTitle(titulo);
        return p;
    }

    private static class Pending<T> implements PublicationDetailSource.Request {
        boolean canceled;
        final PublicationDetailSource.Result<T> result;
        Pending(PublicationDetailSource.Result<T> result) { this.result = result; }
        void success(T value) { result.onSuccess(value); }
        void error(PublicationDetailSource.LoadError error) { result.onError(error); }
        @Override public void cancel() { canceled = true; }
    }

    private static class FakeSource implements PublicationDetailSource {
        final List<Pending<Publicacion>> detalles = new ArrayList<>();
        final List<Pending<List<Pregunta>>> preguntas = new ArrayList<>();
        final List<Pending<Void>> visitas = new ArrayList<>();
        @Override public Request getDetail(String id, Result<Publicacion> result) {
            Pending<Publicacion> pendiente = new Pending<>(result);
            detalles.add(pendiente);
            return pendiente;
        }
        @Override public Request getQuestions(String id, Result<List<Pregunta>> result) {
            Pending<List<Pregunta>> pendiente = new Pending<>(result);
            preguntas.add(pendiente);
            return pendiente;
        }
        @Override public Request recordView(String id) {
            Pending<Void> pendiente = new Pending<>(null);
            visitas.add(pendiente);
            return pendiente;
        }
    }
}
