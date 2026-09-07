package com.oasischeck.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Venta;
import com.oasischeck.sheets.GoogleSheetsService;

import java.util.List;

public class SyncRetryWorker extends Worker {

    public SyncRetryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Venta> unsynced = db.ventaDao().getNoSincronizadas();

        for (Venta venta : unsynced) {
            GoogleSheetsService.sincronizarVenta(getApplicationContext(), venta);
        }

        return Result.success();
    }
}
