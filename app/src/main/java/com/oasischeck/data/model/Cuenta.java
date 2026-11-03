package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Cuenta por cobrar (dinero que nos deben) o por pagar (dinero que debemos).
 */
@Entity(tableName = "cuentas")
public class Cuenta {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long actualizadoEn;
    public long fecha;
    public String tipo;
    public String concepto;
    public double monto;
    public String persona;
    public long vencimiento;
    public boolean pagada;
    public String notas;
    public long creadoEn;

    /** Id de la venta generada automáticamente al cobrar esta cuenta (0 = ninguna). */
    public long ventaGeneradaId;

    public static final String TIPO_COBRAR = "Cobrar";
    public static final String TIPO_PAGAR = "Pagar";

    public Cuenta() {}

    public boolean estaVencida() {
        return !pagada && vencimiento > 0 && vencimiento < System.currentTimeMillis();
    }
}
