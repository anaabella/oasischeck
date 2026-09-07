package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Pedido;

import java.util.List;

@Dao
public interface PedidoDao {
    @Insert
    long insert(Pedido pedido);

    @Update
    void update(Pedido pedido);

    @Delete
    void delete(Pedido pedido);

    @Query("SELECT * FROM pedidos ORDER BY fecha DESC, creadoEn DESC")
    List<Pedido> getAll();

    @Query("SELECT * FROM pedidos WHERE id = :id")
    Pedido getById(long id);

    @Query("SELECT * FROM pedidos WHERE estado = :estado ORDER BY fecha ASC")
    List<Pedido> getByEstado(int estado);

    @Query("SELECT * FROM pedidos WHERE cliente LIKE :cliente ORDER BY fecha DESC")
    List<Pedido> searchByCliente(String cliente);

    @Query("SELECT * FROM pedidos WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    List<Pedido> getByFechaRango(long inicio, long fin);

    @Query("DELETE FROM pedidos")
    void deleteAll();
}