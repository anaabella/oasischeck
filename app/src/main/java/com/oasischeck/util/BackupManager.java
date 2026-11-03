package com.oasischeck.util;

import android.content.Context;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Gasto;
import com.oasischeck.data.model.Intercambio;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.model.WishPlanta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Genera y restaura copias de seguridad en JSON. Todos los métodos deben
 * ejecutarse fuera del hilo principal.
 */
public final class BackupManager {

    private BackupManager() {}

    public static JSONObject buildBackup(Context context) throws Exception {
        AppDatabase db = AppDatabase.getInstance(context);
        JSONObject root = new JSONObject();

        JSONArray ventasArray = new JSONArray();
        for (Venta v : db.ventaDao().getAll()) {
            JSONObject o = new JSONObject();
            o.put("id", v.id);
            o.put("fecha", v.fecha);
            o.put("cantidad", v.cantidad);
            o.put("precioUnitario", v.precioUnitario);
            o.put("total", v.total);
            o.put("lugar", v.lugar);
            o.put("categoria", v.categoria);
            o.put("infoVenta", v.infoVenta);
            o.put("sincronizado", v.sincronizado);
            o.put("nombrePlanta", v.nombrePlanta);
            o.put("duenia", v.duenia);
            o.put("estadoDinero", v.estadoDinero);
            ventasArray.put(o);
        }
        root.put("ventas", ventasArray);

        JSONArray gastosArray = new JSONArray();
        for (Gasto g : db.gastoDao().getAll()) {
            JSONObject o = new JSONObject();
            o.put("id", g.id);
            o.put("fecha", g.fecha);
            o.put("concepto", g.concepto);
            o.put("monto", g.monto);
            o.put("tipo", g.tipo);
            o.put("recurrente", g.recurrente);
            o.put("notas", g.notas);
            gastosArray.put(o);
        }
        root.put("gastos", gastosArray);

        JSONArray pedidosArray = new JSONArray();
        for (Pedido p : db.pedidoDao().getAll()) {
            JSONObject o = new JSONObject();
            o.put("id", p.id);
            o.put("fecha", p.fecha);
            o.put("cliente", p.cliente);
            o.put("productos", p.productos);
            o.put("total", p.total);
            o.put("estado", p.estado);
            o.put("notas", p.notas);
            o.put("creadoEn", p.creadoEn);
            o.put("preparadoEn", p.preparadoEn);
            o.put("entregadoEn", p.entregadoEn);
            pedidosArray.put(o);
        }
        root.put("pedidos", pedidosArray);

        JSONArray plannedArray = new JSONArray();
        for (PlannedVenta pv : db.plannedVentaDao().getAll()) {
            JSONObject o = new JSONObject();
            o.put("id", pv.id);
            o.put("fecha", pv.fecha);
            o.put("cliente", pv.cliente);
            o.put("infoVenta", pv.infoVenta);
            o.put("totalEstimado", pv.totalEstimado);
            o.put("estado", pv.estado);
            o.put("ventaRealId", pv.ventaRealId);
            o.put("notas", pv.notas);
            o.put("creadoEn", pv.creadoEn);
            plannedArray.put(o);
        }
        root.put("ventas_planificadas", plannedArray);

        JSONArray wishlistArray = new JSONArray();
        for (WishPlanta w : db.wishPlantaDao().getAll()) {
            JSONObject o = new JSONObject();
            o.put("id", w.id);
            o.put("nombre", w.nombre);
            o.put("precioVisto", w.precioVisto);
            o.put("lugarVisto", w.lugarVisto);
            o.put("comprada", w.comprada);
            o.put("precioComprada", w.precioComprada);
            o.put("fechaComprada", w.fechaComprada);
            o.put("notas", w.notas);
            o.put("fotoUri", w.fotoUri);
            wishlistArray.put(o);
        }
        root.put("wishlist", wishlistArray);

        JSONArray intercambiosArray = new JSONArray();
        for (Intercambio i : db.intercambioDao().getAll()) {
            JSONObject o = new JSONObject();
            o.put("id", i.id);
            o.put("fecha", i.fecha);
            o.put("plantaEntregada", i.plantaEntregada);
            o.put("plantaRecibida", i.plantaRecibida);
            o.put("persona", i.persona);
            o.put("notas", i.notas);
            o.put("sincronizado", i.sincronizado);
            intercambiosArray.put(o);
        }
        root.put("intercambios", intercambiosArray);

        JSONArray cuentasArray = new JSONArray();
        for (Cuenta c : db.cuentaDao().getAll()) {
            JSONObject o = new JSONObject();
            o.put("id", c.id);
            o.put("fecha", c.fecha);
            o.put("tipo", c.tipo);
            o.put("concepto", c.concepto);
            o.put("monto", c.monto);
            o.put("persona", c.persona);
            o.put("vencimiento", c.vencimiento);
            o.put("pagada", c.pagada);
            o.put("notas", c.notas);
            o.put("creadoEn", c.creadoEn);
            o.put("ventaGeneradaId", c.ventaGeneradaId);
            cuentasArray.put(o);
        }
        root.put("cuentas", cuentasArray);

        return root;
    }

