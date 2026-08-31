package com.example.tprondagrupo2.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.OtpRequest;
import com.example.tprondagrupo2.model.OtpSendRequest;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpFragment extends Fragment {

    public static final String ARG_EMAIL = "email";

    private static final int CODE_LENGTH = 6;
    private static final long RESEND_COOLDOWN_MS = 30_000L;

    private final Handler resendHandler = new Handler(Looper.getMainLooper());
    private final Runnable enableResendRunnable = this::enableResendButton;

    private String email;

    private TextView tvOtpMessage;
    private EditText etCode;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnVerify;
    private Button btnResend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = getArguments() != null ? getArguments().getString(ARG_EMAIL, "") : "";

        tvOtpMessage = view.findViewById(R.id.tvOtpMessage);
        etCode = view.findViewById(R.id.etCode);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);
        btnVerify = view.findViewById(R.id.btnVerify);
        btnResend = view.findViewById(R.id.btnResend);

        tvOtpMessage.setText(getString(R.string.otp_message, email));

        btnVerify.setOnClickListener(v -> doVerify());
        btnResend.setOnClickListener(v -> doResend());
    }

    private void doVerify() {
        String code = etCode.getText().toString().trim();

        if (code.length() != CODE_LENGTH) {
            showError("El código debe tener 6 dígitos");
            return;
        }

        hideError();
        setLoading(true);

        ApiClient.getAuthService().verifyOtp(new OtpRequest(email, code))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        if (!isAdded()) {
                            return;
                        }
                        setLoading(false);

                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            if (body.getToken() != null) {
                                TokenManager.getInstance().saveToken(body.getToken());
                            }
                            NavHostFragment.findNavController(OtpFragment.this)
                                    .navigate(R.id.action_otp_to_home);
                        } else {
                            showError(extractMessage(body, "Código inválido"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) {
                            return;
                        }
                        setLoading(false);
                        showError("Error de conexión");
                    }
                });
    }

    private void doResend() {
        hideError();
        startResendCooldown();

        ApiClient.getAuthService().resendOtp(new OtpSendRequest(email))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        if (!isAdded()) {
                            return;
                        }

                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            showError("Código reenviado");
                        } else {
                            showError(extractMessage(body, "No se pudo reenviar el código"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) {
                            return;
                        }
                        showError("Error de conexión");
                    }
                });
    }

    private void startResendCooldown() {
        btnResend.setEnabled(false);
        resendHandler.removeCallbacks(enableResendRunnable);
        resendHandler.postDelayed(enableResendRunnable, RESEND_COOLDOWN_MS);
    }

    private void enableResendButton() {
        if (btnResend != null) {
            btnResend.setEnabled(true);
        }
    }

    private String extractMessage(@Nullable AuthResponse body, String fallback) {
        if (body != null && body.getMessage() != null && !body.getMessage().isEmpty()) {
            return body.getMessage();
        }
        return fallback;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Evita que el Handler dispare sobre views ya destruidas
        resendHandler.removeCallbacks(enableResendRunnable);
        btnResend = null;
    }
}
