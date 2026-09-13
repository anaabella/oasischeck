package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.IntercambioDao;
import com.oasischeck.data.model.Intercambio;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntercambioRepository {
    private final IntercambioDao intercambioDao;
    private final ExecutorService executor;

    public IntercambioRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        intercambioDao = db.intercambioDao();
        executor = Executors.newFixedThreadPool(2);
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(Intercambio intercambio, Callback<Long> callback) {
        executor.execute(() -> { long id = intercambioDao.insert(intercambio); if (callback != null) callback.onResult(id); });
    }
    public void update(Intercambio intercambio, Runnable callback) {
        executor.execute(() -> { intercambioDao.update(intercambio); if (callback != null) callback.run(); });
    }
    public void delete(Intercambio intercambio, Runnable callback) {
        executor.execute(() -> { intercambioDao.delete(intercambio); if (callback != null) callback.run(); });
    }
    public void getAll(Callback<List<Intercambio>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(intercambioDao.getAll()); });
    }
    public void getById(long id, Callback<Intercambio> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(intercambioDao.getById(id)); });
    }
    public void search(String q, Callback<List<Intercambio>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(intercambioDao.search("%" + q + "%")); });
    }
    public void getByPersona(String persona, Callback<List<Intercambio>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(intercambioDao.getByPersona(persona)); });
    }
    public IntercambioDao getIntercambioDao() { return intercambioDao; }
}
