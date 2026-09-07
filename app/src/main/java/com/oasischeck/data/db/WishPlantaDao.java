package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.WishPlanta;

import java.util.List;

@Dao
public interface WishPlantaDao {
    @Insert
    long insert(WishPlanta wish);

    @Update
    void update(WishPlanta wish);

    @Delete
    void delete(WishPlanta wish);

    @Query("SELECT * FROM wishlist ORDER BY comprada ASC, nombre ASC")
    List<WishPlanta> getAll();

    @Query("SELECT * FROM wishlist WHERE id = :id")
    WishPlanta getById(long id);

    @Query("SELECT * FROM wishlist WHERE comprada = 0 ORDER BY nombre ASC")
    List<WishPlanta> getPendientes();

    @Query("SELECT * FROM wishlist WHERE comprada = 1 ORDER BY fechaComprada DESC")
    List<WishPlanta> getCompradas();

    @Query("SELECT * FROM wishlist WHERE nombre LIKE :q ORDER BY nombre ASC")
    List<WishPlanta> search(String q);
}