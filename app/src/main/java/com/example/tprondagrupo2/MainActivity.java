package com.example.tprondagrupo2;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.tprondagrupo2.network.SessionManager;
import com.example.tprondagrupo2.network.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TokenManager.setContext(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Si ya hay un token guardado, saltar directo al Home (mantener sesion)
            String token = TokenManager.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                navController.navigate(R.id.action_login_to_home);
            }

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
    }
}
