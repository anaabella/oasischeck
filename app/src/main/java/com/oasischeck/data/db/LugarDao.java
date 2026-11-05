package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Lugar;

import java.util.List;

@Dao
public interface LugarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Lugar lugar);

    @Update
    void update(Lugar lugar);

    @Delete
    void delete(Lugar lugar);

    @Query("SELECT * FROM lugares WHERE activo = 1 ORDER BY orden ASC, nombre ASC")
    List<Lugar> getActivos();

    @Query("SELECT * FROM lugares ORDER BY orden ASC, nombre ASC")
    List<Lugar> getTodos();

    @Query("SELECT * FROM lugares WHERE id = :id")
    Lugar getById(long id);

    @Query("DELETE FROM lugares")
    void deleteAll();
}