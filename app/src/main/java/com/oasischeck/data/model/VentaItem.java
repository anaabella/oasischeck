package com.oasischeck.data.model;

public class VentaItem {
    public String nombre;
    public int cantidad;
    public double precioUnitario;
    public double costoUnitario;

    public VentaItem() {
        this("", 1, 0, 0);
    }

    public VentaItem(String nombre, int cantidad, double precioUnitario, double costoUnitario) {
        this.nombre = nombre != null ? nombre : "";
        this.cantidad = Math.max(0, cantidad);
        this.precioUnitario = Math.max(0, precioUnitario);
        this.costoUnitario = Math.max(0, costoUnitario);
    }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    public double getCostoTotal() {
        return cantidad * costoUnitario;
    }
}
