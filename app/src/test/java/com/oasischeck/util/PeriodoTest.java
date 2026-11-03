package com.oasischeck.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

public class PeriodoTest {

    private Calendar referencia() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(2024, Calendar.MARCH, 13, 15, 30, 45);
        c.set(Calendar.MILLISECOND, 500);
        return c;
    }

    private Calendar esperado(int year, int month, int day, int hour, int minute, int second, int millis) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.set(year, month, day, hour, minute, second);
        c.set(Calendar.MILLISECOND, millis);
        return c;
    }

    @Test
    public void hoyCubreElDiaCompleto() {
        Periodo p = Periodo.hoy(referencia());
        assertEquals(esperado(2024, Calendar.MARCH, 13, 0, 0, 0, 0).getTimeInMillis(), p.inicio);
        assertEquals(esperado(2024, Calendar.MARCH, 13, 23, 59, 59, 999).getTimeInMillis(), p.fin);
        assertEquals("Hoy", p.etiqueta);
    }

    @Test
    public void semanaEmpiezaElLunesYTerminaElDomingo() {
        Periodo p = Periodo.semana(referencia());
        assertEquals(esperado(2024, Calendar.MARCH, 11, 0, 0, 0, 0).getTimeInMillis(), p.inicio);
        assertEquals(esperado(2024, Calendar.MARCH, 17, 23, 59, 59, 999).getTimeInMillis(), p.fin);
    }

    @Test
    public void mesCubreDePrimeroAFinDeMes() {
        Periodo p = Periodo.mes(referencia());
        assertEquals(esperado(2024, Calendar.MARCH, 1, 0, 0, 0, 0).getTimeInMillis(), p.inicio);
        assertEquals(esperado(2024, Calendar.MARCH, 31, 23, 59, 59, 999).getTimeInMillis(), p.fin);
    }

    @Test
    public void periodoAnteriorTieneLaMismaDuracion() {
        Periodo p = Periodo.personalizado(1_000L, 1_999L);
        Periodo anterior = p.anterior();
        assertNotNull(anterior);
        assertEquals(0L, anterior.inicio);
        assertEquals(999L, anterior.fin);
        assertEquals(1_000L, anterior.fin - anterior.inicio + 1);
    }

    @Test
    public void todoNoTienePeriodoAnterior() {
        assertNull(Periodo.todo().anterior());
    }
}
