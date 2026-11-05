package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sucursales")
public class Sucursal {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nombre;
    public String direccion;
    public String telefono;
    public boolean activa;
    public long creadoEn;

    public Sucursal() {}
}