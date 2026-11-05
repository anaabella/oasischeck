package com.oasischeck.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.oasischeck.util.BackupManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Copia de seguridad automática NOCTURNA.
 * Se ejecuta todas las noches a las 02:00 AM.
 * Mantiene los últimos 30 respaldos nocturnos.
 */
public class NightlyBackupWorker extends Worker {

    private static final int MAX_BACKUPS = 30;

    public NightlyBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            File dir = new File(getApplicationContext().getFilesDir(), "backups_nightly");
            if (!dir.exists() && !dir.mkdirs()) return Result.retry();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            File destino = new File(dir, "nightly_backup_" + timestamp + ".json");
            BackupManager.writeBackup(getApplicationContext(), destino);

            // Mantener solo los últimos 30 respaldos nocturnos
            File[] archivos = dir.listFiles((d, name) -> name.startsWith("nightly_backup_") && name.endsWith(".json"));
            if (archivos != null && archivos.length > MAX_BACKUPS) {
                Arrays.sort(archivos, Comparator.comparingLong(File::lastModified));
                for (int i = 0; i < archivos.length - MAX_BACKUPS; i++) {
                    archivos[i].delete();
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}