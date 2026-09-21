package com.example.tprondagrupo2.ui.profile;

import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.network.OfferApiService;
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

    @Test
    public void failedSentLoadDoesNotDisplayReceivedOffers() {
        MyOffersViewModel vm = new MyOffersViewModel(api, null, null);
        vm.fetchOffers(true);
        received.respond(Response.success(Collections.singletonList(new Offer())));
        assertEquals(1, vm.getOffers().getValue().size());
        vm.fetchOffers(false);
        assertTrue(vm.getOffers().getValue().isEmpty());
        sent.respond(Response.error(500, ResponseBody.create(null, "error")));
        assertTrue(vm.getOffers().getValue().isEmpty());
        assertEquals("Error cargando ofertas: 500", vm.getErrorMessage().getValue());
        assertFalse(vm.getLoading().getValue());
    }

    @Test
    public void lateResponseCannotOverwriteTheSelectedTab() {
        MyOffersViewModel vm = new MyOffersViewModel(api, null, null);
        vm.fetchOffers(true);
        vm.fetchOffers(false);
        assertTrue(received.isCanceled());
        received.respond(Response.success(Collections.singletonList(new Offer())));
        assertTrue(vm.getOffers().getValue().isEmpty());
        assertTrue(vm.getLoading().getValue());
        Offer purchase = new Offer();
        sent.respond(Response.success(Collections.singletonList(purchase)));
        received.fail(new IllegalStateException("late failure"));
        assertSame(purchase, vm.getOffers().getValue().get(0));
        assertNull(vm.getErrorMessage().getValue());
        assertFalse(vm.getLoading().getValue());
    }
}
