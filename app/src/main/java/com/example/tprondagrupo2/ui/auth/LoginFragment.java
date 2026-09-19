package com.example.tprondagrupo2.ui.auth;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.LoginRequest;
import com.example.tprondagrupo2.model.OtpSendRequest;
import com.example.tprondagrupo2.network.AuthApiService;
import com.example.tprondagrupo2.network.TokenManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.Executor;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private static final int ALLOWED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

    @Inject
    AuthApiService authApiService;

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnLogin;
    private Button btnLoginOtp;
    private Button btnGoRegister;
    private SwitchMaterial switchKeepSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLoginOtp = view.findViewById(R.id.btnLoginOtp);
        btnGoRegister = view.findViewById(R.id.btnGoRegister);
        switchKeepSession = view.findViewById(R.id.switchKeepSession);

        btnLogin.setOnClickListener(v -> doLogin());
        btnLoginOtp.setOnClickListener(v -> doSendOtp());
        btnGoRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));

        // Restaurar el estado del switch
        switchKeepSession.setChecked(TokenManager.getInstance().isKeepSession());

        // Si la biometría está habilitada, mostrar el prompt automáticamente
        // sobre la pantalla de login normal
        TokenManager tokenManager = TokenManager.getInstance();
        if (tokenManager.isBiometricEnabled() && tokenManager.getEncryptedToken() != null && !tokenManager.isKeepSession()) {
            showBiometricPrompt();
        }
    }

    // ── Biometría ──

    private void showBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        int canAuth = biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            Log.w(TAG, "Biometría no disponible, código: " + canAuth);
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(requireContext());

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (!isAdded()) return;

                        String encryptedToken = TokenManager.getInstance().getEncryptedToken();
                        if (encryptedToken != null) {
                            TokenManager.getInstance().saveToken(encryptedToken);
                            goToHome();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Token expirado. Iniciá sesión con tus credenciales.",
                                    Toast.LENGTH_LONG).show();
                            TokenManager.getInstance().setBiometricEnabled(false);
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                     @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (!isAdded()) return;
                        Log.w(TAG, "Error biométrico (" + errorCode + "): " + errString);
                        // El usuario canceló → queda en el login normal
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Iniciar sesión en Ronda")
                .setSubtitle("Usá tu huella digital o credencial del dispositivo")
                .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Después de un login exitoso, ofrece al usuario activar biometría para la próxima vez.
     */
    private void ofrecerBiometria(String token) {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        int canAuth = biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            goToHome();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Acceso biométrico")
                .setMessage("¿Querés usar tu huella digital para iniciar sesión la próxima vez?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    TokenManager tm = TokenManager.getInstance();
                    tm.setBiometricEnabled(true);
                    tm.saveEncryptedToken(token);
                    goToHome();
                })
                .setNegativeButton("No, gracias", (dialog, which) -> goToHome())
                .setCancelable(false)
                .show();
    }

    private void goToHome() {
        if (!isAdded()) return;
        NavHostFragment.findNavController(this).navigate(R.id.action_login_to_home);
    }

    // ── Login con credenciales ──

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Completá email y contraseña");
            return;
        }

        hideError();
        setLoading(true);

        authApiService.login(new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        if (!isAdded()) return;
                        setLoading(false);

                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            String token = body.getToken();
                            if (token != null) {
                                TokenManager.getInstance().saveToken(token);
                            }

                            // Guardar preferencia de mantener sesión
                            TokenManager.getInstance()
                                    .setKeepSession(switchKeepSession.isChecked());

                            // Si la biometría no está habilitada, ofrecer activarla
                            if (!TokenManager.getInstance().isBiometricEnabled()) {
                                ofrecerBiometria(token);
                            } else {
                                // Ya tiene biometría, actualizar el token encriptado
                                TokenManager.getInstance().saveEncryptedToken(token);
                                goToHome();
                            }
                        } else {
                            String msg = extractMessage(body, "");
                            if (msg.contains("no verificado")) {
                                Bundle args = new Bundle();
                                args.putString("email", email);
                                NavHostFragment.findNavController(LoginFragment.this)
                                        .navigate(R.id.action_login_to_otp, args);
                            } else {
                                showError(extractMessage(body, "No se pudo iniciar sesión"));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        setLoading(false);
                        showError("Error de conexión");
                    }
                });
    }

    private void doSendOtp() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            showError("Ingresá tu email para recibir el código");
            return;
        }

        hideError();
        setLoading(true);

        authApiService.sendOtp(new OtpSendRequest(email))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        if (!isAdded()) return;
                        setLoading(false);

                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            Bundle args = new Bundle();
                            args.putString("email", email);

                            NavController navController =
                                    NavHostFragment.findNavController(LoginFragment.this);
                            navController.navigate(R.id.action_login_to_otp, args);
                        } else {
                            showError(extractMessage(body, "No se pudo enviar el código"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        setLoading(false);
                        showError("Error de conexión");
                    }
                });
    }

    private String extractMessage(@Nullable AuthResponse body, String fallback) {
        if (body != null && body.getMessage() != null && !body.getMessage().isEmpty()) {
            return body.getMessage();
        }
        return fallback;
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
