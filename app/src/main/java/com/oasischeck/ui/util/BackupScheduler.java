package com.oasischeck.ui.util;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.oasischeck.worker.NightlyBackupWorker;

import java.util.concurrent.TimeUnit;

public class BackupScheduler {

    private static final String WORK_NIGHTLY_BACKUP = "nightly_backup";

    /**
     * Programa el backup nocturno para ejecutarse todos los días a las 02:00 AM.
     * Usa WorkManager con PeriodicWorkRequest cada 24 horas con un delay inicial
     * calculado para que la primera ejecución sea a las 02:00 AM del día siguiente.
     */
    public static void programarBackupNocturno(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);

        // Cancelar trabajo existente si lo hay
        workManager.cancelUniqueWork(WORK_NIGHTLY_BACKUP);

        // Calcular delay para las 02:00 AM del día siguiente
        long delayMs = calcularDelayHastaLasDosAM();

        PeriodicWorkRequest backupRequest = new PeriodicWorkRequest.Builder(
                NightlyBackupWorker.class,
                24, TimeUnit.HOURS
        )
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueueUniquePeriodicWork(
                WORK_NIGHTLY_BACKUP,
                ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
        );
    }

    /**
     * Calcula los milisegundos hasta las 02:00 AM del día siguiente.
     */
    private static long calcularDelayHastaLasDosAM() {
        java.util.Calendar ahora = java.util.Calendar.getInstance();
        java.util.Calendar dosAM = java.util.Calendar.getInstance();
        dosAM.set(java.util.Calendar.HOUR_OF_DAY, 2);
        dosAM.set(java.util.Calendar.MINUTE, 0);
        dosAM.set(java.util.Calendar.SECOND, 0);
        dosAM.set(java.util.Calendar.MILLISECOND, 0);

        // Si ya son las 02:00 AM o más, programar para el día siguiente
        if (ahora.getTimeInMillis() >= dosAM.getTimeInMillis()) {
            dosAM.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        return dosAM.getTimeInMillis() - ahora.getTimeInMillis();
    }

    public static void cancelarBackupNocturno(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NIGHTLY_BACKUP);
    }
}