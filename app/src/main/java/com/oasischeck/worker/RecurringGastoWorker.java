package com.oasischeck.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Gasto;

import java.util.Calendar;
import java.util.List;

public class RecurringGastoWorker extends Worker {

    public RecurringGastoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Gasto> allGastos = db.gastoDao().getAll();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioMes = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        long finMes = cal.getTimeInMillis();

        List<Gasto> gastosMes = db.gastoDao().getByFechaRango(inicioMes, finMes);

        for (Gasto gasto : allGastos) {
            if (!gasto.recurrente) continue;

            boolean exists = false;
            for (Gasto gMes : gastosMes) {
                if (gMes.concepto != null && gMes.concepto.equals(gasto.concepto)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                Gasto nuevo = new Gasto();
                nuevo.fecha = System.currentTimeMillis();
                nuevo.concepto = gasto.concepto;
                nuevo.monto = gasto.monto;
                nuevo.tipo = gasto.tipo;
                nuevo.recurrente = true;
                nuevo.notas = gasto.notas;
                db.gastoDao().insert(nuevo);
            }
        }

        return Result.success();
    }
}
