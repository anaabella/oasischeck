package com.oasischeck.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Gasto;
import com.oasischeck.data.model.Intercambio;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.model.SyncState;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.model.WishPlanta;

@Database(entities = {Venta.class, PlannedVenta.class, Pedido.class, WishPlanta.class, Gasto.class, Intercambio.class, Cuenta.class, SyncState.class}, version = 9, exportSchema = false)
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
    public abstract SyncStateDao syncStateDao();

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

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "oasischeck_db"
                    ).addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                     .fallbackToDestructiveMigrationOnDowngrade()
                     .build();
                }
            }
        }
        return INSTANCE;
    }
}