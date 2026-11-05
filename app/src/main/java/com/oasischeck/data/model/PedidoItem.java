package com.oasischeck.data.model;

public class PedidoItem {
    public String descripcion;
    public String fotoUri;

    public PedidoItem() {
        this("", "");
    }

    public PedidoItem(String descripcion, String fotoUri) {
        this.descripcion = descripcion != null ? descripcion : "";
        this.fotoUri = fotoUri != null ? fotoUri : "";
    }
}
