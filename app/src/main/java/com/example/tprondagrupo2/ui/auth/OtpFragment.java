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
import com.example.tprondagrupo2.data.repository.AuthRepository;
import com.example.tprondagrupo2.network.AuthApiService;
import com.example.tprondagrupo2.network.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpFragment extends Fragment {

    @Inject
    AuthApiService authApiService;

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

    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TokenManager tokenManager = TokenManager.getInstance();
        authRepository = new AuthRepository(authApiService, tokenManager);

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
            showError("El codigo debe tener 6 digitos");
            return;
        }

        hideError();
        setLoading(true);

        authRepository.verifyOtp(email, code, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String token) {
                if (!isAdded()) return;
                setLoading(false);
                NavHostFragment.findNavController(OtpFragment.this)
                        .navigate(R.id.action_otp_to_home);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                setLoading(false);
                showError(message);
            }

            @Override
            public void onUnverified(String email) {
                // No aplica para verificacion OTP
            }

            @Override
            public void onNetworkError() {
                if (!isAdded()) return;
                setLoading(false);
                showError("Error de conexion");
            }
        });
    }

    private void doResend() {
        hideError();
        startResendCooldown();

        authRepository.resendOtp(email, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                showError("Codigo reenviado");
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                showError(message);
            }

            @Override
            public void onNetworkError() {
                if (!isAdded()) return;
                showError("Error de conexion");
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
        resendHandler.removeCallbacks(enableResendRunnable);
        btnResend = null;
    }
}
