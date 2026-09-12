package com.example.tprondagrupo2.ui.auth;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
    private EditText etTelefono;
    private AutoCompleteTextView etZona;
    private TextView tvError;
    private TextView tvPasswordStrength;
    private ProgressBar progressBar;
    private Button btnRegister;
    private Button btnGoLogin;

    /** Nivel de fuerza actual de la contraseña (0=vacía, 1=débil, 2=media, 3=fuerte) */
    private int passwordStrengthLevel = 0;

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
        etTelefono = view.findViewById(R.id.etTelefono);
        etZona = view.findViewById(R.id.etZona);
        tvError = view.findViewById(R.id.tvError);
        tvPasswordStrength = view.findViewById(R.id.tvPasswordStrength);
        progressBar = view.findViewById(R.id.progressBar);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoLogin = view.findViewById(R.id.btnGoLogin);

        setupPasswordStrengthWatcher();
        setupZonaAutoComplete();
        setupPhoneFormatter();

        btnRegister.setOnClickListener(v -> doRegister());
        btnGoLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_register_to_login));
    }

    // ==================== VALIDACIÓN DE CONTRASEÑA ====================

    private void setupPasswordStrengthWatcher() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordStrength(s.toString());
            }
        });
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            tvPasswordStrength.setVisibility(View.GONE);
            passwordStrengthLevel = 0;
            return;
        }

        tvPasswordStrength.setVisibility(View.VISIBLE);

        if (password.length() < 8) {
            tvPasswordStrength.setText(getString(R.string.password_too_short));
            tvPasswordStrength.setTextColor(Color.parseColor("#D32F2F")); // rojo
            passwordStrengthLevel = 0;
            return;
        }

        // Contar criterios cumplidos
        int criteria = 0;
        if (password.matches(".*[a-z].*")) criteria++; // minúscula
        if (password.matches(".*[A-Z].*")) criteria++; // mayúscula
        if (password.matches(".*\\d.*"))   criteria++; // número
        if (password.matches(".*[^a-zA-Z\\d].*")) criteria++; // símbolo

        if (criteria <= 1) {
            tvPasswordStrength.setText(getString(R.string.password_strength_weak));
            tvPasswordStrength.setTextColor(Color.parseColor("#D32F2F")); // rojo
            passwordStrengthLevel = 1;
        } else if (criteria <= 2) {
            tvPasswordStrength.setText(getString(R.string.password_strength_medium));
            tvPasswordStrength.setTextColor(Color.parseColor("#F57C00")); // naranja
            passwordStrengthLevel = 2;
        } else {
            tvPasswordStrength.setText(getString(R.string.password_strength_strong));
            tvPasswordStrength.setTextColor(Color.parseColor("#388E3C")); // verde
            passwordStrengthLevel = 3;
        }
    }

    // ==================== AUTOCOMPLETE DE ZONA ====================

    private void setupZonaAutoComplete() {
        String[] zonas = getResources().getStringArray(R.array.zonas_argentina);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                zonas
        );
        etZona.setAdapter(adapter);
    }

    // ==================== FORMATEO DE TELÉFONO ====================

    private void setupPhoneFormatter() {
        etTelefono.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                // Sacar todo lo que no sea dígito
                String digits = s.toString().replaceAll("[^\\d]", "");

                // Formatear: 11 3050-9485 (2 + 4 + 4)
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length() && i < 10; i++) {
                    if (i == 2 || i == 6) formatted.append(" ");
                    formatted.append(digits.charAt(i));
                }

                etTelefono.setText(formatted.toString());
                etTelefono.setSelection(formatted.length());

                isFormatting = false;
            }
        });
    }

    // ==================== REGISTRO ====================

    private void doRegister() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String zona = etZona.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Completá todos los campos obligatorios");
            return;
        }

        if (password.length() < 8) {
            showError("La contraseña debe tener al menos 8 caracteres");
            return;
        }

        if (passwordStrengthLevel < 2) {
            showError("La contraseña es muy débil. Usá mayúsculas, minúsculas y números");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden");
            return;
        }

        hideError();
        setLoading(true);

        Log.d(TAG, "Intentando registro: nombre=" + nombre + " email=" + email);

        RegisterRequest req = new RegisterRequest(nombre, email, password,
                telefono.isEmpty() ? null : telefono,
                zona.isEmpty() ? null : zona);
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
