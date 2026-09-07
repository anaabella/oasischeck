package com.oasischeck.ui.wishlist;

import android.content.Intent;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.WishPlanta;
import com.oasischeck.data.repository.WishPlantaRepository;

import java.io.File;
import java.io.IOException;

public class AgregarWishActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etPrecio, etLugar, etNotas, etPrecioComprada;
    private ImageView ivFotoPreview;
    private WishPlantaRepository repository;
    private WishPlanta existente = null;
    private String fotoUri;
    private Uri photoUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        fotoUri = uri.toString();
                        showPreview(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && photoUri != null) {
                    fotoUri = photoUri.toString();
                    showPreview(photoUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_wish);

        repository = new WishPlantaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNombre = findViewById(R.id.et_nombre);
        etPrecio = findViewById(R.id.et_precio);
        etLugar = findViewById(R.id.et_lugar);
        etNotas = findViewById(R.id.et_notas);
        etPrecioComprada = findViewById(R.id.et_precio_comprada);
        ivFotoPreview = findViewById(R.id.iv_foto_preview);

        findViewById(R.id.btn_foto).setOnClickListener(v -> showPhotoDialog());

        long id = getIntent().getLongExtra("wish_id", -1);
        if (id != -1) {
            toolbar.setTitle("Editar Wish");
            repository.getAll(lista -> {
                for (WishPlanta w : lista) {
                    if (w.id == id) {
                        existente = w;
                        runOnUiThread(() -> cargarDatos(w));
                        break;
                    }
                }
            });
        }

        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());
    }

    private void cargarDatos(WishPlanta w) {
        etNombre.setText(w.nombre);
        etPrecio.setText(w.precioVisto > 0 ? String.valueOf(w.precioVisto) : "");
        etLugar.setText(w.lugarVisto);
        etNotas.setText(w.notas);
        if (w.comprada && w.precioComprada > 0) {
            etPrecioComprada.setText(String.valueOf(w.precioComprada));
        }
        if (w.fotoUri != null && !w.fotoUri.isEmpty()) {
            fotoUri = w.fotoUri;
            showPreview(Uri.parse(w.fotoUri));
        }
    }

    private void guardar() {
        if (etNombre.getText() == null || etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setError("Requerido");
            return;
        }

        WishPlanta w = existente != null ? existente : new WishPlanta();
        w.nombre = etNombre.getText().toString().trim();
        w.precioVisto = 0;
        if (etPrecio.getText() != null && !etPrecio.getText().toString().isEmpty()) {
            try { w.precioVisto = Double.parseDouble(etPrecio.getText().toString()); } catch (NumberFormatException ignored) {}
        }
        w.lugarVisto = etLugar.getText() != null ? etLugar.getText().toString().trim() : "";
        w.notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";
        w.fotoUri = fotoUri != null ? fotoUri : "";

        String precioCompradaStr = etPrecioComprada.getText() != null ? etPrecioComprada.getText().toString() : "";
        if (!precioCompradaStr.isEmpty()) {
            w.comprada = true;
            try { w.precioComprada = Double.parseDouble(precioCompradaStr); } catch (NumberFormatException ignored) { w.precioComprada = 0; }
            w.fechaComprada = System.currentTimeMillis();
        }

        if (existente != null) {
            repository.update(w, () -> runOnUiThread(() -> {
                Toast.makeText(this, "Actualizada", Toast.LENGTH_SHORT).show();
                finish();
            }));
        } else {
            repository.insert(w, id -> runOnUiThread(() -> {
                Toast.makeText(this, "Agregada a wishlist", Toast.LENGTH_SHORT).show();
                finish();
            }));
        }
    }

    private void showPhotoDialog() {
        String[] options = {"Galería", "Cámara"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Agregar foto")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickImageLauncher.launch(intent);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "wish_" + System.currentTimeMillis() + ".jpg");
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        takePhotoLauncher.launch(intent);
                    }
                })
                .show();
    }

    private void showPreview(Uri uri) {
        ivFotoPreview.setVisibility(android.view.View.VISIBLE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                ivFotoPreview.setImageBitmap(ImageDecoder.decodeBitmap(source));
            } else {
                ivFotoPreview.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
            }
        } catch (IOException e) {
            ivFotoPreview.setVisibility(android.view.View.GONE);
        }
    }
}