package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.oasischeck.data.model.ClienteInfo;

import java.util.List;

@Dao
public interface ClienteInfoDao {

    @Query("SELECT * FROM clientes")
    List<ClienteInfo> getAll();

    @Query("SELECT * FROM clientes WHERE clave = :clave LIMIT 1")
    ClienteInfo getByClave(String clave);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ClienteInfo info);

    @Query("DELETE FROM clientes WHERE clave = :clave")
    void deleteByClave(String clave);

    @Query("DELETE FROM clientes")
    void deleteAll();
}
