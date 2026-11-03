package com.oasischeck.data.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Metadatos de sincronización de una entidad local con Firestore.
 * El id remoto se guarda acá para no ensuciar las tablas de negocio.
 */
@Entity(tableName = "sync_state",
        indices = {
                @Index(value = {"tipo", "localId"}, unique = true),
                @Index(value = {"tipo", "remoteId"}, unique = true)
        })
public class SyncState {
    @PrimaryKey(autoGenerate = true)
    public long id;

    /** venta, gasto, pedido, planificada, wish, intercambio, cuenta */
    public String tipo;

    public long localId;
    public String remoteId;
    public long actualizadoEn;
    public boolean sincronizado;
    public boolean eliminado;

    public SyncState() {}
}
