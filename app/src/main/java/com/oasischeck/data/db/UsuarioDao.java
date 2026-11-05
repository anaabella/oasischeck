package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Usuario usuario);

    @Update
    void update(Usuario usuario);

    @Delete
    void delete(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE activo = 1")
    List<Usuario> getActivos();

    @Query("SELECT * FROM usuarios")
    List<Usuario> getTodos();

    @Query("SELECT * FROM usuarios WHERE id = :id")
    Usuario getById(long id);

    @Query("SELECT * FROM usuarios WHERE email = :email")
    Usuario getByEmail(String email);

    @Query("DELETE FROM usuarios")
    void deleteAll();
}