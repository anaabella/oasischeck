package com.oasischeck.ui.configuracion;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class ConfiguracionCategoriasActivity extends AppCompatActivity {

    public static void iniciar(Context context) {
        context.startActivity(new Intent(context, ConfiguracionCategoriasActivity.class));
    }
}