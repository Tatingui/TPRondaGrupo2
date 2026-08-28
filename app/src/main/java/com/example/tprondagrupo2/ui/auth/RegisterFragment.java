package com.example.tprondagrupo2.ui.auth;

import android.os.Bundle;
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

import android.util.Log;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.RegisterRequest;
import com.example.tprondagrupo2.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RONDA_REGISTER";

    private EditText etNombre;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnRegister;
    private Button btnGoLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNombre = view.findViewById(R.id.etNombre);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoLogin = view.findViewById(R.id.btnGoLogin);

        btnRegister.setOnClickListener(v -> doRegister());
        btnGoLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_register_to_login));
    }

    private void doRegister() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Completá todos los campos");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden");
            return;
        }

        hideError();
        setLoading(true);

        Log.d(TAG, "Intentando registro: nombre=" + nombre + " email=" + email);

        RegisterRequest req = new RegisterRequest(nombre, email, password);
        Log.d(TAG, "RegisterRequest creado, llamando a ApiClient.getAuthService().register()");

        ApiClient.getAuthService().register(req)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        Log.d(TAG, "onResponse: code=" + response.code());
                        if (!isAdded()) {
                            Log.w(TAG, "Fragment not added, ignorando respuesta");
                            return;
                        }
                        setLoading(false);

                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            Log.d(TAG, "Registro exitoso, navegando a OTP");
                            Bundle args = new Bundle();
                            args.putString("email", email);

                            NavHostFragment.findNavController(RegisterFragment.this)
                                    .navigate(R.id.action_register_to_otp, args);
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception ignored) {}
                            Log.e(TAG, "Registro fallido: code=" + response.code()
                                    + " body=" + body + " errorBody=" + errorBody);
                            showError(extractMessage(body, "No se pudo crear la cuenta"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getClass().getName() + ": " + t.getMessage(), t);
                        if (!isAdded()) {
                            return;
                        }
                        setLoading(false);
                        showError("Error de conexión: " + t.getMessage());
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
        btnRegister.setVisibility(loading ? View.GONE : View.VISIBLE);
        btnGoLogin.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}
