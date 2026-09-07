package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ventas_planificadas")
public class PlannedVenta {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long fecha;           // Fecha planificada
    public String cliente;       // Nombre del cliente
    public String infoVenta;     // Qué le vas a vender
    public double totalEstimado; // Total estimado
    public int estado;           // 0=Pendiente, 1=Vendida, 2=Cancelada
    public long ventaRealId;     // ID de la venta real si se convirtió (-1 si no)
    public String notas;         // Notas extra
    public long creadoEn;        // Timestamp de creación

    public PlannedVenta() {}

    public static final int ESTADO_PENDIENTE = 0;
    public static final int ESTADO_VENDIDA = 1;
    public static final int ESTADO_CANCELADA = 2;
}