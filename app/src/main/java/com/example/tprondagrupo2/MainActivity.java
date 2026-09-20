package com.example.tprondagrupo2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.tprondagrupo2.network.SessionManager;
import com.example.tprondagrupo2.network.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;

// Esta anotación habilita la inyección en esta Activity. Sin esto, @Inject falla.
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> localNetworkPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    // Recarga las pantallas que pudieron pedir datos antes de obtener el permiso.
                    recreate();
                } else {
                    Toast.makeText(this, R.string.local_network_permission_required,
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TokenManager.setContext(this);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View root = findViewById(R.id.main_container);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );

            view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(root);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Solo ir directo al Home si "Mantener sesión" está activo Y hay token
            TokenManager tm = TokenManager.getInstance();
            String token = tm.getToken();
            if (token != null && !token.isEmpty() && tm.isKeepSession()) {
                navController.navigate(R.id.action_login_to_home);
            }
            // Si hay token pero NO keepSession → queda en Login (biometría o credenciales)
            // Si no hay token → queda en Login normalmente

            // Si el token venció (el backend devolvió 401), volver al login
            SessionManager.getInstance().onSessionExpired().observe(this, expired -> {
                if (Boolean.TRUE.equals(expired)) {
                    SessionManager.getInstance().clearExpiredFlag();
                    Toast.makeText(this, "Tu sesión expiró. Ingresá de nuevo.", Toast.LENGTH_LONG).show();
                    navController.navigate(R.id.loginFragment, null,
                            new androidx.navigation.NavOptions.Builder()
                                    .setPopUpTo(R.id.nav_graph, true)
                                    .build());
                }
            });

            // Ocultar BottomNav en pantallas de Auth y Detalle
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.loginFragment || id == R.id.registerFragment
                        || id == R.id.otpFragment || id == R.id.detallePublicacionFragment) {
                    bottomNav.setVisibility(View.GONE);
                } else {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            });
        }

        // Android 17 bloquea el backend local (incluido 10.0.2.2) sin este permiso.
        if (Build.VERSION.SDK_INT >= 37 && savedInstanceState == null
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCAL_NETWORK)
                != PackageManager.PERMISSION_GRANTED) {
            localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK);
        }
    }
}
