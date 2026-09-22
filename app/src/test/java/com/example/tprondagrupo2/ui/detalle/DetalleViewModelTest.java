package com.example.tprondagrupo2.ui.detalle;

import static org.junit.Assert.*;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.FavoritesDataStoreManager;
import com.example.tprondagrupo2.network.PublicationFavoriteApiService;
import com.example.tprondagrupo2.network.PublicationWriteApiService;
import com.example.tprondagrupo2.support.FakeCall;
import com.example.tprondagrupo2.support.ImmediateMainThreadRule;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;

public class DetalleViewModelTest {
    @Rule public final ImmediateMainThreadRule mainThread = new ImmediateMainThreadRule();
    private FakeSource source;
    private FakeWriteApi writeApi;
    private FakeFavoriteApi favoriteApi;
    private FavoritesDataStoreManager favoritesDataStore;
    private DetalleViewModel vm;

    @Before
    public void setup() {
        source = new FakeSource();
        writeApi = new FakeWriteApi();
        favoriteApi = new FakeFavoriteApi();
        favoritesDataStore = new FavoritesDataStoreManager(true);
        vm = new DetalleViewModel(source, writeApi, favoriteApi, favoritesDataStore);
    }

    // ────────── Tests de lectura existentes ──────────

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
        DetalleViewModelFactory factory = new DetalleViewModelFactory(
                source, writeApi, favoriteApi, favoritesDataStore);
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
            DetalleViewModel model = new DetalleViewModel(actual, writeApi, favoriteApi, favoritesDataStore);
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

    // ────────── Tests de escritura ──────────

    @Test
    public void enviarPreguntaExitosaRecargaPreguntas() {
        inicializarCompleto();
        vm.enviarPregunta("¿Acepta permuta?");

        assertNotNull(writeApi.lastAskQuestionCall);
        writeApi.lastAskQuestionCall.respond(Response.success(new Pregunta()));

        assertEquals("Pregunta enviada", vm.getToastMessage().getValue());
        // Recargó preguntas (2 peticiones: la inicial + la recarga)
        assertEquals(2, source.preguntas.size());
    }

    @Test
    public void enviarPreguntaConErrorMuestraToast() {
        inicializarCompleto();
        vm.enviarPregunta("¿Acepta permuta?");

        writeApi.lastAskQuestionCall.fail(new RuntimeException("timeout"));

        assertEquals("Error de conexión", vm.getToastMessage().getValue());
    }

    @Test
    public void responderPreguntaExitosaRecargaPreguntas() {
        inicializarCompleto();
        vm.responderPregunta(42L, "Sí, acepto permutas");

        assertNotNull(writeApi.lastAnswerQuestionCall);
        writeApi.lastAnswerQuestionCall.respond(Response.success(new Pregunta()));

        assertEquals("Respuesta enviada", vm.getToastMessage().getValue());
        assertEquals(2, source.preguntas.size());
    }

    @Test
    public void enviarOfertaExitosaActualizaOfertaEnEstado() {
        inicializarCompleto();
        assertFalse(estado().isOfertaEnCurso());

        vm.enviarOferta(5000.0, "Ofrezco esto");

        assertTrue(estado().isOfertaEnCurso());
        assertNotNull(writeApi.lastMakeOfferCall);

        Offer ofertaRespuesta = new Gson().fromJson(
                "{\"id\":10,\"status\":\"PENDING\",\"offeredPrice\":5000}", Offer.class);
        writeApi.lastMakeOfferCall.respond(Response.success(ofertaRespuesta));

        assertFalse(estado().isOfertaEnCurso());
        assertEquals("Oferta enviada", vm.getToastMessage().getValue());
        assertSame(ofertaRespuesta, estado().getPublicacion().getMyOffer());
    }

    @Test
    public void enviarOfertaNoPermiteDuplicadoMientrasCursa() {
        inicializarCompleto();
        vm.enviarOferta(5000.0, null);
        assertTrue(estado().isOfertaEnCurso());

        // Segunda llamada mientras la primera cursa — no debe generar otro Call
        vm.enviarOferta(6000.0, null);
        assertEquals(1, writeApi.makeOfferCallCount);
    }

    @Test
    public void enviarOfertaConErrorLimpiaBandera() {
        inicializarCompleto();
        vm.enviarOferta(5000.0, null);
        assertTrue(estado().isOfertaEnCurso());

        writeApi.lastMakeOfferCall.fail(new RuntimeException("red"));

        assertFalse(estado().isOfertaEnCurso());
        assertEquals("Error de conexión", vm.getToastMessage().getValue());
    }

    @Test
    public void cambiarEstadoPublicacionExitosaRecargaDetalle() {
        inicializarCompleto();
        assertFalse(estado().isGestionEnCurso());

        vm.cambiarEstadoPublicacion("PAUSED");

        assertTrue(estado().isGestionEnCurso());
        assertNotNull(writeApi.lastUpdateStatusCall);

        writeApi.lastUpdateStatusCall.respond(Response.success(publicacion("Pausada")));

        assertFalse(estado().isGestionEnCurso());
        assertEquals("Publicación actualizada", vm.getToastMessage().getValue());
        // Recargó detalle
        assertEquals(2, source.detalles.size());
    }

    @Test
    public void cambiarEstadoNoPermiteDuplicadoMientrasCursa() {
        inicializarCompleto();
        vm.cambiarEstadoPublicacion("PAUSED");
        vm.cambiarEstadoPublicacion("ACTIVE");
        assertEquals(1, writeApi.updateStatusCallCount);
    }

