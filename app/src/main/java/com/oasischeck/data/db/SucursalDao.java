package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Sucursal;

import java.util.List;

@Dao
public interface SucursalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Sucursal sucursal);

    @Update
    void update(Sucursal sucursal);

    @Delete
    void delete(Sucursal sucursal);

    @Query("SELECT * FROM sucursales WHERE activa = 1")
    List<Sucursal> getActivas();

    @Query("SELECT * FROM sucursales")
    List<Sucursal> getTodas();

    @Query("SELECT * FROM sucursales WHERE id = :id")
    Sucursal getById(long id);

    @Query("DELETE FROM sucursales")
    void deleteAll();
}