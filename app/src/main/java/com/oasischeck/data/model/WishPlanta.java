package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wishlist")
public class WishPlanta {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String nombre;
    public double precioVisto;
    public String lugarVisto;
    public boolean comprada;
    public double precioComprada;
    public long fechaComprada;
    public String notas;
    public String fotoUri;

    public WishPlanta() {}
}