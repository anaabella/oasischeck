package com.oasischeck.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.oasischeck.data.model.Gasto;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.model.WishPlanta;

@Database(entities = {Venta.class, PlannedVenta.class, Pedido.class, WishPlanta.class, Gasto.class}, version = 5, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract VentaDao ventaDao();
    public abstract PlannedVentaDao plannedVentaDao();
    public abstract PedidoDao pedidoDao();
    public abstract WishPlantaDao wishPlantaDao();
    public abstract GastoDao gastoDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "oasischeck_db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}