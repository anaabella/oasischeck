package com.oasischeck.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.util.NotificationHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Revisa tareas pendientes y muestra una notificación resumen.
 */
public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            int pedidos = db.pedidoDao().getByEstado(Pedido.ESTADO_PENDIENTE).size()
                    + db.pedidoDao().getByEstado(Pedido.ESTADO_PREPARADO).size();

            long ahora = System.currentTimeMillis();
            Calendar limite = Calendar.getInstance();
            limite.add(Calendar.DAY_OF_MONTH, 7);
            long dentroDeUnaSemana = limite.getTimeInMillis();

            int planificadas = 0;
            List<PlannedVenta> pendientes = db.plannedVentaDao().getByEstado(PlannedVenta.ESTADO_PENDIENTE);
            for (PlannedVenta p : pendientes) {
                if (p.fecha >= ahora && p.fecha <= dentroDeUnaSemana) planificadas++;
            }

            Calendar vencimiento = Calendar.getInstance();
            vencimiento.add(Calendar.DAY_OF_MONTH, 3);
            List<Cuenta> cuentas = db.cuentaDao().getPorVencer(vencimiento.getTimeInMillis());

            NotificationHelper.mostrarRecordatorio(getApplicationContext(), pedidos, planificadas, cuentas.size());
            return Result.success();
        } catch (Exception e) {
            return Result.success();
        }
    }
}
