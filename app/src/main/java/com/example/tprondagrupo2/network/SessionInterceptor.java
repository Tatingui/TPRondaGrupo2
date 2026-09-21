package com.example.tprondagrupo2.network;

import java.io.IOException;
import java.util.function.Supplier;
import javax.inject.Inject;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Lee la sesión en cada petición, no al construir el cliente HTTP. */
public final class SessionInterceptor implements Interceptor {
    private final Supplier<String> token;
    private final Runnable onUnauthorized;

    @Inject
    public SessionInterceptor(TokenManager tokenManager, SessionManager sessionManager) {
        this(tokenManager::getToken, () -> {
            tokenManager.clearToken();
            sessionManager.notifySessionExpired();
        });
    }

    public SessionInterceptor(Supplier<String> token, Runnable onUnauthorized) {
        this.token = token;
        this.onUnauthorized = onUnauthorized;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder request = chain.request().newBuilder();
        String currentToken = token.get();
        if (currentToken != null && !currentToken.trim().isEmpty()) {
            request.header("Authorization", "Bearer " + currentToken);
        }
        Response response = chain.proceed(request.build());
        if (response.code() == 401) onUnauthorized.run();
        return response;
    }
}
