package com.oasischeck.data.repository;

import android.app.Application;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.VentaDao;
import com.oasischeck.data.model.Venta;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VentaRepository {
    private final VentaDao ventaDao;
    private final ExecutorService executor;

    public VentaRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        ventaDao = db.ventaDao();
        executor = Executors.newFixedThreadPool(2);
    }

    public interface Callback<T> { void onResult(T result); }

    public void insert(Venta venta, Callback<Long> callback) {
        executor.execute(() -> { long id = ventaDao.insert(venta); if (callback != null) callback.onResult(id); });
    }
    public void update(Venta venta, Runnable callback) {
        executor.execute(() -> { ventaDao.update(venta); if (callback != null) callback.run(); });
    }
    public void delete(Venta venta, Runnable callback) {
        executor.execute(() -> { ventaDao.delete(venta); if (callback != null) callback.run(); });
    }
    public void getAll(Callback<List<Venta>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(ventaDao.getAll()); });
    }
    public void getById(long id, Callback<Venta> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(ventaDao.getById(id)); });
    }
    public void getNoSincronizadas(Callback<List<Venta>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(ventaDao.getNoSincronizadas()); });
    }
    public void search(String q, Callback<List<Venta>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(ventaDao.search("%" + q + "%")); });
    }
    public void getTotalVentas(Callback<Double> callback) {
        executor.execute(() -> { Double t = ventaDao.getTotalVentas(); if (callback != null) callback.onResult(t != null ? t : 0); });
    }
    public void getTotalInversiones(Callback<Double> callback) {
        executor.execute(() -> { Double t = ventaDao.getTotalInversiones(); if (callback != null) callback.onResult(t != null ? t : 0); });
    }
    public void getTotalPorLugar(String lugar, Callback<Double> callback) {
        executor.execute(() -> { Double t = ventaDao.getTotalPorLugar(lugar); if (callback != null) callback.onResult(t != null ? t : 0); });
    }
    public void getTotalUnidades(Callback<Integer> callback) {
        executor.execute(() -> { Integer t = ventaDao.getTotalUnidadesVendidas(); if (callback != null) callback.onResult(t != null ? t : 0); });
    }
    public void getTopPlantas(int limit, Callback<List<VentaDao.PlantaStats>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(ventaDao.getTopPlantas(limit)); });
    }
    public void getTopPlantasRango(long inicio, long fin, Callback<List<VentaDao.PlantaStats>> callback) {
        executor.execute(() -> { if (callback != null) callback.onResult(ventaDao.getTopPlantasRango(inicio, fin)); });
    }
    public void getTotalPorDuenia(String duenia, Callback<Double> callback) {
        executor.execute(() -> { Double t = ventaDao.getTotalPorDuenia(duenia); if (callback != null) callback.onResult(t != null ? t : 0); });
    }
    public void getTotalPorEstadoDinero(String estado, Callback<Double> callback) {
        executor.execute(() -> { Double t = ventaDao.getTotalPorEstadoDinero(estado); if (callback != null) callback.onResult(t != null ? t : 0); });
    }
    public VentaDao getVentaDao() { return ventaDao; }
}