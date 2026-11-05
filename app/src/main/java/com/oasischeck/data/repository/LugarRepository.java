package com.oasischeck.data.repository;

import android.app.Application;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.LugarDao;
import com.oasischeck.data.model.Lugar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LugarRepository {
    private final LugarDao lugarDao;
    private final ExecutorService executor;

    public LugarRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        lugarDao = db.lugarDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(Lugar lugar, Callback<Long> callback) {
        executor.execute(() -> {
            long id = lugarDao.insert(lugar);
            if (callback != null) callback.onResult(id);
        });
    }

    public void update(Lugar lugar, Runnable callback) {
        executor.execute(() -> {
            lugarDao.update(lugar);
            if (callback != null) callback.run();
        });
    }

    public void delete(Lugar lugar, Runnable callback) {
        executor.execute(() -> {
            lugarDao.delete(lugar);
            if (callback != null) callback.run();
        });
    }

    public void getActivos(Callback<List<Lugar>> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(lugarDao.getActivos());
        });
    }

    public void getTodos(Callback<List<Lugar>> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(lugarDao.getTodos());
        });
    }
}