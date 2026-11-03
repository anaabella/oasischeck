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
 * Copia de seguridad automática diaria. Mantiene los últimos 7 respaldos.
 */
public class AutoBackupWorker extends Worker {

    private static final int MAX_BACKUPS = 7;

    public AutoBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            File dir = new File(getApplicationContext().getFilesDir(), "backups");
            if (!dir.exists() && !dir.mkdirs()) return Result.retry();

            String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            File destino = new File(dir, "auto_backup_" + fecha + ".json");
            BackupManager.writeBackup(getApplicationContext(), destino);

            File[] archivos = dir.listFiles((d, name) -> name.startsWith("auto_backup_") && name.endsWith(".json"));
            if (archivos != null && archivos.length > MAX_BACKUPS) {
                Arrays.sort(archivos, Comparator.comparingLong(File::lastModified));
                for (int i = 0; i < archivos.length - MAX_BACKUPS; i++) {
                    //noinspection ResultOfMethodCallIgnored
                    archivos[i].delete();
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
