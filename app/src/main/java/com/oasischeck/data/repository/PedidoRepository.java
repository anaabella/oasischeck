package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.PedidoDao;
import com.oasischeck.data.model.Pedido;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PedidoRepository {
    private final PedidoDao dao;
    private final ExecutorService executor;

    public PedidoRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.pedidoDao();
        executor = Executors.newFixedThreadPool(2);
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    public void insert(Pedido p, Callback<Long> callback) {
        executor.execute(() -> {
            long id = dao.insert(p);
            if (callback != null) callback.onResult(id);
        });
    }

    public void update(Pedido p, Runnable callback) {
        executor.execute(() -> {
            dao.update(p);
            if (callback != null) callback.run();
        });
    }

    public void delete(Pedido p, Runnable callback) {
        executor.execute(() -> {
            dao.delete(p);
            if (callback != null) callback.run();
        });
    }

    public void getAll(Callback<List<Pedido>> callback) {
        executor.execute(() -> {
            List<Pedido> list = dao.getAll();
            if (callback != null) callback.onResult(list);
        });
    }

    public void getById(long id, Callback<Pedido> callback) {
        executor.execute(() -> {
            Pedido p = dao.getById(id);
            if (callback != null) callback.onResult(p);
        });
    }

    public void getByEstado(int estado, Callback<List<Pedido>> callback) {
        executor.execute(() -> {
            List<Pedido> list = dao.getByEstado(estado);
            if (callback != null) callback.onResult(list);
        });
    }

    public void searchByCliente(String cliente, Callback<List<Pedido>> callback) {
        executor.execute(() -> {
            List<Pedido> list = dao.searchByCliente("%" + cliente + "%");
            if (callback != null) callback.onResult(list);
        });
    }

    public PedidoDao getDao() {
        return dao;
    }
}