    public static File writeBackup(Context context, File file) throws Exception {
        JSONObject root = buildBackup(context);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(root.toString(2));
        }
        return file;
    }

    /**
     * Escribe el backup en cualquier destino (por ejemplo una URI de SAF/Drive).
     */
    public static void writeBackup(Context context, OutputStream out) throws Exception {
        JSONObject root = buildBackup(context);
        out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static void importBackup(Context context, JSONObject root) throws Exception {
        AppDatabase db = AppDatabase.getInstance(context);

        if (root.has("ventas")) {
            db.ventaDao().deleteAll();
            JSONArray arr = root.getJSONArray("ventas");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Venta v = new Venta();
                v.id = o.getLong("id");
                v.fecha = o.getLong("fecha");
                v.cantidad = o.getInt("cantidad");
                v.precioUnitario = o.getDouble("precioUnitario");
                v.total = o.getDouble("total");
                v.lugar = o.getString("lugar");
                v.categoria = o.getString("categoria");
                v.infoVenta = o.optString("infoVenta", null);
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
                Gasto g = new Gasto();
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
                Pedido p = new Pedido();
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
                PlannedVenta pv = new PlannedVenta();
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
                WishPlanta w = new WishPlanta();
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

        if (root.has("intercambios")) {
            db.intercambioDao().deleteAll();
            JSONArray arr = root.getJSONArray("intercambios");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Intercambio in = new Intercambio();
                in.id = o.getLong("id");
                in.fecha = o.getLong("fecha");
                in.plantaEntregada = o.optString("plantaEntregada", "");
                in.plantaRecibida = o.optString("plantaRecibida", "");
                in.persona = o.optString("persona", "");
                in.notas = o.optString("notas", "");
                in.sincronizado = o.optBoolean("sincronizado", false);
                db.intercambioDao().insert(in);
            }
        }

        if (root.has("cuentas")) {
            db.cuentaDao().deleteAll();
            JSONArray arr = root.getJSONArray("cuentas");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Cuenta c = new Cuenta();
                c.id = o.getLong("id");
                c.fecha = o.getLong("fecha");
                c.tipo = o.getString("tipo");
                c.concepto = o.getString("concepto");
                c.monto = o.getDouble("monto");
                c.persona = o.optString("persona", "");
                c.vencimiento = o.optLong("vencimiento", 0);
                c.pagada = o.optBoolean("pagada", false);
                c.notas = o.optString("notas", "");
                c.creadoEn = o.optLong("creadoEn", 0);
                c.ventaGeneradaId = o.optLong("ventaGeneradaId", 0);
                db.cuentaDao().insert(c);
            }
        }
    }
}
