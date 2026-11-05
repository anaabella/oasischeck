package com.oasischeck.ui.clientes;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.ClienteInfo;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.model.Venta;
import com.oasischeck.ui.cuentas.CuentasActivity;
import com.oasischeck.ui.editar.EditarVentaActivity;
import com.oasischeck.ui.pedidos.AgregarPedidoActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DetalleClienteActivity extends AppCompatActivity {

    public static final String EXTRA_CLIENTE = "cliente";

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    private static final String SIN_DEFINIR = "Sin definir";
    private static final String[] ORIGENES = {
            SIN_DEFINIR, ClienteInfo.ORIGEN_WHATSAPP, ClienteInfo.ORIGEN_FACEBOOK
    };

    private String cliente;
    private String clave;
    private LinearLayout contenedor;
    private TextView tvResumenVentas, tvResumenPedidos, tvResumenCobrar, tvResumenPagar;
    private TextView tvOrigen, tvWhatsapp, tvFacebook;
    private MaterialButton btnAbrirWhatsApp, btnAbrirFacebook;
    private ClienteInfo infoActual;

    private final StringBuilder compartir = new StringBuilder();
    private double totalVentas, totalPedidos, totalPlanificadas, porCobrar, porPagar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_cliente);

        cliente = getIntent().getStringExtra(EXTRA_CLIENTE);
        if (cliente == null || cliente.trim().isEmpty()) {
            finish();
            return;
        }
        clave = HistorialClientesActivity.normalizar(cliente);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(cliente);
        toolbar.setNavigationOnClickListener(v -> finish());

        contenedor = findViewById(R.id.contenedor_cliente);
        tvResumenVentas = findViewById(R.id.tv_resumen_ventas);
        tvResumenPedidos = findViewById(R.id.tv_resumen_pedidos);
        tvResumenCobrar = findViewById(R.id.tv_resumen_cobrar);
        tvResumenPagar = findViewById(R.id.tv_resumen_pagar);
        tvOrigen = findViewById(R.id.tv_origen_cliente);
        tvWhatsapp = findViewById(R.id.tv_whatsapp_cliente);
        tvFacebook = findViewById(R.id.tv_facebook_cliente);
        btnAbrirWhatsApp = findViewById(R.id.btn_abrir_whatsapp);
        btnAbrirFacebook = findViewById(R.id.btn_abrir_facebook);

        findViewById(R.id.btn_compartir_cliente).setOnClickListener(v -> compartir());
        findViewById(R.id.btn_editar_datos_cliente).setOnClickListener(v -> editarDatosCliente());
        btnAbrirWhatsApp.setOnClickListener(v -> abrirWhatsApp());
        btnAbrirFacebook.setOnClickListener(v -> abrirFacebook());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            List<Venta> ventas = new ArrayList<>();
            for (Venta v : db.ventaDao().getAll()) {
                if (coincide(v.cliente) && !"Inversion".equals(v.categoria)) ventas.add(v);
            }

            List<Pedido> pedidos = new ArrayList<>();
            for (Pedido p : db.pedidoDao().getAll()) {
                if (coincide(p.cliente)) pedidos.add(p);
            }

            List<PlannedVenta> planificadas = new ArrayList<>();
            for (PlannedVenta p : db.plannedVentaDao().getAll()) {
                if (coincide(p.cliente) && p.estado == PlannedVenta.ESTADO_PENDIENTE) planificadas.add(p);
            }

            List<Cuenta> cuentas = new ArrayList<>();
            for (Cuenta c : db.cuentaDao().getAll()) {
                if (coincide(c.persona) && c.getSaldoPendiente() > 0) cuentas.add(c);
            }

            ClienteInfo info = db.clienteInfoDao().getByClave(clave);

            Collections.sort(ventas, (a, b) -> Long.compare(b.fecha, a.fecha));
            Collections.sort(pedidos, (a, b) -> Long.compare(b.fecha, a.fecha));
            Collections.sort(planificadas, (a, b) -> Long.compare(b.fecha, a.fecha));
            Collections.sort(cuentas, (a, b) -> Long.compare(b.fecha, a.fecha));

            runOnUiThread(() -> {
                mostrar(ventas, pedidos, planificadas, cuentas);
                mostrarInfoCliente(info);
            });
        });
    }

    private void mostrarInfoCliente(ClienteInfo info) {
        infoActual = info;
        boolean tieneWhatsApp = info != null && info.tieneWhatsApp();
        boolean tieneFacebook = info != null && info.tieneFacebook();

        tvOrigen.setText("Origen: " + (info != null ? info.getOrigenMostrar() : SIN_DEFINIR));

        if (tieneWhatsApp) {
            tvWhatsapp.setText("WhatsApp: " + info.whatsapp);
            tvWhatsapp.setVisibility(View.VISIBLE);
        } else {
            tvWhatsapp.setVisibility(View.GONE);
        }

        if (tieneFacebook) {
            tvFacebook.setText("Facebook: " + info.facebook);
            tvFacebook.setVisibility(View.VISIBLE);
        } else {
            tvFacebook.setVisibility(View.GONE);
        }

        btnAbrirWhatsApp.setVisibility(tieneWhatsApp ? View.VISIBLE : View.GONE);
        btnAbrirFacebook.setVisibility(tieneFacebook ? View.VISIBLE : View.GONE);
    }

    private void editarDatosCliente() {
        View view = getLayoutInflater().inflate(R.layout.dialog_cliente_info, null);
        AutoCompleteTextView spinnerOrigen = view.findViewById(R.id.spinner_origen_cliente);
        TextInputEditText etWhatsApp = view.findViewById(R.id.et_whatsapp_cliente);
        TextInputEditText etFacebook = view.findViewById(R.id.et_facebook_cliente);

        spinnerOrigen.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, ORIGENES));
        spinnerOrigen.setKeyListener(null);

        if (infoActual != null) {
            spinnerOrigen.setText(infoActual.getOrigenMostrar(), false);
            etWhatsApp.setText(infoActual.whatsapp != null ? infoActual.whatsapp : "");
            etFacebook.setText(infoActual.facebook != null ? infoActual.facebook : "");
        } else {
            spinnerOrigen.setText(SIN_DEFINIR, false);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.datos_cliente)
                .setView(view)
                .setPositiveButton(R.string.guardar, (d, w) -> guardarInfoCliente(
                        spinnerOrigen.getText().toString(),
                        texto(etWhatsApp),
                        texto(etFacebook)))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void guardarInfoCliente(String origenSeleccionado, String whatsapp, String facebook) {
        String origen = SIN_DEFINIR.equalsIgnoreCase(origenSeleccionado) ? "" : origenSeleccionado.trim();
        String numero = whatsapp.trim();
        String perfil = facebook.trim();
        if (!perfil.isEmpty() && !perfil.startsWith("http://") && !perfil.startsWith("https://")) {
            perfil = "https://" + perfil;
        }

        ClienteInfo info = new ClienteInfo();
        info.clave = clave;
        info.nombre = cliente;
        info.origen = origen;
        info.whatsapp = numero;
        info.facebook = perfil;
        info.actualizadoEn = System.currentTimeMillis();

        final String perfilFinal = perfil;
        final boolean vacio = origen.isEmpty() && numero.isEmpty() && perfilFinal.isEmpty();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (vacio) {
                db.clienteInfoDao().deleteByClave(clave);
            } else {
                db.clienteInfoDao().upsert(info);
            }
            runOnUiThread(() -> {
                mostrarInfoCliente(vacio ? null : info);
                Toast.makeText(this, "Datos del cliente guardados", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString() : "";
    }

    private void abrirWhatsApp() {
        if (infoActual == null || !infoActual.tieneWhatsApp()) return;
        String numero = infoActual.whatsapp.replaceAll("[^0-9]", "");
        if (numero.isEmpty()) {
            Toast.makeText(this, "El número de WhatsApp no es válido", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + numero)));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirFacebook() {
        if (infoActual == null || !infoActual.tieneFacebook()) return;
        String url = infoActual.facebook.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir Facebook", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrar(List<Venta> ventas, List<Pedido> pedidos,
                         List<PlannedVenta> planificadas, List<Cuenta> cuentas) {
        contenedor.removeAllViews();
        compartir.setLength(0);
        totalVentas = 0;
        totalPedidos = 0;
        totalPlanificadas = 0;
        porCobrar = 0;
        porPagar = 0;

        if (!ventas.isEmpty()) {
            agregarSeccion("Ventas (" + ventas.size() + ")");
            for (Venta v : ventas) {
                totalVentas += v.total;
                String titulo = textoVenta(v);
                String subtitulo = dateFormat.format(new Date(v.fecha)) + " · " + v.cantidad + " u.";
                agregarFila(titulo, subtitulo, currencyFormat.format(v.total), 0, () -> {
                    Intent intent = new Intent(this, EditarVentaActivity.class);
                    intent.putExtra("venta_id", v.id);
                    startActivity(intent);
                });
            }
        }

        if (!pedidos.isEmpty()) {
            agregarSeccion("Pedidos (" + pedidos.size() + ")");
            for (Pedido p : pedidos) {
                totalPedidos += p.total;
                String subtitulo = dateFormat.format(new Date(p.fecha)) + " · " + estadoPedido(p.estado);
                agregarFila(p.productos != null ? p.productos : "Pedido", subtitulo,
                        currencyFormat.format(p.total), 0, () -> {
                            Intent intent = new Intent(this, AgregarPedidoActivity.class);
                            intent.putExtra("pedido_id", p.id);
                            startActivity(intent);
                        });
            }
        }

        if (!planificadas.isEmpty()) {
            agregarSeccion("Ventas planificadas (" + planificadas.size() + ")");
            for (PlannedVenta p : planificadas) {
                totalPlanificadas += p.totalEstimado;
                String subtitulo = dateFormat.format(new Date(p.fecha)) + " · pendiente";
                agregarFila(p.infoVenta != null ? p.infoVenta : "Venta planificada", subtitulo,
                        currencyFormat.format(p.totalEstimado), 0, null);
            }
        }

        if (!cuentas.isEmpty()) {
            agregarSeccion("Cuentas pendientes (" + cuentas.size() + ")");
            for (Cuenta c : cuentas) {
                double saldo = c.getSaldoPendiente();
                boolean cobrar = Cuenta.TIPO_COBRAR.equals(c.tipo);
                if (cobrar) porCobrar += saldo;
                else porPagar += saldo;

                String subtitulo = c.tipo;
                if (c.vencimiento > 0) {
                    subtitulo += " · vence " + dateFormat.format(new Date(c.vencimiento));
                }
                int color = ContextCompat.getColor(this, cobrar ? R.color.green_primary : R.color.red);
                agregarFila(c.concepto != null && !c.concepto.isEmpty() ? c.concepto : "Cuenta",
                        subtitulo, currencyFormat.format(saldo), color, () -> {
                            startActivity(new Intent(this, CuentasActivity.class));
                        });
            }
        }

        if (ventas.isEmpty() && pedidos.isEmpty() && planificadas.isEmpty() && cuentas.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText(R.string.sin_registros);
            vacio.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            vacio.setPadding(0, dp(24), 0, 0);
            contenedor.addView(vacio);
        }

        tvResumenVentas.setText("Ventas: " + currencyFormat.format(totalVentas)
                + " (" + ventas.size() + ")");
        tvResumenPedidos.setText("Pedidos: " + currencyFormat.format(totalPedidos)
                + " (" + pedidos.size() + ") · Planificadas: " + currencyFormat.format(totalPlanificadas));
        tvResumenCobrar.setText("Por cobrar: " + currencyFormat.format(porCobrar));
        tvResumenPagar.setText("Por pagar: " + currencyFormat.format(porPagar));

        compartir.insert(0, "Cliente: " + cliente + "\n"
                + "Ventas: " + currencyFormat.format(totalVentas) + " (" + ventas.size() + ")\n"
                + "Pedidos: " + currencyFormat.format(totalPedidos) + " (" + pedidos.size() + ")\n"
                + "Planificadas: " + currencyFormat.format(totalPlanificadas) + "\n"
                + "Por cobrar: " + currencyFormat.format(porCobrar) + "\n"
                + "Por pagar: " + currencyFormat.format(porPagar) + "\n");
    }

    private void agregarSeccion(String titulo) {
        TextView tv = new TextView(this);
        tv.setText(titulo);
        tv.setTextSize(15);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(16);
        params.bottomMargin = dp(4);
        contenedor.addView(tv, params);
        compartir.append("\n").append(titulo).append("\n");
    }

    private void agregarFila(String titulo, String subtitulo, String monto, int color, Runnable accion) {
        View row = getLayoutInflater().inflate(R.layout.item_historial_row, contenedor, false);
        ((TextView) row.findViewById(R.id.tv_fila_titulo)).setText(titulo);
        ((TextView) row.findViewById(R.id.tv_fila_subtitulo)).setText(subtitulo);
        TextView tvMonto = row.findViewById(R.id.tv_fila_monto);
        tvMonto.setText(monto);
        if (color != 0) tvMonto.setTextColor(color);
        if (accion != null) row.setOnClickListener(v -> accion.run());
        contenedor.addView(row);
        compartir.append("• ").append(titulo).append(" — ").append(subtitulo)
                .append(" — ").append(monto).append("\n");
    }

    private void compartir() {
        StringBuilder texto = new StringBuilder(compartir);
        if (infoActual != null) {
            texto.append("Origen: ").append(infoActual.getOrigenMostrar()).append("\n");
            if (infoActual.tieneWhatsApp()) {
                texto.append("WhatsApp: ").append(infoActual.whatsapp).append("\n");
            }
            if (infoActual.tieneFacebook()) {
                texto.append("Facebook: ").append(infoActual.facebook).append("\n");
            }
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, texto.toString());
        try {
            startActivity(Intent.createChooser(intent, "Compartir resumen"));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo compartir", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean coincide(String nombre) {
        return HistorialClientesActivity.normalizar(nombre).equals(clave);
    }

    private String textoVenta(Venta v) {
        if (v.nombrePlanta != null && !v.nombrePlanta.trim().isEmpty()) return v.nombrePlanta;
        if (v.infoVenta != null && !v.infoVenta.trim().isEmpty()) return v.infoVenta;
        return v.categoria != null ? v.categoria : "Venta";
    }

    private String estadoPedido(int estado) {
        switch (estado) {
            case Pedido.ESTADO_PREPARADO: return "Preparado";
            case Pedido.ESTADO_ENTREGADO: return "Entregado";
            default: return "Pedido";
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
