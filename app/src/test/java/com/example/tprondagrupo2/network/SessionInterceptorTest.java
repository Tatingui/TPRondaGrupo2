package com.example.tprondagrupo2.network;

import org.junit.Test;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import static org.junit.Assert.*;

public class SessionInterceptorTest {
    @Test public void leeElTokenActualEnCadaPeticionYRespetaElLogout() throws Exception {
        AtomicReference<String> token = new AtomicReference<>("session-a");
        SessionInterceptor interceptor = new SessionInterceptor(token::get, () -> fail("No es 401"));
        assertEquals("Bearer session-a", intercept(interceptor, 200).header("Authorization"));
        token.set("session-b");
        assertEquals("Bearer session-b", intercept(interceptor, 200).header("Authorization"));
        token.set(null);
        assertNull(intercept(interceptor, 200).header("Authorization"));
    }

    @Test public void noEnviaBearerVacio() throws Exception {
        SessionInterceptor interceptor = new SessionInterceptor(() -> "  ", () -> fail("No es 401"));
        assertNull(intercept(interceptor, 200).header("Authorization"));
    }

    @Test public void un401DisparaElManejoDeSesionUnaVez() throws Exception {
        AtomicInteger events = new AtomicInteger();
        SessionInterceptor interceptor = new SessionInterceptor(() -> "expired-test-session", events::incrementAndGet);
        intercept(interceptor, 401);
        assertEquals(1, events.get());
    }

    @Test public void un403O500NoCierraLaSesion() throws Exception {
        SessionInterceptor interceptor = new SessionInterceptor(() -> "test-session", () -> fail("No es 401"));
        intercept(interceptor, 403);
        intercept(interceptor, 500);
    }

    @Test public void unaFallaDeRedNoSeConvierteEnSesionVencida() {
        SessionInterceptor interceptor = new SessionInterceptor(() -> "test-session", () -> fail("No hubo HTTP"));
        assertThrows(IOException.class, () -> interceptor.intercept(chain(-1, new AtomicReference<>())));
    }

    private Request intercept(SessionInterceptor interceptor, int code) throws IOException {
        AtomicReference<Request> sent = new AtomicReference<>();
        try (Response response = interceptor.intercept(chain(code, sent))) {
            assertEquals(code, response.code());
        }
        return sent.get();
    }

    private Interceptor.Chain chain(int code, AtomicReference<Request> sent) {
        Request original = new Request.Builder().url("https://example.test/publications").build();
        return (Interceptor.Chain) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Interceptor.Chain.class}, (proxy, method, args) -> {
                    if (method.getName().equals("request")) return original;
                    if (method.getName().equals("proceed")) {
                        if (code < 0) throw new IOException("Sin conexión");
                        sent.set((Request) args[0]);
                        return new Response.Builder().request(sent.get()).protocol(Protocol.HTTP_1_1)
                                .code(code).message("Test").body(ResponseBody.create(null, "")).build();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
