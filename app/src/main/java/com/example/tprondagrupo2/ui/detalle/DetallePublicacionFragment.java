package com.example.tprondagrupo2.ui.detalle;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.data.repository.PublicationDetailSource.LoadError;
import com.example.tprondagrupo2.db.dao.PublicacionDao;
import com.example.tprondagrupo2.db.entity.PublicacionEntity;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.NetworkObserver;
import com.example.tprondagrupo2.util.FormatUtils;
import com.example.tprondagrupo2.util.PublicationConstants;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetallePublicacionFragment extends Fragment {

    public static final String ARG_PUBLICACION = "publicacion";

    private static final Locale LOCALE_AR = new Locale("es", "AR");
    private final ComoLlegarResolver comoLlegarResolver = new ComoLlegarResolver();
    private MapaNavigator mapaNavigator;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private AlertDialog activeDialog;
    private DetalleViewModel viewModel;
    private boolean detalleConfirmado;
    private List<String> fotosMostradas;
    private TextView tvCargaDetalle;
    private Button btnReintentarDetalle;

    @Inject
    PublicacionDao publicacionDao;

    private ViewPager2 vpGaleria;
    private TextView tvIndicadorFotos;
    private TextView tvTitulo;
    private TextView tvPrecio;
    private TextView tvCategoria;
    private TextView tvEstado;
    private TextView tvFechaPublicacion;
    private TextView tvDescripcion;
    private ImageButton btnFavorite;
    private Publicacion currentPublicacion;
    private NetworkObserver networkObserver;

    // Sección del vendedor
    private View seccionVendedor;
    private TextView tvVendedorAvatar;
    private TextView tvVendedorNombre;
    private TextView tvVendedorNivel;
    private RatingBar rbVendedorReputacion;
    private TextView tvVendedorReputacion;
    private TextView tvVendedorVentas;
    private TextView tvVendedorMiembroDesde;
    private Button btnVerPerfilVendedor;

    // Dirección y acciones según quién mira
    private TextView tvDireccion;
    private Button btnComoLlegar;
    private TextView tvAvisoEstado;
    private LinearLayout layoutAccionesComprador;
    private TextView tvMiOferta;
    private Button btnPreguntar;
    private Button btnOfertar;
    private Button btnVerOfertas;
    private LinearLayout layoutGestionVendedor;
    private TextView tvEstadoPublicacion;
    private Button btnPausarReactivar;
    private Button btnMarcarVendida;

    // Preguntas
    private TextView tvSinPreguntas;
    private LinearLayout layoutPreguntas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapaNavigator = new MapaNavigator(new AndroidMapaLauncher(this::startActivity));
        viewModel = new ViewModelProvider(this)
                .get(DetalleViewModel.class);
        tvCargaDetalle = view.findViewById(R.id.tvCargaDetalle);
        btnReintentarDetalle = view.findViewById(R.id.btnReintentarDetalle);
        btnReintentarDetalle.setOnClickListener(v -> {
            DetalleUiState estado = viewModel.getEstado().getValue();
            if (estado != null && estado.getError() == null && estado.getErrorPreguntas() != null) {
                viewModel.recargarPreguntas();
            } else {
                viewModel.recargarDetalle();
            }
        });

        vpGaleria = view.findViewById(R.id.vpGaleria);
        tvIndicadorFotos = view.findViewById(R.id.tvIndicadorFotos);
        tvTitulo = view.findViewById(R.id.tvTitulo);
        tvPrecio = view.findViewById(R.id.tvPrecio);
        tvCategoria = view.findViewById(R.id.tvCategoria);
        tvEstado = view.findViewById(R.id.tvEstado);
        tvFechaPublicacion = view.findViewById(R.id.tvFechaPublicacion);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        btnFavorite = view.findViewById(R.id.btnFavoriteDetail);

        seccionVendedor = view.findViewById(R.id.seccionVendedor);
        tvVendedorAvatar = view.findViewById(R.id.tvVendedorAvatar);
        tvVendedorNombre = view.findViewById(R.id.tvVendedorNombre);
        tvVendedorNivel = view.findViewById(R.id.tvVendedorNivel);
        rbVendedorReputacion = view.findViewById(R.id.rbVendedorReputacion);
        tvVendedorReputacion = view.findViewById(R.id.tvVendedorReputacion);
        tvVendedorVentas = view.findViewById(R.id.tvVendedorVentas);
        tvVendedorMiembroDesde = view.findViewById(R.id.tvVendedorMiembroDesde);
        btnVerPerfilVendedor = view.findViewById(R.id.btnVerPerfilVendedor);

        tvDireccion = view.findViewById(R.id.tvDireccion);
        btnComoLlegar = view.findViewById(R.id.btnComoLlegar);
        tvAvisoEstado = view.findViewById(R.id.tvAvisoEstado);
        layoutAccionesComprador = view.findViewById(R.id.layoutAccionesComprador);
        tvMiOferta = view.findViewById(R.id.tvMiOferta);
        btnPreguntar = view.findViewById(R.id.btnPreguntar);
        btnOfertar = view.findViewById(R.id.btnOfertar);
        layoutGestionVendedor = view.findViewById(R.id.layoutGestionVendedor);
        btnVerOfertas = view.findViewById(R.id.btnVerOfertas);
        tvEstadoPublicacion = view.findViewById(R.id.tvEstadoPublicacion);
        btnPausarReactivar = view.findViewById(R.id.btnPausarReactivar);
        btnMarcarVendida = view.findViewById(R.id.btnMarcarVendida);
        tvSinPreguntas = view.findViewById(R.id.tvSinPreguntas);
        layoutPreguntas = view.findViewById(R.id.layoutPreguntas);

        currentPublicacion = obtenerPublicacion();
        if (currentPublicacion == null) {
            Toast.makeText(getContext(), "No se encontró la publicación", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        btnFavorite.setOnClickListener(v -> {
            if (!hayConexion()) return;
            viewModel.toggleFavorite();
        });
        btnPreguntar.setOnClickListener(v -> mostrarDialogoPreguntar());
        btnOfertar.setOnClickListener(v -> mostrarDialogoOfertar());
        btnPausarReactivar.setOnClickListener(v -> {
            if (!hayConexion()) return;
            String nuevoEstado = "ACTIVE".equals(currentPublicacion.getState()) ? "PAUSED" : "ACTIVE";
            viewModel.cambiarEstadoPublicacion(nuevoEstado);
        });
        btnMarcarVendida.setOnClickListener(v -> confirmarMarcarVendida());
        btnVerOfertas.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_detalle_to_myOffers);
        });

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                viewModel.seleccionarFoto(position);
                actualizarIndicador(position, currentPublicacion.getCantidadFotos());
            }
        };
        vpGaleria.registerOnPageChangeCallback(pageChangeCallback);

        networkObserver = new NetworkObserver(requireContext());
        networkObserver.getIsConnected().observe(getViewLifecycleOwner(), connected -> {
            float alpha = Boolean.TRUE.equals(connected) ? 1.0f : 0.5f;
            btnFavorite.setAlpha(alpha);
            btnPreguntar.setAlpha(alpha);
            btnOfertar.setAlpha(alpha);
            btnPausarReactivar.setAlpha(alpha);
            btnMarcarVendida.setAlpha(alpha);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && isAdded()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        Publicacion inicial = currentPublicacion;
        viewModel.getEstado().observe(getViewLifecycleOwner(), this::mostrarEstadoDetalle);
        viewModel.inicializar(inicial);
    }

    private void mostrarEstadoDetalle(DetalleUiState estado) {
        if (estado.getPublicacion() == null) return;
        currentPublicacion = estado.getPublicacion();
        detalleConfirmado = estado.isDetalleConfirmado();
        mostrarPublicacion(currentPublicacion);
        if (detalleConfirmado) {
            mostrarAcciones(currentPublicacion, estado);
        } else {
            layoutAccionesComprador.setVisibility(View.GONE);
            layoutGestionVendedor.setVisibility(View.GONE);
            tvAvisoEstado.setVisibility(View.GONE);
            btnFavorite.setVisibility(View.GONE);
        }
        btnFavorite.setEnabled(!estado.isFavoritoEnCurso());
        if (estado.isOfertaEnCurso()) btnOfertar.setEnabled(false);
        btnPausarReactivar.setEnabled(!estado.isGestionEnCurso());
        btnMarcarVendida.setEnabled(!estado.isGestionEnCurso());
        mostrarPreguntas(estado.getPreguntas());
        if (estado.isCargandoPreguntas() || estado.getErrorPreguntas() != null) {
            tvSinPreguntas.setVisibility(View.VISIBLE);
            tvSinPreguntas.setText(estado.isCargandoPreguntas()
                    ? R.string.detalle_cargando_preguntas : R.string.detalle_error_preguntas);
        } else {
            tvSinPreguntas.setText(R.string.detalle_sin_preguntas);
        }
        tvCargaDetalle.setVisibility(estado.isCargando() || estado.getError() != null ? View.VISIBLE : View.GONE);
        if (estado.isCargando()) tvCargaDetalle.setText(R.string.detalle_cargando);
        else if (estado.getError() != null) tvCargaDetalle.setText(textoErrorCarga(estado.getError()));
        boolean reintentar = !estado.isCargando() && !estado.isCargandoPreguntas()
                && (estado.getError() != null || estado.getErrorPreguntas() != null);
        btnReintentarDetalle.setVisibility(reintentar ? View.VISIBLE : View.GONE);

        Publicacion paraCache = viewModel.consumirCachePendiente();
        if (paraCache != null) saveToCache(paraCache);
        if (viewModel.consumirVisitaLocal()) registrarVistaLocal(currentPublicacion);
    }

    private int textoErrorCarga(LoadError error) {
        switch (error) {
            case NETWORK: return R.string.detalle_error_red;
            case NOT_FOUND: return R.string.detalle_no_existe;
            case UNAUTHORIZED: return R.string.detalle_sesion_vencida;
            case FORBIDDEN: return R.string.detalle_sin_permiso;
            default: return R.string.detalle_error_carga;
        }
    }

    private void saveToCache(Publicacion p) {
        PublicacionEntity entity = PublicacionEntity.fromModel(p);
        publicacionDao.insert(entity);
    }

    private Publicacion obtenerPublicacion() {
        if (getArguments() != null) {
            Object extra = getArguments().getSerializable(ARG_PUBLICACION);
            if (extra instanceof Publicacion) {
                return (Publicacion) extra;
            }
        }
        return null;
    }

    private void mostrarPublicacion(@NonNull Publicacion publicacion) {
        if (fotosMostradas == null || !fotosMostradas.equals(publicacion.getImageUrls())) {
            configurarGaleria(publicacion);
            fotosMostradas = new java.util.ArrayList<>(publicacion.getImageUrls());
        }

        tvTitulo.setText(publicacion.getTitle());
        tvPrecio.setText(formatearPrecio(publicacion.getPrice()));
        tvCategoria.setText(publicacion.getCategoryName());
        tvEstado.setText(PublicationConstants.translateStatus(publicacion.getStatus()));
        tvFechaPublicacion.setText(
                getString(R.string.detalle_publicado_el, formatearFecha(publicacion.getCreatedAt())));
        tvDescripcion.setText(publicacion.getDescription());

        actualizarIconoFavorito(publicacion.isFavorite());
        mostrarVendedor(publicacion.getVendedor());
        mostrarDireccion(publicacion);
    }

    private void mostrarDireccion(@NonNull Publicacion publicacion) {
        String zona = getString(R.string.detalle_zona,
                publicacion.getLocation() != null ? publicacion.getLocation() : "-");

        if (detalleConfirmado && publicacion.isAddressVisible()) {
            String direccion = publicacion.getAddress();
            if (direccion != null && !direccion.trim().isEmpty()) {
                tvDireccion.setText(zona + "\n"
                        + getString(R.string.detalle_direccion_exacta, direccion.trim()));
            } else {
                tvDireccion.setText(zona + "\n" + getString(R.string.detalle_sin_direccion_guardada));
            }
        } else if (publicacion.isOwner()) {
            tvDireccion.setText(zona);
        } else {
            tvDireccion.setText(zona + "\n" + getString(R.string.detalle_direccion_oculta));
        }

        mostrarComoLlegar(publicacion);
    }

    private void mostrarComoLlegar(@NonNull Publicacion publicacion) {
        String destino = detalleConfirmado ? comoLlegarResolver.resolver(publicacion) : null;

        if (destino == null) {
            btnComoLlegar.setVisibility(View.GONE);
            btnComoLlegar.setOnClickListener(null);
            return;
        }

        btnComoLlegar.setVisibility(View.VISIBLE);
        btnComoLlegar.setOnClickListener(v -> abrirEnMapa(destino));
    }

    private void abrirEnMapa(@NonNull String destino) {
        if (!mapaNavigator.abrir(destino)) {
            Toast.makeText(getContext(), R.string.detalle_sin_app_mapas, Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarAcciones(@NonNull Publicacion publicacion, DetalleUiState estado) {
        if (!detalleConfirmado) return;
        if (publicacion.isOwner()) {
            layoutAccionesComprador.setVisibility(View.GONE);
            tvAvisoEstado.setVisibility(View.GONE);
            btnFavorite.setVisibility(View.GONE);
            layoutGestionVendedor.setVisibility(View.VISIBLE);

            tvEstadoPublicacion.setText("Estado: " + PublicationConstants.translateStatus(publicacion.getState()));
            boolean vendida = "SOLD".equals(publicacion.getState());
            btnPausarReactivar.setText("ACTIVE".equals(publicacion.getState())
                    ? R.string.detalle_pausar : R.string.detalle_reactivar);
            btnPausarReactivar.setVisibility(vendida ? View.GONE : View.VISIBLE);
            btnMarcarVendida.setVisibility(vendida ? View.GONE : View.VISIBLE);
            return;
        }

        layoutGestionVendedor.setVisibility(View.GONE);
        btnFavorite.setVisibility(View.VISIBLE);

        if (!publicacion.estaActiva()) {
            layoutAccionesComprador.setVisibility(View.GONE);
            tvAvisoEstado.setVisibility(View.VISIBLE);
            tvAvisoEstado.setText("Esta publicación está "
                    + PublicationConstants.translateStatus(publicacion.getState()).toLowerCase(LOCALE_AR));
            return;
        }

        tvAvisoEstado.setVisibility(View.GONE);
        layoutAccionesComprador.setVisibility(View.VISIBLE);

        Offer miOferta = publicacion.getMyOffer();
        if (miOferta != null) {
            tvMiOferta.setVisibility(View.VISIBLE);
            tvMiOferta.setText(getString(R.string.detalle_mi_oferta,
                    formatearPrecio(miOferta.getOfferedPrice()),
                    PublicationConstants.translateOfferStatus(miOferta.getStatus())));
            btnOfertar.setEnabled(!"PENDING".equals(miOferta.getStatus()) && !estado.isOfertaEnCurso());
        } else {
            tvMiOferta.setVisibility(View.GONE);
            btnOfertar.setEnabled(!estado.isOfertaEnCurso());
        }
    }

    // ---------- Preguntas ----------

    private void mostrarPreguntas(List<Pregunta> preguntas) {
        layoutPreguntas.removeAllViews();
        tvSinPreguntas.setVisibility(preguntas.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Pregunta pregunta : preguntas) {
            View item = inflater.inflate(R.layout.item_pregunta, layoutPreguntas, false);
            TextView tvPregunta = item.findViewById(R.id.tvPregunta);
            TextView tvRespuesta = item.findViewById(R.id.tvRespuesta);
            Button btnResponder = item.findViewById(R.id.btnResponder);

            tvPregunta.setText(pregunta.getText());
            if (pregunta.estaRespondida()) {
                tvRespuesta.setText("↳ " + pregunta.getAnswer());
            } else {
                tvRespuesta.setText(R.string.detalle_sin_respuesta);
                if (detalleConfirmado && currentPublicacion.isOwner()) {
                    btnResponder.setVisibility(View.VISIBLE);
                    btnResponder.setOnClickListener(v -> mostrarDialogoResponder(pregunta));
                }
            }
            layoutPreguntas.addView(item);
        }
    }

    private void mostrarDialogoPreguntar() {
        if (!hayConexion()) return;

        EditText etPregunta = crearCampoTexto("Escribí tu pregunta");
        activeDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Preguntar al vendedor")
                .setView(etPregunta)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String texto = etPregunta.getText().toString().trim();
                    if (texto.isEmpty()) {
                        Toast.makeText(getContext(), "La pregunta no puede estar vacía", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.enviarPregunta(texto);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoResponder(Pregunta pregunta) {
        if (!hayConexion()) return;

        EditText etRespuesta = crearCampoTexto("Escribí tu respuesta");
        activeDialog = new AlertDialog.Builder(requireContext())
                .setTitle(pregunta.getText())
                .setView(etRespuesta)
                .setPositiveButton("Responder", (dialog, which) -> {
                    String texto = etRespuesta.getText().toString().trim();
                    if (texto.isEmpty()) {
                        Toast.makeText(getContext(), "La respuesta no puede estar vacía", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.responderPregunta(pregunta.getId(), texto);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- Ofertas ----------

    private void mostrarDialogoOfertar() {
        if (!hayConexion()) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ofertar, null);
        TextView tvPrecioPublicado = dialogView.findViewById(R.id.tvPrecioPublicado);
        EditText etMonto = dialogView.findViewById(R.id.etMontoOferta);
        EditText etMensaje = dialogView.findViewById(R.id.etMensajeOferta);
        TextView tvPorcentaje = dialogView.findViewById(R.id.tvPorcentajeOferta);
        double precioOriginal = currentPublicacion.getPrice();
        tvPrecioPublicado.setText("Precio publicado: " + formatearPrecio(precioOriginal));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Hacer una oferta")
                .setView(dialogView)
                .setPositiveButton("Ofertar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        activeDialog = dialog;
        dialog.setOnShowListener(d -> {
            android.widget.Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            etMonto.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        double monto = Double.parseDouble(s.toString().trim());
                        if (monto > 0 && precioOriginal > 0) {
                            int porcentaje = (int) Math.round((monto / precioOriginal) * 100);
                            tvPorcentaje.setVisibility(View.VISIBLE);
                            if (monto > precioOriginal) {
                                tvPorcentaje.setText("No podés ofertar a un precio mayor al publicado");
                                tvPorcentaje.setTextColor(0xFFD32F2F);
                                btnPositive.setEnabled(false);
                            } else {
                                tvPorcentaje.setText(porcentaje + "% del precio publicado");
                                tvPorcentaje.setTextColor(0xFF888888);
                                btnPositive.setEnabled(true);
                            }
                        } else {
                            tvPorcentaje.setVisibility(View.GONE);
                            btnPositive.setEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        tvPorcentaje.setVisibility(View.GONE);
                        btnPositive.setEnabled(false);
                    }
                }
            });

            btnPositive.setEnabled(false);
            btnPositive.setOnClickListener(v -> {
                double monto;
                try {
                    monto = Double.parseDouble(etMonto.getText().toString().trim());
                } catch (NumberFormatException e) {
                    monto = 0;
                }
                if (monto <= 0 || monto > precioOriginal) {
                    return;
                }
                String mensaje = etMensaje.getText().toString().trim();
                viewModel.enviarOferta(monto, mensaje.isEmpty() ? null : mensaje);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // ---------- Gestión del vendedor ----------

    private void confirmarMarcarVendida() {
        if (!hayConexion()) return;

        activeDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Marcar como vendida")
                .setMessage("La publicación deja de mostrarse en el listado. ¿Continuar?")
                .setPositiveButton("Sí", (dialog, which) -> viewModel.cambiarEstadoPublicacion("SOLD"))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- Utilidades ----------

    private boolean hayConexion() {
        if (networkObserver.isCurrentlyConnected()) {
            return true;
        }
        Toast.makeText(getContext(), R.string.detalle_sin_conexion_accion, Toast.LENGTH_SHORT).show();
        return false;
    }

    private EditText crearCampoTexto(String hint) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        editText.setPadding(padding, padding, padding, padding);
        return editText;
    }

    /** El backend manda la fecha como "2026-09-18T10:30:00"; la mostramos como "18/09/2026". */
    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.length() < 10 || fecha.charAt(4) != '-') {
            return fecha != null ? fecha : "";
        }
        String anio = fecha.substring(0, 4);
        String mes = fecha.substring(5, 7);
        String dia = fecha.substring(8, 10);
        return dia + "/" + mes + "/" + anio;
    }

    private void registrarVistaLocal(@NonNull Publicacion publicacion) {
        if (publicacion.getId() == null) return;
        viewModel.registrarVistaLocal(publicacion.getId());
        publicacion.setHasUpdates(false);
        publicacion.setLastSeenPrice(publicacion.getPrice());
    }

    private void actualizarIconoFavorito(boolean isFavorite) {
        btnFavorite.setImageResource(isFavorite
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);
    }

    private void mostrarVendedor(@Nullable Vendedor vendedor) {
        if (vendedor == null) {
            seccionVendedor.setVisibility(View.GONE);
            return;
        }
        seccionVendedor.setVisibility(View.VISIBLE);

        tvVendedorNombre.setText(vendedor.getNombre());
        VendedorViewBinder.bindReputacion(vendedor, tvVendedorAvatar, rbVendedorReputacion,
                tvVendedorReputacion, tvVendedorNivel);

        tvVendedorVentas.setText(getString(R.string.vendedor_operaciones,
                vendedor.getCantidadVentas(), vendedor.getCantidadCompras()));

        if (vendedor.getMiembroDesde() != null) {
            tvVendedorMiembroDesde.setVisibility(View.VISIBLE);
            tvVendedorMiembroDesde.setText(
                    getString(R.string.vendedor_miembro_desde, vendedor.getMiembroDesde()));
        } else {
            tvVendedorMiembroDesde.setVisibility(View.GONE);
        }

        btnVerPerfilVendedor.setOnClickListener(v -> abrirPerfil(vendedor));
    }

    private void abrirPerfil(@NonNull Vendedor vendedor) {
        Bundle args = new Bundle();
        args.putSerializable(PerfilVendedorFragment.ARG_VENDEDOR, vendedor);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_detalle_to_perfil_vendedor, args);
    }

    private void configurarGaleria(@NonNull Publicacion publicacion) {
        List<String> fotos = publicacion.getImageUrls();

        GaleriaFotosAdapter adapter = new GaleriaFotosAdapter(fotos, position -> {
            FotoFullscreenDialog dialog = FotoFullscreenDialog.newInstance(fotos, position);
            dialog.show(getParentFragmentManager(), "foto_fullscreen");
        });
        int posicion = Math.min(viewModel.getFotoSeleccionada(), Math.max(0, fotos.size() - 1));
        vpGaleria.setAdapter(adapter);
        vpGaleria.setCurrentItem(posicion, false);
        actualizarIndicador(posicion, publicacion.getCantidadFotos());
    }

    private void actualizarIndicador(int position, int total) {
        if (total <= 0) {
            tvIndicadorFotos.setVisibility(View.GONE);
            return;
        }
        tvIndicadorFotos.setVisibility(View.VISIBLE);
        tvIndicadorFotos.setText(getString(R.string.detalle_indicador_fotos, position + 1, total));
    }

    private String formatearPrecio(double precio) {
        return FormatUtils.formatPrice(precio);
    }

    @Override
    public void onDestroyView() {
        if (viewModel != null && viewModel.hasPendingWrites()) {
            viewModel.revalidarAlVolver();
        }
        if (activeDialog != null) activeDialog.dismiss();
        if (vpGaleria != null) {
            if (pageChangeCallback != null) vpGaleria.unregisterOnPageChangeCallback(pageChangeCallback);
            vpGaleria.setAdapter(null);
        }
        activeDialog = null;
        pageChangeCallback = null;
        mapaNavigator = null;
        networkObserver = null;
        currentPublicacion = null;
        fotosMostradas = null;
        tvCargaDetalle = null;
        btnReintentarDetalle = null;
        vpGaleria = null;
        tvIndicadorFotos = null;
        tvTitulo = null;
        tvPrecio = null;
        tvCategoria = null;
        tvEstado = null;
        tvFechaPublicacion = null;
        tvDescripcion = null;
        btnFavorite = null;
        seccionVendedor = null;
        tvVendedorAvatar = null;
        tvVendedorNombre = null;
        tvVendedorNivel = null;
        rbVendedorReputacion = null;
        tvVendedorReputacion = null;
        tvVendedorVentas = null;
        tvVendedorMiembroDesde = null;
        btnVerPerfilVendedor = null;
        tvDireccion = null;
        btnComoLlegar = null;
        tvAvisoEstado = null;
        layoutAccionesComprador = null;
        tvMiOferta = null;
        btnPreguntar = null;
        btnOfertar = null;
        btnVerOfertas = null;
        layoutGestionVendedor = null;
        tvEstadoPublicacion = null;
        btnPausarReactivar = null;
        btnMarcarVendida = null;
        tvSinPreguntas = null;
        layoutPreguntas = null;
        super.onDestroyView();
    }
}
