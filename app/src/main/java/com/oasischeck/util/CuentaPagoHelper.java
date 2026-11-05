package com.oasischeck.util;

import android.app.Application;
import android.content.Context;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.repository.VentaRepository;
import com.oasischeck.sheets.GoogleSheetsService;

/**
 * Genera la venta asociada cuando una cuenta por cobrar queda saldada.
 */
public final class CuentaPagoHelper {

    private CuentaPagoHelper() {}

    public static void generarVentaSiCorresponde(Context context, Cuenta cuenta, boolean estabaPagada) {
        if (!cuenta.pagada || estabaPagada) return;
        if (!Cuenta.TIPO_COBRAR.equals(cuenta.tipo)) return;
        if (cuenta.ventaGeneradaId > 0) return;

        Venta venta = new Venta(cuenta.fecha, 1, cuenta.monto, "Entrega", "Plantas",
                "Cobro: " + (cuenta.concepto != null ? cuenta.concepto : ""));
        venta.nombrePlanta = cuenta.concepto != null ? cuenta.concepto : "";
        venta.cliente = cuenta.persona != null ? cuenta.persona : "";
        venta.duenia = Venta.DUENIA_AMBAS;
        venta.estadoDinero = Venta.ESTADO_EMPRENDIMIENTO;
        venta.sincronizado = false;

        Application app = (Application) context.getApplicationContext();
        VentaRepository ventaRepository = new VentaRepository(app);
        ventaRepository.insert(venta, id -> {
            venta.id = id;
            GoogleSheetsService.sincronizarVenta(context.getApplicationContext(), venta);
            new Thread(() -> {
                cuenta.ventaGeneradaId = id;
                AppDatabase.getInstance(context.getApplicationContext()).cuentaDao().update(cuenta);
            }).start();
        });
    }
}
