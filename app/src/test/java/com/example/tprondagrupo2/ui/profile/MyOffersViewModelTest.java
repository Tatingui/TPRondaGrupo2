package com.example.tprondagrupo2.ui.profile;

import com.example.tprondagrupo2.data.repository.OfferRepository;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.network.HistorialApiService;
import com.example.tprondagrupo2.network.OfferApiService;
import com.example.tprondagrupo2.network.PublicationReadApiService;
import com.example.tprondagrupo2.support.FakeCall;
import com.example.tprondagrupo2.support.ImmediateMainThreadRule;
import org.junit.Rule;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;
import okhttp3.ResponseBody;
import static org.junit.Assert.*;

public class MyOffersViewModelTest {
    @Rule public final ImmediateMainThreadRule mainThread = new ImmediateMainThreadRule();
    private final FakeCall<List<Offer>> received = new FakeCall<>();
    private final FakeCall<List<Offer>> sent = new FakeCall<>();
    private final OfferApiService api = (OfferApiService) Proxy.newProxyInstance(
            OfferApiService.class.getClassLoader(), new Class<?>[]{OfferApiService.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getReceivedOffers")) return received;
                if (method.getName().equals("getSentOffers")) return sent;
                throw new AssertionError("Unexpected API call: " + method.getName());
            });

    private final HistorialApiService historialApi = (HistorialApiService) Proxy.newProxyInstance(
            HistorialApiService.class.getClassLoader(), new Class<?>[]{HistorialApiService.class},
            (proxy, method, args) -> { throw new AssertionError("Unexpected call: " + method.getName()); });

    private final PublicationReadApiService pubApi = (PublicationReadApiService) Proxy.newProxyInstance(
            PublicationReadApiService.class.getClassLoader(), new Class<?>[]{PublicationReadApiService.class},
            (proxy, method, args) -> { throw new AssertionError("Unexpected call: " + method.getName()); });

    private MyOffersViewModel createVm() {
        return new MyOffersViewModel(new OfferRepository(api, historialApi, pubApi));
    }

    @Test
    public void failedSentLoadSetsError() {
        MyOffersViewModel vm = createVm();
        vm.fetchOffers(true);
        received.respond(Response.success(Collections.singletonList(new Offer())));
        assertEquals(1, vm.getOffers().getValue().size());
        vm.fetchOffers(false);
        assertTrue(vm.getOffers().getValue().isEmpty());
        sent.respond(Response.error(500, ResponseBody.create(null, "error")));
        assertNotNull(vm.getErrorMessage().getValue());
        assertFalse(vm.getLoading().getValue());
    }

    @Test
    public void successfulFetchSetsOffers() {
        MyOffersViewModel vm = createVm();
        vm.fetchOffers(true);
        Offer offer = new Offer();
        received.respond(Response.success(Collections.singletonList(offer)));
        assertEquals(1, vm.getOffers().getValue().size());
        assertSame(offer, vm.getOffers().getValue().get(0));
        assertFalse(vm.getLoading().getValue());
    }
}
