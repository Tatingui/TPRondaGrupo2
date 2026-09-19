package com.example.tprondagrupo2.ui.auth;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tprondagrupo2.network.TokenManager;

import java.util.concurrent.Executor;

/**
 * Encapsula toda la logica de autenticacion biometrica:
 * - Verificacion de disponibilidad del hardware
 * - Construccion y lanzamiento del BiometricPrompt
 * - Dialogo para ofrecer activacion post-login
 *
 * Patron utilizado: Helper/Delegate. Extrae responsabilidad del Fragment
 * para que este se ocupe solo de la UI.
 */
public class BiometricHelper {

    private static final String TAG = "BiometricHelper";

    /**
     * Combinacion de authenticators permitidos.
     * BIOMETRIC_STRONG: huella, rostro (Class 3).
     * DEVICE_CREDENTIAL: PIN, patron, contrasena del dispositivo.
     * Al combinarlos, el sistema usa lo que este disponible.
     */
    private static final int ALLOWED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

    /**
     * Callback que el Fragment implementa para reaccionar
     * a los resultados de la autenticacion biometrica.
     */
    public interface BiometricCallback {
        /** Biometria exitosa, el token ya fue restaurado en TokenManager. */
        void onBiometricSuccess();

        /** El token encriptado no existia o estaba corrupto. */
        void onBiometricTokenExpired();

        /** El usuario cancelo el prompt o hubo un error del sistema. */
        void onBiometricCancelled();
    }

    private final TokenManager tokenManager;

    public BiometricHelper(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    /**
     * Verifica si el dispositivo tiene biometria (o credencial) disponible.
     * Usa BiometricManager.canAuthenticate() que chequea hardware + enrolamiento.
     */
    public boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Determina si corresponde lanzar el prompt biometrico al abrir el login.
     * Condiciones: biometria habilitada por el usuario + hay token encriptado guardado
     * + no tiene "mantener sesion" activo (porque en ese caso va directo al home).
     */
    public boolean shouldPromptBiometric() {
        return tokenManager.isBiometricEnabled()
                && tokenManager.getEncryptedToken() != null
                && !tokenManager.isKeepSession();
    }

    /**
     * Lanza el BiometricPrompt del sistema como modal sobre el Fragment.
     *
     * BiometricPrompt requiere:
     * - Un LifecycleOwner (el Fragment)
     * - Un Executor para los callbacks (main thread)
     * - Los callbacks de autenticacion
     *
     * El PromptInfo configura el texto visible y los authenticators permitidos.
     */
    public void showPrompt(Fragment fragment, BiometricCallback callback) {
        Context context = fragment.requireContext();

        if (!isBiometricAvailable(context)) {
            Log.w(TAG, "Biometria no disponible en el dispositivo");
            callback.onBiometricCancelled();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(context);

        BiometricPrompt biometricPrompt = new BiometricPrompt(fragment, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (!fragment.isAdded()) return;

                        String encryptedToken = tokenManager.getEncryptedToken();
                        if (encryptedToken != null) {
                            tokenManager.saveToken(encryptedToken);
                            callback.onBiometricSuccess();
                        } else {
                            tokenManager.setBiometricEnabled(false);
                            callback.onBiometricTokenExpired();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // El prompt sigue abierto, el sistema muestra feedback
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                     @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (!fragment.isAdded()) return;
                        Log.w(TAG, "Error biometrico (" + errorCode + "): " + errString);
                        callback.onBiometricCancelled();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Iniciar sesion en Ronda")
                .setSubtitle("Usa tu huella digital o credencial del dispositivo")
                .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Muestra un AlertDialog ofreciendo activar biometria despues de un login exitoso.
     * Si acepta, guarda el token de forma encriptada con EncryptedSharedPreferences
     * y activa el flag biometrico para futuros inicios.
     */
    public void offerBiometricEnrollment(Context context, String token, Runnable onComplete) {
        if (!isBiometricAvailable(context)) {
            onComplete.run();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Acceso biometrico")
                .setMessage("Queres usar tu huella digital para iniciar sesion la proxima vez?")
                .setPositiveButton("Si", (dialog, which) -> {
                    tokenManager.setBiometricEnabled(true);
                    tokenManager.saveEncryptedToken(token);
                    onComplete.run();
                })
                .setNegativeButton("No, gracias", (dialog, which) -> onComplete.run())
                .setCancelable(false)
                .show();
    }
}
