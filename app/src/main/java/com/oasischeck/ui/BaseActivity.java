package com.oasischeck.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        try {
            super.setContentView(layoutResID);
        } catch (Exception e) {
            // Si el layout no existe o hay error, mostrar mensaje y terminar
            Toast.makeText(this, "Error cargando pantalla: " + layoutResID, Toast.LENGTH_LONG).show();
            android.util.Log.e("OasisCheck", "Layout error: " + layoutResID, e);
            finish();
        }
    }

    @Override
    public void setContentView(android.view.View view) {
        try {
            super.setContentView(view);
        } catch (Exception e) {
            Toast.makeText(this, "Error cargando vista", Toast.LENGTH_LONG).show();
            android.util.Log.e("OasisCheck", "View error", e);
            finish();
        }
    }
}