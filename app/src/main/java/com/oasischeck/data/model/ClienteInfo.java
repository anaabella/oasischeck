package com.oasischeck.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clientes")
public class ClienteInfo {

    public static final String ORIGEN_WHATSAPP = "WhatsApp";
    public static final String ORIGEN_FACEBOOK = "Facebook";

    @PrimaryKey
    @NonNull
    public String clave = "";

    public String nombre;
    public String origen;
    public String whatsapp;
    public String facebook;
    public long actualizadoEn;

    public String getOrigenMostrar() {
        return origen == null || origen.trim().isEmpty() ? "Sin definir" : origen;
    }

    public boolean tieneWhatsApp() {
        return whatsapp != null && !whatsapp.trim().isEmpty();
    }

    public boolean tieneFacebook() {
        return facebook != null && !facebook.trim().isEmpty();
    }

    public double getSaldoPendiente() {
        return 0;
    }
}
