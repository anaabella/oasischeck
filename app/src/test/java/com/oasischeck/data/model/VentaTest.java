package com.oasischeck.data.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VentaTest {

    @Test
    public void constructorCalculaElTotal() {
        Venta v = new Venta(0L, 3, 250.0, "Entrega", "Plantas", "");
        assertEquals(750.0, v.total, 0.001);
    }

    @Test
    public void calcularTotalUsaCantidadYPrecio() {
        Venta v = new Venta();
        v.cantidad = 4;
        v.precioUnitario = 100.0;
        v.calcularTotal();
        assertEquals(400.0, v.total, 0.001);
    }

    @Test
    public void montoDueniaCompletoCuandoEsDeUnaSolaDuenia() {
        Venta v = new Venta(0L, 1, 1000.0, "Entrega", "Plantas", "");
        v.duenia = Venta.DUENIA_ANA;
        assertEquals(1000.0, v.getMontoDuenia(), 0.001);

        v.duenia = Venta.DUENIA_CLAU;
        assertEquals(1000.0, v.getMontoDuenia(), 0.001);
    }

    @Test
    public void montoDueniaEsLaCuartaParteCuandoEsDeAmbas() {
        Venta v = new Venta(0L, 1, 1000.0, "Entrega", "Plantas", "");
        v.duenia = Venta.DUENIA_AMBAS;
        assertEquals(250.0, v.getMontoDuenia(), 0.001);
    }

    @Test
    public void montosDeEmprendimientoYWishlist() {
        Venta v = new Venta(0L, 1, 1000.0, "Entrega", "Plantas", "");
        assertEquals(500.0, v.getMontoEmprendimiento(), 0.001);
        assertEquals(100.0, v.getMontoWishlist(), 0.001);
    }
}
