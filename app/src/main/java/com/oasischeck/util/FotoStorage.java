package com.oasischeck.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Guarda las fotos dentro del almacenamiento interno de la app para que no
 * dependan de permisos temporales ni de URIs de otras apps, y las serializa
 * en Base64 para incluirse en las copias de seguridad.
 */
public final class FotoStorage {

    private static final String DIRECTORIO = "fotos";
    private static final int MAX_DIMENSION = 1280;
    private static final int CALIDAD_JPEG = 85;

    private FotoStorage() {
    }

    public static String guardarDesdeUri(Context context, Uri uri, String prefijo) {
        if (uri == null) return "";

        if ("file".equals(uri.getScheme())) {
            File actual = new File(uri.getPath());
            if (esInterna(context, actual) && actual.exists()) return uri.toString();
        }

        try {
            Bitmap bitmap = decodificar(context, uri);
            if (bitmap == null) return uri.toString();
            return escribirBitmap(context, bitmap, prefijo);
        } catch (Exception e) {
            return uri.toString();
        }
    }

    public static String leerBase64(Context context, String fotoUri) {
        if (fotoUri == null || fotoUri.isEmpty()) return null;
        try {
            Uri uri = Uri.parse(fotoUri);
            if ("file".equals(uri.getScheme())) {
                File archivo = new File(uri.getPath());
                if (esInterna(context, archivo) && archivo.exists()) {
                    return Base64.encodeToString(leerBytes(archivo), Base64.NO_WRAP);
                }
            }

            Bitmap bitmap = decodificar(context, uri);
            if (bitmap == null) return null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, out);
            bitmap.recycle();
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    public static String restaurarDesdeBase64(Context context, String base64, String prefijo) {
        if (base64 == null || base64.isEmpty()) return "";
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            File dir = directorio(context);
            File destino = new File(dir, nombreArchivo(prefijo));
            try (FileOutputStream out = new FileOutputStream(destino)) {
                out.write(bytes);
            }
            return Uri.fromFile(destino).toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static Bitmap decodificar(Context context, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

        int sample = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sample > MAX_DIMENSION * 2) {
            sample *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            bitmap = BitmapFactory.decodeStream(in, null, options);
        }
        if (bitmap == null) return null;

        int ancho = bitmap.getWidth();
        int alto = bitmap.getHeight();
        int mayor = Math.max(ancho, alto);
        if (mayor > MAX_DIMENSION) {
            float escala = MAX_DIMENSION / (float) mayor;
            Bitmap reducido = Bitmap.createScaledBitmap(bitmap,
                    Math.round(ancho * escala), Math.round(alto * escala), true);
            if (reducido != bitmap) bitmap.recycle();
            bitmap = reducido;
        }
        return bitmap;
    }

    private static String escribirBitmap(Context context, Bitmap bitmap, String prefijo) throws IOException {
        File destino = new File(directorio(context), nombreArchivo(prefijo));
        try (FileOutputStream out = new FileOutputStream(destino)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, out);
        } finally {
            bitmap.recycle();
        }
        return Uri.fromFile(destino).toString();
    }

    private static byte[] leerBytes(File archivo) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = new java.io.FileInputStream(archivo)) {
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = in.read(buffer)) != -1) {
                out.write(buffer, 0, leidos);
            }
        }
        return out.toByteArray();
    }

    private static String nombreArchivo(String prefijo) {
        return prefijo + "_" + System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
    }

    private static File directorio(Context context) {
        File dir = new File(context.getFilesDir(), DIRECTORIO);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static boolean esInterna(Context context, File archivo) {
        try {
            String base = directorio(context).getCanonicalPath();
            return archivo.getCanonicalPath().startsWith(base);
        } catch (IOException e) {
            return false;
        }
    }
}
