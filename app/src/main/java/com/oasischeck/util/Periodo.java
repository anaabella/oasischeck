package com.oasischeck.util;

import java.util.Calendar;

/**
 * Rango de fechas usado por los reportes. La comparación con el período
 * anterior usa un rango de igual duración inmediatamente anterior.
 */
public final class Periodo {

    public final long inicio;
    public final long fin;
    public final String etiqueta;
    public final boolean todo;

    public Periodo(long inicio, long fin, String etiqueta, boolean todo) {
        this.inicio = inicio;
        this.fin = fin;
        this.etiqueta = etiqueta;
        this.todo = todo;
    }

    public static Periodo todo() {
        return new Periodo(Long.MIN_VALUE, Long.MAX_VALUE, "Todo", true);
    }

    public static Periodo hoy(Calendar ref) {
        Calendar c = (Calendar) ref.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long inicio = c.getTimeInMillis();

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return new Periodo(inicio, c.getTimeInMillis(), "Hoy", false);
    }

    public static Periodo semana(Calendar ref) {
        Calendar c = (Calendar) ref.clone();
        int diaSemana = c.get(Calendar.DAY_OF_WEEK);
        int diasDesdeInicio = (diaSemana - c.getFirstDayOfWeek() + 7) % 7;
        c.add(Calendar.DAY_OF_MONTH, -diasDesdeInicio);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long inicio = c.getTimeInMillis();

        Calendar fin = (Calendar) c.clone();
        fin.add(Calendar.DAY_OF_MONTH, 6);
        fin.set(Calendar.HOUR_OF_DAY, 23);
        fin.set(Calendar.MINUTE, 59);
        fin.set(Calendar.SECOND, 59);
        fin.set(Calendar.MILLISECOND, 999);
        return new Periodo(inicio, fin.getTimeInMillis(), "Esta semana", false);
    }

    public static Periodo mes(Calendar ref) {
        Calendar c = (Calendar) ref.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long inicio = c.getTimeInMillis();

        Calendar fin = (Calendar) c.clone();
        fin.add(Calendar.MONTH, 1);
        fin.add(Calendar.MILLISECOND, -1);
        return new Periodo(inicio, fin.getTimeInMillis(), "Este mes", false);
    }

    public static Periodo personalizado(long inicio, long fin) {
        return new Periodo(inicio, fin, "Personalizado", false);
    }

    public Periodo anterior() {
        if (todo || fin < inicio) return null;
        long duracion = fin - inicio + 1;
        return new Periodo(inicio - duracion, inicio - 1, "Período anterior", false);
    }
}
