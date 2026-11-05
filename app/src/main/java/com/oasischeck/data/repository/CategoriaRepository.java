package com.oasischeck.data.repository;

import android.app.Application;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.CategoriaDao;
import com.oasischeck.data.model.Categoria;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoriaRepository {
    private final CategoriaDao categoriaDao;
    private final ExecutorService executor;

    public CategoriaRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        categoriaDao = db.categoriaDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(Categoria categoria, Callback<Long> callback) {
        executor.execute(() -> {
            long id = categoriaDao.insert(categoria);
            if (callback != null) callback.onResult(id);
        });
    }

    public void update(Categoria categoria, Runnable callback) {
        executor.execute(() -> {
            categoriaDao.update(categoria);
            if (callback != null) callback.run();
        });
    }

    public void delete(Categoria categoria, Runnable callback) {
        executor.execute(() -> {
            categoriaDao.delete(categoria);
            if (callback != null) callback.run();
        });
    }

    public void getActivas(Callback<List<Categoria>> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(categoriaDao.getActivas());
        });
    }

    public void getTodas(Callback<List<Categoria>> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(categoriaDao.getTodas());
        });
    }
}