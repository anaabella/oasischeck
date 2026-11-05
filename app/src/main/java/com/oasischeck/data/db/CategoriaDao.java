package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Categoria;

import java.util.List;

@Dao
public interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Categoria categoria);

    @Update
    void update(Categoria categoria);

    @Delete
    void delete(Categoria categoria);

    @Query("SELECT * FROM categorias WHERE activo = 1 ORDER BY orden ASC, nombre ASC")
    List<Categoria> getActivas();

    @Query("SELECT * FROM categorias ORDER BY orden ASC, nombre ASC")
    List<Categoria> getTodas();

    @Query("SELECT * FROM categorias WHERE id = :id")
    Categoria getById(long id);

    @Query("DELETE FROM categorias")
    void deleteAll();
}