package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ventas")
public class Venta {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long fecha;
    public int cantidad;
    public double precioUnitario;
    public double total;
    public String lugar;
    public String categoria;
    public String infoVenta;
    public boolean sincronizado;
    public String nombrePlanta;
    public String duenia;
    public String estadoDinero;

    public static final String DUENIA_ANA = "Ana";
    public static final String DUENIA_CLAU = "Clau";
    public static final String DUENIA_AMBAS = "Ambas";
    public static final String ESTADO_EMPRENDIMIENTO = "Emprendimiento";
    public static final String ESTADO_RETIRADO = "Retirado";
    public static final String ESTADO_DEJADO_OASIS = "Dejado en Oasis";

    public Venta() {}

    public Venta(long fecha, int cantidad, double precioUnitario, String lugar, String categoria, String infoVenta) {
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = cantidad * precioUnitario;
        this.lugar = lugar;
        this.categoria = categoria;
        this.infoVenta = infoVenta;
        this.sincronizado = false;
        this.duenia = DUENIA_AMBAS;
        this.estadoDinero = ESTADO_EMPRENDIMIENTO;
    }

    public void calcularTotal() {
        this.total = this.cantidad * this.precioUnitario;
    }

    public double getMontoDuenia() {
        if (DUENIA_ANA.equals(duenia)) return total;
        if (DUENIA_CLAU.equals(duenia)) return total;
        return total * 0.25;
    }

    public double getMontoEmprendimiento() {
        return total * 0.50;
    }

    public double getMontoWishlist() {
        return total * 0.10;
    }
}