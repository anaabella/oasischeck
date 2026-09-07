package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.WishPlantaDao;
import com.oasischeck.data.model.WishPlanta;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WishPlantaRepository {
    private final WishPlantaDao dao;
    private final ExecutorService executor;

    public WishPlantaRepository(Application app) {
        dao = AppDatabase.getInstance(app).wishPlantaDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(WishPlanta w, Callback<Long> cb) {
        executor.execute(() -> { long id = dao.insert(w); if (cb != null) cb.onResult(id); });
    }
    public void update(WishPlanta w, Runnable cb) {
        executor.execute(() -> { dao.update(w); if (cb != null) cb.run(); });
    }
    public void delete(WishPlanta w, Runnable cb) {
        executor.execute(() -> { dao.delete(w); if (cb != null) cb.run(); });
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