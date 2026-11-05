package com.oasischeck.ui.util;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class AccesibilidadHelper {

    /**
     * Aplica contentDescription a vistas que carecen de él para lectores de pantalla.
     */
    public static void aplicarContentDescription(@NonNull View view, String descripcion) {
        if (view.getContentDescription() == null || view.getContentDescription().toString().isEmpty()) {
            view.setContentDescription(descripcion);
        }
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /**
     * Ajusta el tamaño de fuente para ser escalable (usa sp en lugar de dp programáticamente si es necesario).
     */
    public static void aplicarTamanioFuenteEscalable(@NonNull TextView textView, float tamanioSp) {
        textView.setTextSize(tamanioSp);
    }

    /**
     * Verifica contraste mínimo recomendado (4.5:1 para texto normal, 3:1 para texto grande).
     * Retorna true si el contraste cumple el mínimo.
     */
    public static boolean verificarContrasteMinimo(int colorTexto, int colorFondo, boolean esTextoGrande) {
        double ratio = calcularRatioContraste(colorTexto, colorFondo);
        return esTextoGrande ? ratio >= 3.0 : ratio >= 4.5;
    }

    private static double calcularRatioContraste(int color1, int color2) {
        double lum1 = obtenerLuminanciaRelativa(color1);
        double lum2 = obtenerLuminanciaRelativa(color2);
        double claro = Math.max(lum1, lum2);
        double oscuro = Math.min(lum1, lum2);
        return (claro + 0.05) / (oscuro + 0.05);
    }

    private static double obtenerLuminanciaRelativa(int color) {
        double r = ((color >> 16) & 0xFF) / 255.0;
        double g = ((color >> 8) & 0xFF) / 255.0;
        double b = (color & 0xFF) / 255.0;
        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
}