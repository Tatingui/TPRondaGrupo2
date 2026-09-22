package com.example.tprondagrupo2.util;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;

/**
 * Utilidades para manejo de imágenes compartidas entre adapters y fragments.
 */
public final class ImageUtils {

    private ImageUtils() { }

    /**
     * Muestra una imagen en pantalla completa dentro de un Dialog.
     *
     * @param anchorView vista de anclaje para obtener el Context
     * @param imageUrl   URL de la imagen a mostrar
     */
    public static void showFullscreenImage(View anchorView, String imageUrl) {
        Dialog dialog = new Dialog(anchorView.getContext(),
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_image);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivFullscreen = dialog.findViewById(R.id.ivFullscreen);
        dialog.findViewById(R.id.btnCloseFullscreen).setOnClickListener(v -> dialog.dismiss());
        ivFullscreen.setOnClickListener(v -> dialog.dismiss());

        Glide.with(anchorView.getContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivFullscreen);

        dialog.show();
    }
}
