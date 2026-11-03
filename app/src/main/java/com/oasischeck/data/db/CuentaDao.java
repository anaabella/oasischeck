package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Cuenta;

import java.util.List;

@Dao
public interface CuentaDao {
    @Insert
    long insert(Cuenta cuenta);

    @Update
    void update(Cuenta cuenta);

    @Delete
    void delete(Cuenta cuenta);

    @Query("SELECT * FROM cuentas ORDER BY pagada ASC, fecha DESC, id DESC")
    List<Cuenta> getAll();

    @Query("SELECT * FROM cuentas WHERE id = :id")
    Cuenta getById(long id);

    @Query("SELECT * FROM cuentas WHERE tipo = :tipo ORDER BY pagada ASC, fecha DESC")
    List<Cuenta> getByTipo(String tipo);

    @Query("SELECT SUM(monto) FROM cuentas WHERE tipo = :tipo AND pagada = 0")
    Double getTotalPendiente(String tipo);

    @Query("SELECT SUM(monto) FROM cuentas WHERE tipo = :tipo AND pagada = 1")
    Double getTotalPagado(String tipo);

    @Query("SELECT * FROM cuentas WHERE pagada = 0 AND vencimiento > 0 AND vencimiento <= :hasta ORDER BY vencimiento ASC")
    List<Cuenta> getPorVencer(long hasta);

    @Query("DELETE FROM cuentas")
    void deleteAll();
}
