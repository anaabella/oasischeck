package com.oasischeck.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.oasischeck.data.model.Categoria;
import com.oasischeck.data.model.ClienteInfo;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Gasto;
import com.oasischeck.data.model.Intercambio;
import com.oasischeck.data.model.Lugar;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.model.Sucursal;
import com.oasischeck.data.model.SyncState;
import com.oasischeck.data.model.Usuario;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.model.WishPlanta;

@Database(entities = {
    Venta.class, PlannedVenta.class, Pedido.class, WishPlanta.class, Gasto.class,
    Intercambio.class, Cuenta.class, SyncState.class, ClienteInfo.class,
    Categoria.class, Lugar.class, Usuario.class, Sucursal.class
}, version = 11, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract VentaDao ventaDao();
    public abstract PlannedVentaDao plannedVentaDao();
    public abstract PedidoDao pedidoDao();
    public abstract WishPlantaDao wishPlantaDao();
    public abstract GastoDao gastoDao();
    public abstract IntercambioDao intercambioDao();
    public abstract CuentaDao cuentaDao();
    public abstract ClienteInfoDao clienteInfoDao();
    public abstract SyncStateDao syncStateDao();
    public abstract CategoriaDao categoriaDao();
    public abstract LugarDao lugarDao();
    public abstract UsuarioDao usuarioDao();
    public abstract SucursalDao sucursalDao();

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `cuentas` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`fecha` INTEGER NOT NULL, " +
                    "`tipo` TEXT, " +
                    "`concepto` TEXT, " +
                    "`monto` REAL NOT NULL, " +
                    "`persona` TEXT, " +
                    "`vencimiento` INTEGER NOT NULL, " +
                    "`pagada` INTEGER NOT NULL, " +
                    "`notas` TEXT, " +
                    "`creadoEn` INTEGER NOT NULL)");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `cuentas` ADD COLUMN `ventaGeneradaId` INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `ventas` ADD COLUMN `actualizadoEn` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `ventas_planificadas` ADD COLUMN `actualizadoEn` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `pedidos` ADD COLUMN `actualizadoEn` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `wishlist` ADD COLUMN `actualizadoEn` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `gastos` ADD COLUMN `actualizadoEn` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `intercambios` ADD COLUMN `actualizadoEn` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `cuentas` ADD COLUMN `actualizadoEn` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE TABLE IF NOT EXISTS `sync_state` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`tipo` TEXT NOT NULL, " +
                    "`localId` INTEGER NOT NULL, " +
                    "`remoteId` TEXT NOT NULL, " +
                    "`actualizadoEn` INTEGER NOT NULL, " +
                    "`sincronizado` INTEGER NOT NULL, " +
                    "`eliminado` INTEGER NOT NULL)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sync_state_tipo_localId` ON `sync_state` (`tipo`, `localId`)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sync_state_tipo_remoteId` ON `sync_state` (`tipo`, `remoteId`)");
        }
    };

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `ventas` ADD COLUMN `rutaFoto` TEXT");
            db.execSQL("ALTER TABLE `ventas` ADD COLUMN `rutaMiniatura` TEXT");
        }
    };

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            // Tabla categorias
            db.execSQL("CREATE TABLE IF NOT EXISTS `categorias` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`nombre` TEXT NOT NULL, " +
                    "`orden` INTEGER NOT NULL DEFAULT 0, " +
                    "`activo` INTEGER NOT NULL DEFAULT 1, " +
                    "`creadoEn` INTEGER NOT NULL)");
            // Tabla lugares
            db.execSQL("CREATE TABLE IF NOT EXISTS `lugares` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`nombre` TEXT NOT NULL, " +
                    "`orden` INTEGER NOT NULL DEFAULT 0, " +
                    "`activo` INTEGER NOT NULL DEFAULT 1, " +
                    "`creadoEn` INTEGER NOT NULL)");
            // Tabla usuarios
            db.execSQL("CREATE TABLE IF NOT EXISTS `usuarios` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`nombre` TEXT NOT NULL, " +
                    "`email` TEXT, " +
                    "`rol` TEXT, " +
                    "`activo` INTEGER NOT NULL DEFAULT 1, " +
                    "`creadoEn` INTEGER NOT NULL)");
            // Tabla sucursales
            db.execSQL("CREATE TABLE IF NOT EXISTS `sucursales` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`nombre` TEXT NOT NULL, " +
                    "`direccion` TEXT, " +
                    "`telefono` TEXT, " +
                    "`activa` INTEGER NOT NULL DEFAULT 1, " +
                    "`creadoEn` INTEGER NOT NULL)");
            // Agregar columnas a ventas
            db.execSQL("ALTER TABLE `ventas` ADD COLUMN `usuarioId` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `ventas` ADD COLUMN `sucursalId` INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ventas_usuarioId` ON `ventas` (`usuarioId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ventas_sucursalId` ON `ventas` (`sucursalId`)");

            // Insertar datos por defecto
            long ahora = System.currentTimeMillis();
            db.execSQL("INSERT INTO categorias (nombre, orden, activo, creadoEn) VALUES ('Plantas', 1, 1, " + ahora + ")");
            db.execSQL("INSERT INTO categorias (nombre, orden, activo, creadoEn) VALUES ('Stickers', 2, 1, " + ahora + ")");
            db.execSQL("INSERT INTO categorias (nombre, orden, activo, creadoEn) VALUES ('Semillas', 3, 1, " + ahora + ")");
            db.execSQL("INSERT INTO categorias (nombre, orden, activo, creadoEn) VALUES ('Inversion', 4, 1, " + ahora + ")");

            db.execSQL("INSERT INTO lugares (nombre, orden, activo, creadoEn) VALUES ('Feria', 1, 1, " + ahora + ")");
            db.execSQL("INSERT INTO lugares (nombre, orden, activo, creadoEn) VALUES ('Vivero', 2, 1, " + ahora + ")");
            db.execSQL("INSERT INTO lugares (nombre, orden, activo, creadoEn) VALUES ('Quiniela', 3, 1, " + ahora + ")");
            db.execSQL("INSERT INTO lugares (nombre, orden, activo, creadoEn) VALUES ('Libreria', 4, 1, " + ahora + ")");
            db.execSQL("INSERT INTO lugares (nombre, orden, activo, creadoEn) VALUES ('Entrega', 5, 1, " + ahora + ")");
            db.execSQL("INSERT INTO lugares (nombre, orden, activo, creadoEn) VALUES ('Inversion', 6, 1, " + ahora + ")");

            db.execSQL("INSERT INTO usuarios (nombre, email, rol, activo, creadoEn) VALUES ('Administrador', 'admin@oasischeck.com', 'admin', 1, " + ahora + ")");
            db.execSQL("INSERT INTO sucursales (nombre, direccion, telefono, activa, creadoEn) VALUES ('Principal', '', '', 1, " + ahora + ")");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "oasischeck_db"
                    ).fallbackToDestructiveMigration()
                     .build();
                }
            }
        }
        return INSTANCE;
    }
}