package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pedidos")
public class Pedido {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long fecha;           // Fecha del pedido
    public String cliente;       // Nombre del cliente
    public String productos;     // Qué pidió
    public double total;         // Total del pedido
    public int estado;           // 0=Pendiente, 1=Preparado, 2=Entregado
    public String notas;         // Notas extra
    public long creadoEn;
    public long preparadoEn;     // Timestamp cuando se marcó preparado
    public long entregadoEn;     // Timestamp cuando se marcó entregado

    public Pedido() {}

    public static final int ESTADO_PENDIENTE = 0;
    public static final int ESTADO_PREPARADO = 1;
    public static final int ESTADO_ENTREGADO = 2;
}