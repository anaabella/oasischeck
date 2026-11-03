package com.oasischeck.data.db;

import com.oasischeck.data.model.SyncState;

import java.util.UUID;

/**
 * Marca registros locales como pendientes de sincronizar.
 * Debe llamarse dentro de los executors de los repositorios (nunca en el hilo principal).
 */
public final class SyncTracker {
    public static final String TIPO_VENTA = "venta";
    public static final String TIPO_PLANIFICADA = "planificada";
    public static final String TIPO_PEDIDO = "pedido";
    public static final String TIPO_WISH = "wish";
    public static final String TIPO_GASTO = "gasto";
    public static final String TIPO_INTERCAMBIO = "intercambio";
    public static final String TIPO_CUENTA = "cuenta";

    private SyncTracker() {}

    public static void registrar(SyncStateDao dao, String tipo, long localId, long actualizadoEn) {
        SyncState state = dao.getByLocal(tipo, localId);
        if (state == null) {
            state = new SyncState();
            state.tipo = tipo;
            state.localId = localId;
            state.remoteId = UUID.randomUUID().toString();
            state.actualizadoEn = actualizadoEn;
            state.sincronizado = false;
            state.eliminado = false;
            dao.insert(state);
        } else {
            state.actualizadoEn = actualizadoEn;
            state.sincronizado = false;
            state.eliminado = false;
            dao.update(state);
        }
    }

    public static void marcarEliminado(SyncStateDao dao, String tipo, long localId) {
        SyncState state = dao.getByLocal(tipo, localId);
        if (state == null) return;
        state.eliminado = true;
        state.sincronizado = false;
        state.actualizadoEn = System.currentTimeMillis();
        dao.update(state);
    }
}
