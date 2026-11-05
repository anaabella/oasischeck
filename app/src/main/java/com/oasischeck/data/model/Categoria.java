package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nombre;
    public int orden;
    public boolean activo;
    public long creadoEn;

    public Categoria() {}
}