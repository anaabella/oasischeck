package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.GastoDao;
import com.oasischeck.data.db.SyncStateDao;
import com.oasischeck.data.db.SyncTracker;
import com.oasischeck.data.model.Gasto;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GastoRepository {
    private final GastoDao dao;
    private final SyncStateDao syncDao;
    private final ExecutorService executor;

    public GastoRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.gastoDao();
        syncDao = db.syncStateDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(Gasto g, Callback<Long> cb) {
        executor.execute(() -> {
            long ts = System.currentTimeMillis();
            g.actualizadoEn = ts;
            long id = dao.insert(g);
            SyncTracker.registrar(syncDao, SyncTracker.TIPO_GASTO, id, ts);
            if (cb != null) cb.onResult(id);
        });
    }
    public void update(Gasto g, Runnable cb) {
        executor.execute(() -> {
            long ts = System.currentTimeMillis();
            g.actualizadoEn = ts;
            dao.update(g);
            SyncTracker.registrar(syncDao, SyncTracker.TIPO_GASTO, g.id, ts);
            if (cb != null) cb.run();
        });
    }
    public void delete(Gasto g, Runnable cb) {
        executor.execute(() -> {
            SyncTracker.marcarEliminado(syncDao, SyncTracker.TIPO_GASTO, g.id);
            dao.delete(g);
            if (cb != null) cb.run();
        });
    }
    public void getAll(Callback<List<Gasto>> cb) {
        executor.execute(() -> { if (cb != null) cb.onResult(dao.getAll()); });
    }
    public void getByRango(long inicio, long fin, Callback<List<Gasto>> cb) {
        executor.execute(() -> { if (cb != null) cb.onResult(dao.getByFechaRango(inicio, fin)); });
    }
    public void getTotalMes(long inicio, long fin, Callback<Double> cb) {
        executor.execute(() -> { Double t = dao.getTotalRango(inicio, fin); if (cb != null) cb.onResult(t != null ? t : 0); });
    }
    public void getTotalTipo(String tipo, Callback<Double> cb) {
        executor.execute(() -> { Double t = dao.getTotalPorTipo(tipo); if (cb != null) cb.onResult(t != null ? t : 0); });
    }
    public void getTotalTipoRango(String tipo, long inicio, long fin, Callback<Double> cb) {
        executor.execute(() -> { Double t = dao.getTotalTipoRango(tipo, inicio, fin); if (cb != null) cb.onResult(t != null ? t : 0); });
    }
}