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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.LoginRequest;
import com.example.tprondagrupo2.model.OtpSendRequest;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvError;
    private ProgressBar progressBar;
    private Button btnLogin;
    private Button btnLoginOtp;
    private Button btnGoRegister;

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

        btnLogin.setOnClickListener(v -> doLogin());
        btnLoginOtp.setOnClickListener(v -> doSendOtp());
        btnGoRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Completá email y contraseña");
            return;
        }

        hideError();
        setLoading(true);

        ApiClient.getAuthService().login(new LoginRequest(email, password))
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
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_login_to_home);
                        } else {
                            showError(extractMessage(body, "No se pudo iniciar sesión"));
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

    private void doSendOtp() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            showError("Ingresá tu email para recibir el código");
            return;
        }

        hideError();
        setLoading(true);

        ApiClient.getAuthService().sendOtp(new OtpSendRequest(email))
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
                        if (!isAdded()) {
                            return;
                        }
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
