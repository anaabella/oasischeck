package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "gastos")
public class Gasto {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long fecha;
    public String concepto;
    public double monto;
    public String tipo;
    public boolean recurrente;
    public String notas;

    public static final String TIPO_VIAJE = "Viaje";
    public static final String TIPO_FIJO = "Fijo";
    public static final String TIPO_INSUMO = "Insumo";
    public static final String TIPO_OTRO = "Otro";

    public Gasto() {}
}