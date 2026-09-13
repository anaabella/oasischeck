package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Intercambio;

import java.util.List;

@Dao
public interface IntercambioDao {
    @Insert
    long insert(Intercambio intercambio);

    @Update
    void update(Intercambio intercambio);

    @Delete
    void delete(Intercambio intercambio);

    @Query("SELECT * FROM intercambios ORDER BY fecha DESC, id DESC")
    List<Intercambio> getAll();

    @Query("SELECT * FROM intercambios WHERE id = :id")
    Intercambio getById(long id);

    @Query("SELECT * FROM intercambios WHERE sincronizado = 0")
    List<Intercambio> getNoSincronizadas();

    @Query("SELECT * FROM intercambios WHERE persona LIKE :q OR plantaEntregada LIKE :q OR plantaRecibida LIKE :q ORDER BY fecha DESC")
    List<Intercambio> search(String q);

    @Query("SELECT * FROM intercambios WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha DESC")
    List<Intercambio> getByFechaRango(long fechaInicio, long fechaFin);

    @Query("SELECT * FROM intercambios WHERE persona = :persona ORDER BY fecha DESC")
    List<Intercambio> getByPersona(String persona);

    @Query("UPDATE intercambios SET sincronizado = 1 WHERE id = :id")
    void marcarSincronizado(long id);

    @Query("DELETE FROM intercambios")
    void deleteAll();
}
