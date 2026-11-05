package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nombre;
    public String email;
    public String rol; // "admin", "vendedor", "contador"
    public boolean activo;
    public long creadoEn;

    public Usuario() {}
}