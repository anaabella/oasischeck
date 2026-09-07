package com.oasischeck.ui.export;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

public class ExportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnExportar = findViewById(R.id.btn_exportar);
        MaterialButton btnImportar = findViewById(R.id.btn_importar);

        btnExportar.setOnClickListener(v -> exportarJSON());
        btnImportar.setOnClickListener(v -> importarJSON());
    }

    private void exportarJSON() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                JSONObject root = new JSONObject();

                JSONArray ventasArray = new JSONArray();
                for (var v : db.ventaDao().getAll()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", v.id);
                    obj.put("fecha", v.fecha);
                    obj.put("cantidad", v.cantidad);
                    obj.put("precioUnitario", v.precioUnitario);
                    obj.put("total", v.total);
                    obj.put("lugar", v.lugar);
                    obj.put("categoria", v.categoria);
                    obj.put("infoVenta", v.infoVenta);
                    obj.put("sincronizado", v.sincronizado);
                    obj.put("nombrePlanta", v.nombrePlanta);
                    obj.put("duenia", v.duenia);
                    obj.put("estadoDinero", v.estadoDinero);
                    ventasArray.put(obj);
                }
                root.put("ventas", ventasArray);

                JSONArray gastosArray = new JSONArray();
                for (var g : db.gastoDao().getAll()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", g.id);
                    obj.put("fecha", g.fecha);
                    obj.put("concepto", g.concepto);
                    obj.put("monto", g.monto);
                    obj.put("tipo", g.tipo);
                    obj.put("recurrente", g.recurrente);
                    obj.put("notas", g.notas);
                    gastosArray.put(obj);
                }
                root.put("gastos", gastosArray);

                JSONArray pedidosArray = new JSONArray();
                for (var p : db.pedidoDao().getAll()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", p.id);
                    obj.put("fecha", p.fecha);
                    obj.put("cliente", p.cliente);
                    obj.put("productos", p.productos);
                    obj.put("total", p.total);
                    obj.put("estado", p.estado);
                    obj.put("notas", p.notas);
                    obj.put("creadoEn", p.creadoEn);
                    obj.put("preparadoEn", p.preparadoEn);
                    obj.put("entregadoEn", p.entregadoEn);
                    pedidosArray.put(obj);
                }
                root.put("pedidos", pedidosArray);

                JSONArray plannedArray = new JSONArray();
                for (var pv : db.plannedVentaDao().getAll()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", pv.id);
                    obj.put("fecha", pv.fecha);
                    obj.put("cliente", pv.cliente);
                    obj.put("infoVenta", pv.infoVenta);
                    obj.put("totalEstimado", pv.totalEstimado);
                    obj.put("estado", pv.estado);
                    obj.put("ventaRealId", pv.ventaRealId);
                    obj.put("notas", pv.notas);
                    obj.put("creadoEn", pv.creadoEn);
                    plannedArray.put(obj);
                }
                root.put("ventas_planificadas", plannedArray);

                JSONArray wishlistArray = new JSONArray();
                for (var w : db.wishPlantaDao().getAll()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", w.id);
                    obj.put("nombre", w.nombre);
                    obj.put("precioVisto", w.precioVisto);
                    obj.put("lugarVisto", w.lugarVisto);
                    obj.put("comprada", w.comprada);
                    obj.put("precioComprada", w.precioComprada);
                    obj.put("fechaComprada", w.fechaComprada);
                    obj.put("notas", w.notas);
                    obj.put("fotoUri", w.fotoUri);
                    wishlistArray.put(obj);
                }
                root.put("wishlist", wishlistArray);

                File exportDir = getExternalFilesDir(null);
                File file = new File(exportDir, "oasischeck_backup.json");
                FileWriter writer = new FileWriter(file);
                writer.write(root.toString(2));
                writer.close();

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
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(getContentResolver().openInputStream(uri)));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject root = new JSONObject(sb.toString());
                    AppDatabase db = AppDatabase.getInstance(this);

                    if (root.has("ventas")) {
                        db.ventaDao().deleteAll();
                        JSONArray arr = root.getJSONArray("ventas");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            com.oasischeck.data.model.Venta v = new com.oasischeck.data.model.Venta();
                            v.id = o.getLong("id");
                            v.fecha = o.getLong("fecha");
                            v.cantidad = o.getInt("cantidad");
                            v.precioUnitario = o.getDouble("precioUnitario");
                            v.total = o.getDouble("total");
                            v.lugar = o.getString("lugar");
                            v.categoria = o.getString("categoria");
                            v.infoVenta = o.getString("infoVenta");
                            v.sincronizado = o.getBoolean("sincronizado");
                            v.nombrePlanta = o.optString("nombrePlanta", "");
                            v.duenia = o.optString("duenia", "");
                            v.estadoDinero = o.optString("estadoDinero", "");
                            db.ventaDao().insert(v);
                        }
                    }

                    if (root.has("gastos")) {
                        db.gastoDao().deleteAll();
                        JSONArray arr = root.getJSONArray("gastos");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            com.oasischeck.data.model.Gasto g = new com.oasischeck.data.model.Gasto();
                            g.id = o.getLong("id");
                            g.fecha = o.getLong("fecha");
                            g.concepto = o.getString("concepto");
                            g.monto = o.getDouble("monto");
                            g.tipo = o.getString("tipo");
                            g.recurrente = o.getBoolean("recurrente");
                            g.notas = o.optString("notas", "");
                            db.gastoDao().insert(g);
                        }
                    }

                    if (root.has("pedidos")) {
                        db.pedidoDao().deleteAll();
                        JSONArray arr = root.getJSONArray("pedidos");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            com.oasischeck.data.model.Pedido p = new com.oasischeck.data.model.Pedido();
                            p.id = o.getLong("id");
                            p.fecha = o.getLong("fecha");
                            p.cliente = o.getString("cliente");
                            p.productos = o.getString("productos");
                            p.total = o.getDouble("total");
                            p.estado = o.getInt("estado");
                            p.notas = o.optString("notas", "");
                            p.creadoEn = o.optLong("creadoEn", 0);
                            p.preparadoEn = o.optLong("preparadoEn", 0);
                            p.entregadoEn = o.optLong("entregadoEn", 0);
                            db.pedidoDao().insert(p);
                        }
                    }

                    if (root.has("ventas_planificadas")) {
                        db.plannedVentaDao().deleteAll();
                        JSONArray arr = root.getJSONArray("ventas_planificadas");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            com.oasischeck.data.model.PlannedVenta pv = new com.oasischeck.data.model.PlannedVenta();
                            pv.id = o.getLong("id");
                            pv.fecha = o.getLong("fecha");
                            pv.cliente = o.getString("cliente");
                            pv.infoVenta = o.getString("infoVenta");
                            pv.totalEstimado = o.getDouble("totalEstimado");
                            pv.estado = o.getInt("estado");
                            pv.ventaRealId = o.optLong("ventaRealId", -1);
                            pv.notas = o.optString("notas", "");
                            pv.creadoEn = o.optLong("creadoEn", 0);
                            db.plannedVentaDao().insert(pv);
                        }
                    }

                    if (root.has("wishlist")) {
                        db.wishPlantaDao().deleteAll();
                        JSONArray arr = root.getJSONArray("wishlist");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            com.oasischeck.data.model.WishPlanta w = new com.oasischeck.data.model.WishPlanta();
                            w.id = o.getLong("id");
                            w.nombre = o.getString("nombre");
                            w.precioVisto = o.getDouble("precioVisto");
                            w.lugarVisto = o.optString("lugarVisto", "");
                            w.comprada = o.getBoolean("comprada");
                            w.precioComprada = o.optDouble("precioComprada", 0);
                            w.fechaComprada = o.optLong("fechaComprada", 0);
                            w.notas = o.optString("notas", "");
                            w.fotoUri = o.optString("fotoUri", "");
                            db.wishPlantaDao().insert(w);
                        }
                    }

                    runOnUiThread(() ->
                            Toast.makeText(this, "Importado correctamente", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al importar: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
        }
    }
}
