package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Gasto;

import java.util.List;

@Dao
public interface GastoDao {
    @Insert
    long insert(Gasto gasto);

    @Update
    void update(Gasto gasto);

    @Delete
    void delete(Gasto gasto);

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    List<Gasto> getAll();

    @Query("SELECT * FROM gastos WHERE id = :id")
    Gasto getById(long id);

    @Query("SELECT * FROM gastos WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    List<Gasto> getByFechaRango(long inicio, long fin);

    @Query("SELECT * FROM gastos WHERE tipo = :tipo ORDER BY fecha DESC")
    List<Gasto> getByTipo(String tipo);

    @Query("SELECT SUM(monto) FROM gastos")
    Double getTotalGeneral();

    @Query("SELECT SUM(monto) FROM gastos WHERE fecha BETWEEN :inicio AND :fin")
    Double getTotalRango(long inicio, long fin);

    @Query("SELECT SUM(monto) FROM gastos WHERE tipo = :tipo")
    Double getTotalPorTipo(String tipo);

    @Query("SELECT SUM(monto) FROM gastos WHERE tipo = :tipo AND fecha BETWEEN :inicio AND :fin")
    Double getTotalTipoRango(String tipo, long inicio, long fin);

    @Query("DELETE FROM gastos")
    void deleteAll();
}