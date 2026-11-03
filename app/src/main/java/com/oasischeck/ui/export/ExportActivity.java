package com.oasischeck.ui.export;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.oasischeck.R;
import com.oasischeck.util.BackupManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ExportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnExportar = findViewById(R.id.btn_exportar);
        MaterialButton btnExportarNube = findViewById(R.id.btn_exportar_nube);
        MaterialButton btnImportar = findViewById(R.id.btn_importar);

        btnExportar.setOnClickListener(v -> exportarJSON());
        btnExportarNube.setOnClickListener(v -> exportarNube());
        btnImportar.setOnClickListener(v -> importarJSON());
    }

    private void exportarNube() {
        String fecha = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "oasischeck_backup_" + fecha + ".json");
        startActivityForResult(intent, 1002);
    }

    private void subirArchivo(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new IllegalStateException("No se pudo abrir el archivo");
                BackupManager.writeBackup(this, out);
                runOnUiThread(() ->
                        Toast.makeText(this, "Copia guardada en la nube", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al guardar en la nube: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        });
    }

    private void exportarJSON() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File exportDir = getExternalFilesDir(null);
                File file = new File(exportDir, "oasischeck_backup.json");
                BackupManager.writeBackup(this, file);

                Uri uri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", file);

                runOnUiThread(() -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/json");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Backup OasisCheck");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Compartir backup"));
                    Toast.makeText(this, "Exportado correctamente", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void importarJSON() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            new AlertDialog.Builder(this)
                    .setTitle("Importar backup")
                    .setMessage("Esto reemplazará TODOS los datos actuales por los del archivo. " +
                            "Antes de importar se guardará un backup de seguridad. ¿Continuar?")
                    .setPositiveButton("Importar", (d, w) -> procesarImportacion(uri))
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else if (requestCode == 1002 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            subirArchivo(data.getData());
        }
    }

    private void procesarImportacion(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(getContentResolver().openInputStream(uri)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }

                JSONObject root = new JSONObject(sb.toString());

                String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File respaldo = new File(getFilesDir(), "backup_pre_import_" + fecha + ".json");
                BackupManager.writeBackup(this, respaldo);

                BackupManager.importBackup(this, root);

                runOnUiThread(() ->
                        Toast.makeText(this, "Importado correctamente. Backup previo guardado.", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al importar: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
