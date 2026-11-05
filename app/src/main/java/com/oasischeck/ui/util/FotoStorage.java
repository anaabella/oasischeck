package com.oasischeck.ui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FotoStorage {

    private static final String CARPETA_FOTOS = "oasischeck_fotos";

    public static String guardarImagen(Context context, Bitmap bitmap, String nombreArchivo) {
        try {
            File carpeta = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), CARPETA_FOTOS);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }
            File archivo = new File(carpeta, nombreArchivo + ".jpg");
            FileOutputStream fos = new FileOutputStream(archivo);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
            return archivo.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap cargarImagen(Context context, String ruta) {
        if (ruta == null || ruta.isEmpty()) return null;
        File archivo = new File(ruta);
        if (!archivo.exists()) return null;
        return BitmapFactory.decodeFile(ruta);
    }

    public static boolean eliminarImagen(String ruta) {
        if (ruta == null || ruta.isEmpty()) return false;
        File archivo = new File(ruta);
        return archivo.exists() && archivo.delete();
    }
}