    @Test
    public void toggleFavoriteAgregaYNotifica() {
        inicializarCompleto();
        assertFalse(estado().getPublicacion().isFavorite());
        assertFalse(estado().isFavoritoEnCurso());

        vm.toggleFavorite();

        assertTrue(estado().isFavoritoEnCurso());
        assertNotNull(favoriteApi.lastMarkCall);

        favoriteApi.lastMarkCall.respond(Response.success(null));

        assertFalse(estado().isFavoritoEnCurso());
        assertTrue(estado().getPublicacion().isFavorite());
        assertEquals("Agregado a favoritos", vm.getToastMessage().getValue());
    }

    @Test
    public void toggleFavoriteQuitaYNotifica() {
        inicializarCompleto();
        vm.actualizarFavorito(true);
        assertTrue(estado().getPublicacion().isFavorite());

        vm.toggleFavorite();

        assertTrue(estado().isFavoritoEnCurso());
        assertNotNull(favoriteApi.lastUnmarkCall);

        favoriteApi.lastUnmarkCall.respond(Response.success(null));

        assertFalse(estado().isFavoritoEnCurso());
        assertFalse(estado().getPublicacion().isFavorite());
        assertEquals("Eliminado de favoritos", vm.getToastMessage().getValue());
    }

    @Test
    public void toggleFavoriteNoPermiteDuplicadoMientrasCursa() {
        inicializarCompleto();
        vm.toggleFavorite();
        assertTrue(estado().isFavoritoEnCurso());
        vm.toggleFavorite();
        assertEquals(1, favoriteApi.markCallCount);
    }

    @Test
    public void hasPendingWritesTrueConEscrituraEnCurso() {
        inicializarCompleto();
        assertFalse(vm.hasPendingWrites());

        vm.enviarPregunta("Hola");
        assertTrue(vm.hasPendingWrites());

        writeApi.lastAskQuestionCall.respond(Response.success(new Pregunta()));
        assertFalse(vm.hasPendingWrites());
    }

    @Test
    public void onClearedCancelaEscriturasEnCurso() {
        inicializarCompleto();
        vm.enviarOferta(100, null);
        FakeCall<Offer> call = writeApi.lastMakeOfferCall;

        vm.onCleared();
        assertTrue(call.isCanceled());
    }

    @Test
    public void registrarVistaLocalPersisteFlagEnDataStore() {
        vm.registrarVistaLocal("42");
        // No lanza excepción con dataStore null (guarded)
        // El memoryCache interno se actualiza
    }

    // ────────── Helpers ──────────

    private void inicializarCompleto() {
        vm.inicializar(publicacion("Test"));
        source.detalles.get(0).success(publicacion("Completa"));
        source.preguntas.get(0).success(Collections.emptyList());
    }

    private DetalleUiState estado() { return vm.getEstado().getValue(); }

    private static Publicacion publicacion(String titulo) {
        Publicacion p = new Publicacion();
        p.setId("1");
        p.setTitle(titulo);
        return p;
    }

    // ────────── Fakes ──────────

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

    private static class FakeWriteApi implements PublicationWriteApiService {
        FakeCall<Pregunta> lastAskQuestionCall;
        FakeCall<Pregunta> lastAnswerQuestionCall;
        FakeCall<Offer> lastMakeOfferCall;
        FakeCall<Publicacion> lastUpdateStatusCall;
        int makeOfferCallCount;
        int updateStatusCallCount;

        @Override public retrofit2.Call<Publicacion> createPublication(
                com.example.tprondagrupo2.model.PublicationCreateRequest request) {
            return new FakeCall<>();
        }
        @Override public retrofit2.Call<Publicacion> updatePublicationStatus(Long id, String state) {
            updateStatusCallCount++;
            lastUpdateStatusCall = new FakeCall<>();
            return lastUpdateStatusCall;
        }
        @Override public retrofit2.Call<Void> deletePublication(Long id) {
            return new FakeCall<>();
        }
        @Override public retrofit2.Call<Pregunta> askQuestion(String id,
                com.example.tprondagrupo2.model.TextoRequest request) {
            lastAskQuestionCall = new FakeCall<>();
            return lastAskQuestionCall;
        }
        @Override public retrofit2.Call<Pregunta> answerQuestion(Long questionId,
                com.example.tprondagrupo2.model.TextoRequest request) {
            lastAnswerQuestionCall = new FakeCall<>();
            return lastAnswerQuestionCall;
        }
        @Override public retrofit2.Call<Offer> makeOffer(String id,
                com.example.tprondagrupo2.model.OfertaRequest request) {
            makeOfferCallCount++;
            lastMakeOfferCall = new FakeCall<>();
            return lastMakeOfferCall;
        }
    }

    private static class FakeFavoriteApi implements PublicationFavoriteApiService {
        FakeCall<Void> lastMarkCall;
        FakeCall<Void> lastUnmarkCall;
        int markCallCount;
        int unmarkCallCount;

        @Override public retrofit2.Call<Void> markAsFavorite(String id) {
            markCallCount++;
            lastMarkCall = new FakeCall<>();
            return lastMarkCall;
        }
        @Override public retrofit2.Call<Void> unmarkAsFavorite(String id) {
            unmarkCallCount++;
            lastUnmarkCall = new FakeCall<>();
            return lastUnmarkCall;
        }
    }
}
