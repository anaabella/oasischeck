package com.oasischeck.data.repository;

import android.app.Application;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.SucursalDao;
import com.oasischeck.data.db.UsuarioDao;
import com.oasischeck.data.model.Sucursal;
import com.oasischeck.data.model.Usuario;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioSucursalRepository {
    private final UsuarioDao usuarioDao;
    private final SucursalDao sucursalDao;
    private final ExecutorService executor;

    public UsuarioSucursalRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        usuarioDao = db.usuarioDao();
        sucursalDao = db.sucursalDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> { void onResult(T result); }

    // Usuarios
    public void insertUsuario(Usuario usuario, Callback<Long> callback) {
        executor.execute(() -> {
            long id = usuarioDao.insert(usuario);
            if (callback != null) callback.onResult(id);
        });
    }

    public void getUsuariosActivos(Callback<List<Usuario>> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(usuarioDao.getActivos());
        });
    }

    public void getUsuarioById(long id, Callback<Usuario> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(usuarioDao.getById(id));
        });
    }

    // Sucursales
    public void insertSucursal(Sucursal sucursal, Callback<Long> callback) {
        executor.execute(() -> {
            long id = sucursalDao.insert(sucursal);
            if (callback != null) callback.onResult(id);
        });
    }

    public void getSucursalesActivas(Callback<List<Sucursal>> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(sucursalDao.getActivas());
        });
    }

    public void getSucursalById(long id, Callback<Sucursal> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onResult(sucursalDao.getById(id));
        });
    }
}