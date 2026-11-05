package com.oasischeck.ui.util;

import android.content.Context;

import com.oasischeck.data.model.Venta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelExporter {

    /**
     * Exporta ventas a un archivo CSV que se puede abrir directamente en Excel.
     * El archivo tiene extensión .csv pero se abre correctamente en Excel.
     */
    public static File exportarVentasAExcel(Context context, List<Venta> ventas, String nombreArchivo) throws IOException {
        File file = new File(context.getExternalFilesDir(null), nombreArchivo + ".csv");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        try (FileWriter writer = new FileWriter(file)) {
            // Encabezados (BOM para que Excel reconozca UTF-8 correctamente)
            writer.write("\uFEFF");
            writer.append("Fecha,Cantidad,Precio Unitario,Total,Lugar,Categoría,Info,Planta,Dueña,Estado,Usuario ID,Sucursal ID\n");

            for (Venta v : ventas) {
                writer.append(sdf.format(new Date(v.fecha))).append(",");
                writer.append(String.valueOf(v.cantidad)).append(",");
                writer.append(String.format(Locale.US, "%.2f", v.precioUnitario)).append(",");
                writer.append(String.format(Locale.US, "%.2f", v.total)).append(",");
                writer.append(csvEscape(v.lugar != null ? v.lugar : "")).append(",");
                writer.append(csvEscape(v.categoria != null ? v.categoria : "")).append(",");
                writer.append(csvEscape(v.infoVenta != null ? v.infoVenta : "")).append(",");
                writer.append(csvEscape(v.nombrePlanta != null ? v.nombrePlanta : "")).append(",");
                writer.append(csvEscape(v.duenia != null ? v.duenia : "")).append(",");
                writer.append(csvEscape(v.estadoDinero != null ? v.estadoDinero : "")).append(",");
                writer.append(String.valueOf(v.usuarioId)).append(",");
                writer.append(String.valueOf(v.sucursalId)).append("\n");
            }
        }

        return file;
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}