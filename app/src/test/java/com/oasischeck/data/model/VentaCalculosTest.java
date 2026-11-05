package com.oasischeck.data.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class VentaCalculosTest {

    @Test
    public void testCalcularTotal() {
        Venta venta = new Venta(System.currentTimeMillis(), 5, 1000, "Feria", "Plantas", "Test");
        assertEquals(5000, venta.total, 0.001);
    }

    @Test
    public void testGetMontoDueniaAna() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        venta.duenia = Venta.DUENIA_ANA;
        assertEquals(1000, venta.getMontoDuenia(), 0.001);
    }

    @Test
    public void testGetMontoDueniaClau() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        venta.duenia = Venta.DUENIA_CLAU;
        assertEquals(1000, venta.getMontoDuenia(), 0.001);
    }

    @Test
    public void testGetMontoDueniaAmbas() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        venta.duenia = Venta.DUENIA_AMBAS;
        assertEquals(250, venta.getMontoDuenia(), 0.001);
    }

    @Test
    public void testGetMontoEmprendimiento() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        assertEquals(500, venta.getMontoEmprendimiento(), 0.001);
    }

    @Test
    public void testGetMontoWishlist() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        assertEquals(100, venta.getMontoWishlist(), 0.001);
    }

    @Test
    public void testCantidadCeroTotalCero() {
        Venta venta = new Venta(System.currentTimeMillis(), 0, 1000, "Feria", "Plantas", "Test");
        assertEquals(0, venta.total, 0.001);
    }

    @Test
    public void testTieneFotoSinRuta() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        assertFalse(venta.tieneFoto());
    }

    @Test
    public void testTieneFotoConRuta() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        venta.rutaFoto = "/storage/emulated/0/foto.jpg";
        assertTrue(venta.tieneFoto());
    }

    @Test
    public void testTieneFotoRutaVacia() {
        Venta venta = new Venta(System.currentTimeMillis(), 1, 1000, "Feria", "Plantas", "Test");
        venta.rutaFoto = "";
        assertFalse(venta.tieneFoto());
    }
}