package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "intercambios")
public class Intercambio {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long fecha;
    public String plantaEntregada;
    public String plantaRecibida;
    public String persona;
    public String notas;
    public boolean sincronizado;

    public Intercambio() {}

    public Intercambio(long fecha, String plantaEntregada, String plantaRecibida, String persona, String notas) {
        this.fecha = fecha;
        this.plantaEntregada = plantaEntregada;
        this.plantaRecibida = plantaRecibida;
        this.persona = persona;
        this.notas = notas;
        this.sincronizado = false;
    }
}
