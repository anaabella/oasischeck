package com.oasischeck.ui.clientes;

public class ClienteResumen {
    public String nombre = "";
    public String origen = "";
    public int ventas;
    public int pedidos;
    public int planificadas;
    public int cuentasPendientes;
    public double totalVentas;
    public double totalPedidos;
    public double totalPlanificadas;
    public double porCobrar;
    public double porPagar;
    public long ultimaActividad;

    public double getTotalComprado() {
        return totalVentas + totalPedidos;
    }
}
