package com.example.tprondagrupo2.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.data.repository.AuthRepository;
import com.example.tprondagrupo2.network.TokenManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    @Inject
    AuthRepository authRepository;

    @Inject
    TokenManager tokenManager;

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnLogin;
    private Button btnLoginOtp;
    private Button btnGoRegister;
    private SwitchMaterial switchKeepSession;

    private BiometricHelper biometricHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        biometricHelper = new BiometricHelper(tokenManager);

        bindViews(view);
        setupListeners();

        switchKeepSession.setChecked(tokenManager.isKeepSession());

        if (biometricHelper.shouldPromptBiometric()) {
            biometricHelper.showPrompt(this, biometricCallback);
        }
    }

    private final BiometricHelper.BiometricCallback biometricCallback =
            new BiometricHelper.BiometricCallback() {
                @Override
                public void onBiometricSuccess() {
                    navigateToHome();
                }

                @Override
                public void onBiometricTokenExpired() {
                    Toast.makeText(requireContext(),
                            "Token expirado. Inicia sesion con tus credenciales.",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onBiometricCancelled() {
                }
            };

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Completa email y contrasena");
            return;
        }

        hideError();
        setLoading(true);

        authRepository.login(email, password, switchKeepSession.isChecked(),
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(String token) {
                        if (!isAdded()) return;
                        setLoading(false);
                        handleLoginSuccess(token);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;
                        setLoading(false);
                        showError(message);
                    }

                    @Override
                    public void onUnverified(String email) {
                        if (!isAdded()) return;
                        setLoading(false);
                        navigateToOtp(email);
                    }

                    @Override
                    public void onNetworkError() {
                        if (!isAdded()) return;
                        setLoading(false);
                        showError("Error de conexion");
                    }
                });
    }

    private void handleLoginSuccess(String token) {
        TokenManager tm = tokenManager;
        if (!tm.isBiometricEnabled()) {
            biometricHelper.offerBiometricEnrollment(
                    requireContext(), token, this::navigateToHome);
        } else {
            tm.saveEncryptedToken(token);
            navigateToHome();
        }
    }

    private void doSendOtp() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            showError("Ingresa tu email para recibir el codigo");
            return;
        }

        hideError();
        setLoading(true);

        authRepository.sendOtp(email, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                setLoading(false);
                navigateToOtp(email);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                setLoading(false);
                showError(message);
            }

            @Override
            public void onNetworkError() {
                if (!isAdded()) return;
                setLoading(false);
                showError("Error de conexion");
            }
        });
    }

    private void navigateToHome() {
        if (!isAdded()) return;
        NavHostFragment.findNavController(this).navigate(R.id.action_login_to_home);
    }

    private void navigateToOtp(String email) {
        Bundle args = new Bundle();
        args.putString("email", email);
        NavHostFragment.findNavController(this).navigate(R.id.action_login_to_otp, args);
    }

    private void bindViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLoginOtp = view.findViewById(R.id.btnLoginOtp);
        btnGoRegister = view.findViewById(R.id.btnGoRegister);
        switchKeepSession = view.findViewById(R.id.switchKeepSession);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> doLogin());
        btnLoginOtp.setOnClickListener(v -> doSendOtp());
        btnGoRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setVisibility(loading ? View.GONE : View.VISIBLE);
        btnLoginOtp.setEnabled(!loading);
        btnGoRegister.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}
