package com.oasischeck.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.oasischeck.data.model.SyncState;

import java.util.List;

@Dao
public interface SyncStateDao {
    @Insert
    long insert(SyncState state);

    @Update
    void update(SyncState state);

    @Delete
    void delete(SyncState state);

    @Query("SELECT * FROM sync_state WHERE tipo = :tipo AND sincronizado = 0 ORDER BY actualizadoEn ASC")
    List<SyncState> getPendientes(String tipo);

    @Query("SELECT * FROM sync_state WHERE sincronizado = 0 ORDER BY actualizadoEn ASC")
    List<SyncState> getPendientes();

    @Query("SELECT * FROM sync_state WHERE tipo = :tipo AND localId = :localId LIMIT 1")
    SyncState getByLocal(String tipo, long localId);

    @Query("SELECT * FROM sync_state WHERE tipo = :tipo AND remoteId = :remoteId LIMIT 1")
    SyncState getByRemote(String tipo, String remoteId);

    @Query("SELECT * FROM sync_state WHERE tipo = :tipo")
    List<SyncState> getByTipo(String tipo);

    @Query("DELETE FROM sync_state")
    void deleteAll();
}
