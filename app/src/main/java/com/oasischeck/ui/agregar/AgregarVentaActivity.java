package com.oasischeck.ui.agregar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.repository.VentaRepository;
import com.oasischeck.sheets.GoogleSheetsService;
import com.oasischeck.ui.util.FotoStorage;
import com.oasischeck.ui.util.PermissionHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class AgregarVentaActivity extends AppCompatActivity implements PermissionHelper.PermissionCallback {

    private TextInputEditText etFecha, etCantidad, etPrecio, etInfo;
    private AutoCompleteTextView etNombrePlanta;
    private AutoCompleteTextView spinnerLugar, spinnerCategoria, spinnerDuenia;
    private TextView tvTotal;
    private ImageView ivFotoPreview;
    private VentaRepository repository;
    private long fechaSeleccionada = System.currentTimeMillis();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private String rutaFotoSeleccionada = null;

    // Activity Result Launchers para cámara y galería
    private final ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri photoUri = result.getData().getData();
                    if (photoUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                            ivFotoPreview.setImageBitmap(bitmap);
                            rutaFotoSeleccionada = FotoStorage.guardarImagen(this, bitmap, "venta_" + System.currentTimeMillis());
                            Toast.makeText(this, "Foto guardada", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(this, "Error al cargar foto", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            ivFotoPreview.setImageBitmap(bitmap);
                            rutaFotoSeleccionada = FotoStorage.guardarImagen(this, bitmap, "venta_" + System.currentTimeMillis());
                            Toast.makeText(this, "Foto guardada", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(this, "Error al cargar foto", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_venta);

        repository = new VentaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFecha = findViewById(R.id.et_fecha);
        etCantidad = findViewById(R.id.et_cantidad);
        etPrecio = findViewById(R.id.et_precio);
        etInfo = findViewById(R.id.et_info);
        etNombrePlanta = findViewById(R.id.et_nombre_planta);
        spinnerLugar = findViewById(R.id.spinner_lugar);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        spinnerDuenia = findViewById(R.id.spinner_duenia);
        tvTotal = findViewById(R.id.tv_total);
        ivFotoPreview = findViewById(R.id.iv_foto_preview);
        MaterialButton btnGuardar = findViewById(R.id.btn_guardar);
        MaterialButton btnTomarFoto = findViewById(R.id.btn_tomar_foto);
        MaterialButton btnGaleria = findViewById(R.id.btn_galeria);

        setupSpinners();
        setupDatePicker();
        setupTotalCalculator();

        boolean fromPlanned = getIntent().getBooleanExtra("from_planned", false);
        if (fromPlanned) {
            long prefillFecha = getIntent().getLongExtra("prefill_fecha", System.currentTimeMillis());
            String prefillInfo = getIntent().getStringExtra("prefill_info");
            double prefillTotal = getIntent().getDoubleExtra("prefill_total", 0);
            String prefillCliente = getIntent().getStringExtra("prefill_cliente");
            fechaSeleccionada = prefillFecha;
            etFecha.setText(new java.text.SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new java.util.Date(prefillFecha)));
            String infoCompleta = (prefillCliente != null ? prefillCliente + ": " : "") + (prefillInfo != null ? prefillInfo : "");
            etInfo.setText(infoCompleta);
            etPrecio.setText(String.valueOf(prefillTotal));
            etCantidad.setText("1");
            actualizarTotal();
        } else {
            Calendar cal = Calendar.getInstance();
            etFecha.setText(String.format("%d/%d/%d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
        }

        btnGuardar.setOnClickListener(v -> guardarVenta(getIntent().getLongExtra("planned_id", -1)));

        btnTomarFoto.setOnClickListener(v -> {
            if (PermissionHelper.hasCameraPermission(this)) {
                abrirCamara();
            } else {
                PermissionHelper.requestCameraAndStorage(this, this);
            }
        });

        btnGaleria.setOnClickListener(v -> {
            if (PermissionHelper.hasCameraPermission(this)) {
                abrirGaleria();
            } else {
                PermissionHelper.requestCameraAndStorage(this, this);
            }
        });
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "venta_" + System.currentTimeMillis() + ".jpg");
        android.net.Uri photoUri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        takePhotoLauncher.launch(intent);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onGranted() {
        // Permisos concedidos, permitir al usuario volver a intentar
        Toast.makeText(this, "Permisos concedidos. Intente de nuevo tomar o seleccionar foto", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDenied() {
        Toast.makeText(this, "Permisos denegados. No se podrá agregar fotos", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void setupSpinners() {
        spinnerLugar.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                com.oasischeck.util.AppConstants.LUGARES));
        spinnerCategoria.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                com.oasischeck.util.AppConstants.CATEGORIAS));
        spinnerDuenia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                com.oasischeck.util.AppConstants.DUENIAS));
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar c = Calendar.getInstance();
                c.set(year, month, day, 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                fechaSeleccionada = c.getTimeInMillis();
                etFecha.setText(String.format("%d/%d/%d", day, month + 1, year));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTotalCalculator() {
        android.text.TextWatcher tw = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { actualizarTotal(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etCantidad.addTextChangedListener(tw);
        etPrecio.addTextChangedListener(tw);
    }

    private void actualizarTotal() {
        try {
            int cant = etCantidad.getText() != null ? Integer.parseInt(etCantidad.getText().toString()) : 0;
            double precio = etPrecio.getText() != null ? Double.parseDouble(etPrecio.getText().toString()) : 0;
            tvTotal.setText("Total: " + currencyFormat.format(cant * precio));
        } catch (NumberFormatException e) {
            tvTotal.setText("Total: $0");
        }
    }

    private void guardarVenta(long plannedId) {
        if (etCantidad.getText() == null || etCantidad.getText().toString().isEmpty()) { etCantidad.setError("Requerido"); return; }
        if (etPrecio.getText() == null || etPrecio.getText().toString().isEmpty()) { etPrecio.setError("Requerido"); return; }
        if (spinnerLugar.getText().toString().isEmpty()) { spinnerLugar.setError("Requerido"); return; }
        if (spinnerCategoria.getText().toString().isEmpty()) { spinnerCategoria.setError("Requerido"); return; }

        int cantidad;
        double precio;
        try {
            cantidad = Integer.parseInt(etCantidad.getText().toString());
            precio = Double.parseDouble(etPrecio.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresá un número válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cantidad <= 0) { etCantidad.setError("Debe ser mayor a cero"); return; }
        if (precio < 0) { etPrecio.setError("El precio no puede ser negativo"); return; }
        String lugar = spinnerLugar.getText().toString();
        String categoria = spinnerCategoria.getText().toString();
        String info = etInfo.getText() != null ? etInfo.getText().toString() : "";

        Venta venta = new Venta(fechaSeleccionada, cantidad, precio, lugar, categoria, info);
        venta.nombrePlanta = etNombrePlanta.getText() != null ? etNombrePlanta.getText().toString().trim() : "";
        venta.duenia = spinnerDuenia.getText().toString();
        venta.estadoDinero = Venta.ESTADO_EMPRENDIMIENTO;
        venta.rutaFoto = rutaFotoSeleccionada;

        repository.insert(venta, id -> {
            venta.id = id;
            GoogleSheetsService.sincronizarVenta(this, venta);
            if (plannedId != -1) {
                new Thread(() -> {
                    com.oasischeck.data.db.AppDatabase db = com.oasischeck.data.db.AppDatabase.getInstance(getApplicationContext());
                    com.oasischeck.data.model.PlannedVenta p = db.plannedVentaDao().getById(plannedId);
                    if (p != null) { p.estado = com.oasischeck.data.model.PlannedVenta.ESTADO_VENDIDA; p.ventaRealId = id; db.plannedVentaDao().update(p); }
                }).start();
            }
            runOnUiThread(() -> { Toast.makeText(this, "Venta guardada", Toast.LENGTH_SHORT).show(); finish(); });
        });
    }
}