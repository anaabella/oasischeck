package com.oasischeck.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Cuenta;

import java.util.concurrent.Executors;

public final class SaldoHelper {

    private static final String PREFS = "oasischeck_prefs";
    private static final String KEY_SALDO_INICIAL = "saldo_inicial";
    private static final String KEY_CONFIGURADO = "saldo_inicial_configurado";

    private SaldoHelper() {
    }

    public static boolean estaConfigurado(Context context) {
        return prefs(context).getBoolean(KEY_CONFIGURADO, false);
    }

    public static double getSaldoInicial(Context context) {
        return Double.longBitsToDouble(prefs(context).getLong(KEY_SALDO_INICIAL, Double.doubleToLongBits(0)));
    }

    public static void setSaldoInicial(Context context, double valor) {
        prefs(context).edit()
                .putLong(KEY_SALDO_INICIAL, Double.doubleToLongBits(valor))
                .putBoolean(KEY_CONFIGURADO, true)
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public interface Callback {
        void onResult(double inicial, double ventas, double inversiones, double gastos, double disponible);
    }

    public static void calcular(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(app);
            Double totalVentas = db.ventaDao().getTotalVentas();
            Double totalInversiones = db.ventaDao().getTotalInversiones();
            Double totalGastos = db.gastoDao().getTotalGeneral();
            Double cobrosParciales = db.cuentaDao().getTotalPagosSinVenta(Cuenta.TIPO_COBRAR);
            Double pagosDeuda = db.cuentaDao().getTotalPagosSinVenta(Cuenta.TIPO_PAGAR);

            double ventas = totalVentas != null ? totalVentas : 0;
            double inversiones = totalInversiones != null ? totalInversiones : 0;
            double gastos = totalGastos != null ? totalGastos : 0;
            double cobros = cobrosParciales != null ? cobrosParciales : 0;
            double deudas = pagosDeuda != null ? pagosDeuda : 0;
            double inicial = getSaldoInicial(app);
            double disponible = inicial + ventas - inversiones - gastos + cobros - deudas;

            callback.onResult(inicial, ventas, inversiones, gastos, disponible);
        });
    }
}
