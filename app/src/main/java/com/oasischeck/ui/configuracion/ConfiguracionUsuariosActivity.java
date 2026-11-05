package com.oasischeck.ui.configuracion;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class ConfiguracionUsuariosActivity extends AppCompatActivity {

    public static void iniciar(Context context) {
        context.startActivity(new Intent(context, ConfiguracionUsuariosActivity.class));
    }
}