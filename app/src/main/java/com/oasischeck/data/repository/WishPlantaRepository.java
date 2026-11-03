package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.SyncStateDao;
import com.oasischeck.data.db.SyncTracker;
import com.oasischeck.data.db.WishPlantaDao;
import com.oasischeck.data.model.WishPlanta;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WishPlantaRepository {
    private final WishPlantaDao dao;
    private final SyncStateDao syncDao;
    private final ExecutorService executor;

    public WishPlantaRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.wishPlantaDao();
        syncDao = db.syncStateDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(WishPlanta w, Callback<Long> cb) {
        executor.execute(() -> {
            long ts = System.currentTimeMillis();
            w.actualizadoEn = ts;
            long id = dao.insert(w);
            SyncTracker.registrar(syncDao, SyncTracker.TIPO_WISH, id, ts);
            if (cb != null) cb.onResult(id);
        });
    }
    public void update(WishPlanta w, Runnable cb) {
        executor.execute(() -> {
            long ts = System.currentTimeMillis();
            w.actualizadoEn = ts;
            dao.update(w);
            SyncTracker.registrar(syncDao, SyncTracker.TIPO_WISH, w.id, ts);
            if (cb != null) cb.run();
        });
    }
    public void delete(WishPlanta w, Runnable cb) {
        executor.execute(() -> {
            SyncTracker.marcarEliminado(syncDao, SyncTracker.TIPO_WISH, w.id);
            dao.delete(w);
            if (cb != null) cb.run();
        });
    }
    public void getAll(Callback<List<WishPlanta>> cb) {
        executor.execute(() -> { if (cb != null) cb.onResult(dao.getAll()); });
    }
    public void getPendientes(Callback<List<WishPlanta>> cb) {
        executor.execute(() -> { if (cb != null) cb.onResult(dao.getPendientes()); });
    }
    public void search(String q, Callback<List<WishPlanta>> cb) {
        executor.execute(() -> { if (cb != null) cb.onResult(dao.search("%" + q + "%")); });
    }
}