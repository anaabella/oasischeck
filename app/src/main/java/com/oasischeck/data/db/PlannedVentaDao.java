package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.PlannedVenta;

import java.util.List;

@Dao
public interface PlannedVentaDao {
    @Insert
    long insert(PlannedVenta plannedVenta);

    @Update
    void update(PlannedVenta plannedVenta);

    @Delete
    void delete(PlannedVenta plannedVenta);

    @Query("SELECT * FROM ventas_planificadas ORDER BY fecha ASC, creadoEn ASC")
    List<PlannedVenta> getAll();

    @Query("SELECT * FROM ventas_planificadas WHERE id = :id")
    PlannedVenta getById(long id);

    @Query("SELECT * FROM ventas_planificadas WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha ASC, creadoEn ASC")
    List<PlannedVenta> getByFechaRango(long inicio, long fin);

    @Query("SELECT * FROM ventas_planificadas WHERE fecha = :fecha ORDER BY creadoEn ASC")
    List<PlannedVenta> getByFecha(long fecha);

    @Query("SELECT * FROM ventas_planificadas WHERE estado = :estado ORDER BY fecha ASC")
    List<PlannedVenta> getByEstado(int estado);

    @Query("SELECT * FROM ventas_planificadas WHERE cliente LIKE :cliente ORDER BY fecha ASC")
    List<PlannedVenta> searchByCliente(String cliente);

    @Query("DELETE FROM ventas_planificadas")
    void deleteAll();
}