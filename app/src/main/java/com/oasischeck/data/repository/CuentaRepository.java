package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.CuentaDao;
import com.oasischeck.data.db.SyncStateDao;
import com.oasischeck.data.db.SyncTracker;
import com.oasischeck.data.model.Cuenta;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CuentaRepository {
    private final CuentaDao dao;
    private final SyncStateDao syncDao;
    private final ExecutorService executor;

    public CuentaRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.cuentaDao();
        syncDao = db.syncStateDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(Cuenta c, Callback<Long> cb) {
        executor.execute(() -> {
            long ts = System.currentTimeMillis();
            c.actualizadoEn = ts;
            long id = dao.insert(c);
            SyncTracker.registrar(syncDao, SyncTracker.TIPO_CUENTA, id, ts);
            if (cb != null) cb.onResult(id);
        });
    }

    public void update(Cuenta c, Runnable cb) {
        executor.execute(() -> {
            long ts = System.currentTimeMillis();
            c.actualizadoEn = ts;
            dao.update(c);
            SyncTracker.registrar(syncDao, SyncTracker.TIPO_CUENTA, c.id, ts);
            if (cb != null) cb.run();
        });
    }

    public void delete(Cuenta c, Runnable cb) {
        executor.execute(() -> {
            SyncTracker.marcarEliminado(syncDao, SyncTracker.TIPO_CUENTA, c.id);
            dao.delete(c);
            if (cb != null) cb.run();
        });
    }

    public void getAll(Callback<List<Cuenta>> cb) {
        executor.execute(() -> { if (cb != null) cb.onResult(dao.getAll()); });
    }

    public void getById(long id, Callback<Cuenta> cb) {
        executor.execute(() -> { if (cb != null) cb.onResult(dao.getById(id)); });
    }
}
