package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.PlannedVentaDao;
import com.oasischeck.data.model.PlannedVenta;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlannedVentaRepository {
    private final PlannedVentaDao dao;
    private final ExecutorService executor;

    public PlannedVentaRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.plannedVentaDao();
        executor = Executors.newFixedThreadPool(2);
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    public void insert(PlannedVenta p, Callback<Long> callback) {
        executor.execute(() -> {
            long id = dao.insert(p);
            if (callback != null) callback.onResult(id);
        });
    }

    public void update(PlannedVenta p, Runnable callback) {
        executor.execute(() -> {
            dao.update(p);
            if (callback != null) callback.run();
        });
    }

    public void delete(PlannedVenta p, Runnable callback) {
        executor.execute(() -> {
            dao.delete(p);
            if (callback != null) callback.run();
        });
    }

    public void getAll(Callback<List<PlannedVenta>> callback) {
        executor.execute(() -> {
            List<PlannedVenta> list = dao.getAll();
            if (callback != null) callback.onResult(list);
        });
    }

    public void getById(long id, Callback<PlannedVenta> callback) {
        executor.execute(() -> {
            PlannedVenta p = dao.getById(id);
            if (callback != null) callback.onResult(p);
        });
    }

    public void getByFecha(long fecha, Callback<List<PlannedVenta>> callback) {
        executor.execute(() -> {
            List<PlannedVenta> list = dao.getByFecha(fecha);
            if (callback != null) callback.onResult(list);
        });
    }

    public void getByFechaRango(long inicio, long fin, Callback<List<PlannedVenta>> callback) {
        executor.execute(() -> {
            List<PlannedVenta> list = dao.getByFechaRango(inicio, fin);
            if (callback != null) callback.onResult(list);
        });
    }

    public void getPendientes(Callback<List<PlannedVenta>> callback) {
        executor.execute(() -> {
            List<PlannedVenta> list = dao.getByEstado(PlannedVenta.ESTADO_PENDIENTE);
            if (callback != null) callback.onResult(list);
        });
    }

    public void searchByCliente(String cliente, Callback<List<PlannedVenta>> callback) {
        executor.execute(() -> {
            List<PlannedVenta> list = dao.searchByCliente("%" + cliente + "%");
            if (callback != null) callback.onResult(list);
        });
    }

    public PlannedVentaDao getDao() {
        return dao;
    }
}