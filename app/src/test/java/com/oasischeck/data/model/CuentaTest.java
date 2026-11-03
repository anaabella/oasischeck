package com.oasischeck.data.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CuentaTest {

    @Test
    public void vencidaCuandoLaFechaYaPasoYPendiente() {
        Cuenta c = new Cuenta();
        c.pagada = false;
        c.vencimiento = System.currentTimeMillis() - 60_000L;
        assertTrue(c.estaVencida());
    }

    @Test
    public void noVencidaSiYaEstaPagada() {
        Cuenta c = new Cuenta();
        c.pagada = true;
        c.vencimiento = System.currentTimeMillis() - 60_000L;
        assertFalse(c.estaVencida());
    }

    @Test
    public void noVencidaSiElVencimientoEsFuturo() {
        Cuenta c = new Cuenta();
        c.pagada = false;
        c.vencimiento = System.currentTimeMillis() + 86_400_000L;
        assertFalse(c.estaVencida());
    }

    @Test
    public void sinVencimientoNoEstaVencida() {
        Cuenta c = new Cuenta();
        c.pagada = false;
        c.vencimiento = 0;
        assertFalse(c.estaVencida());
    }
}
