package com.oasischeck.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Controla el modo oscuro manual. Si no hay preferencia guardada la app
 * sigue el tema del sistema.
 */
public final class ThemeHelper {

    private static final String PREFS = "oasischeck_prefs";
    private static final String KEY_DARK = "dark_mode";

    private ThemeHelper() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void aplicar(Context context) {
        SharedPreferences p = prefs(context);
        if (!p.contains(KEY_DARK)) {
            p.edit().putBoolean(KEY_DARK, true).apply();
        }
        boolean oscuro = p.getBoolean(KEY_DARK, true);
        AppCompatDelegate.setDefaultNightMode(
                oscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean esOscuro(Context context) {
        SharedPreferences p = prefs(context);
        if (p.contains(KEY_DARK)) return p.getBoolean(KEY_DARK, false);
        int modo = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return modo == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void alternar(Context context) {
        boolean nuevo = !esOscuro(context);
        prefs(context).edit().putBoolean(KEY_DARK, nuevo).apply();
        AppCompatDelegate.setDefaultNightMode(
                nuevo ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
