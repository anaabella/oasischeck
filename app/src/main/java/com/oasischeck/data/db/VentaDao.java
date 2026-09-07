package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.Venta;

import java.util.List;

@Dao
public interface VentaDao {
    @Insert
    long insert(Venta venta);

    @Update
    void update(Venta venta);

    @Delete
    void delete(Venta venta);

    @Query("SELECT * FROM ventas ORDER BY fecha DESC, id DESC")
    List<Venta> getAll();

    @Query("SELECT * FROM ventas WHERE id = :id")
    Venta getById(long id);

    @Query("SELECT * FROM ventas WHERE sincronizado = 0")
    List<Venta> getNoSincronizadas();

    @Query("SELECT * FROM ventas WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha DESC")
    List<Venta> getByFechaRango(long fechaInicio, long fechaFin);

    @Query("SELECT * FROM ventas WHERE lugar = :lugar ORDER BY fecha DESC")
    List<Venta> getByLugar(String lugar);

    @Query("SELECT * FROM ventas WHERE categoria = :categoria ORDER BY fecha DESC")
    List<Venta> getByCategoria(String categoria);

    @Query("SELECT * FROM ventas WHERE nombrePlanta LIKE :q OR infoVenta LIKE :q ORDER BY fecha DESC")
    List<Venta> search(String q);

    @Query("SELECT SUM(total) FROM ventas WHERE categoria != 'Inversion'")
    Double getTotalVentas();

    @Query("SELECT SUM(total) FROM ventas WHERE categoria = 'Inversion'")
    Double getTotalInversiones();

    @Query("SELECT SUM(total) FROM ventas WHERE lugar = :lugar AND categoria != 'Inversion'")
    Double getTotalPorLugar(String lugar);

    @Query("SELECT SUM(total) FROM ventas WHERE categoria = :cat AND categoria != 'Inversion'")
    Double getTotalPorCategoria(String cat);

    @Query("SELECT SUM(cantidad) FROM ventas WHERE categoria != 'Inversion'")
    Integer getTotalUnidadesVendidas();

    @Query("UPDATE ventas SET sincronizado = 1 WHERE id = :id")
    void marcarSincronizado(long id);

    @Query("DELETE FROM ventas")
    void deleteAll();

    @Query("SELECT SUM(total) FROM ventas WHERE fecha BETWEEN :inicio AND :fin AND categoria != 'Inversion'")
    Double getTotalRango(long inicio, long fin);

    @Query("SELECT SUM(cantidad) FROM ventas WHERE fecha BETWEEN :inicio AND :fin AND categoria != 'Inversion'")
    Integer getUnidadesRango(long inicio, long fin);

    @Query("SELECT SUM(total) FROM ventas WHERE duenia = :duenia AND categoria != 'Inversion'")
    Double getTotalPorDuenia(String duenia);

    @Query("SELECT SUM(total) FROM ventas WHERE estadoDinero = :estado AND categoria != 'Inversion'")
    Double getTotalPorEstadoDinero(String estado);

    @Query("SELECT nombrePlanta, SUM(cantidad) as cant, SUM(total) as tot FROM ventas WHERE categoria != 'Inversion' AND nombrePlanta IS NOT NULL AND nombrePlanta != '' GROUP BY nombrePlanta ORDER BY cant DESC LIMIT :limit")
    List<PlantaStats> getTopPlantas(int limit);

    @Query("SELECT nombrePlanta, SUM(cantidad) as cant, SUM(total) as tot FROM ventas WHERE categoria != 'Inversion' AND nombrePlanta IS NOT NULL AND nombrePlanta != '' AND fecha BETWEEN :inicio AND :fin GROUP BY nombrePlanta ORDER BY cant DESC")
    List<PlantaStats> getTopPlantasRango(long inicio, long fin);

    class PlantaStats {
        public String nombrePlanta;
        public int cant;
        public double tot;
    }
}