package com.oasischeck.ui.lista;

import com.oasischeck.data.model.Venta;

public class DisplayItem {
    public static final int TYPE_HEADER_DIA = 0;
    public static final int TYPE_HEADER_MONTO = 1;
    public static final int TYPE_VENTA = 2;

    public int type;
    public String headerDia;
    public String headerMonto;
    public int headerCantidad;
    public Venta venta;

    public static DisplayItem headerDia(String dia, int cantidad) {
        DisplayItem item = new DisplayItem();
        item.type = TYPE_HEADER_DIA;
        item.headerDia = dia;
        item.headerCantidad = cantidad;
        return item;
    }

    public static DisplayItem header(String monto, int cantidad) {
        DisplayItem item = new DisplayItem();
        item.type = TYPE_HEADER_MONTO;
        item.headerMonto = monto;
        item.headerCantidad = cantidad;
        return item;
    }

    public static DisplayItem venta(Venta venta) {
        DisplayItem item = new DisplayItem();
        item.type = TYPE_VENTA;
        item.venta = venta;
        return item;
    }
}