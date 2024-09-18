package com.example.astrocamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ImageButton btn;
    private TextureView imageView;
    private Spinner iso;
    private Spinner exposure;
    List<String> exposures = List.of("1","2","3","4","5","6","7","8","9","10","12","15");
    List<String> isos = List.of("100","300","500","700","900","1100","1400","1700","2000","2300","2600");

    private CameraLogic cl;

    public void checkPermission(String perm) {
        if(ContextCompat.checkSelfPermission(MainActivity.this, perm) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "This permission has not been granted",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn = findViewById(R.id.capture);
        imageView = findViewById(R.id.image);
        iso = findViewById(R.id.iso);
        exposure = findViewById(R.id.exposure);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, exposures);
        exposure.setAdapter(dataAdapter);

        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, isos);
        iso.setAdapter(dataAdapter1);

        cl = new CameraLogic(this);

        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                PackageManager.PERMISSION_GRANTED);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PackageManager.PERMISSION_GRANTED) {
            cl.startCamera();
        }
    }
    public ImageButton getBtn() {
        return btn;
    }

    public TextureView getImageView() {
        return imageView;
    }

    public int getIso() {
        Object o = iso.getSelectedItem();
        if(o == null)return 2000;
        else return Integer.parseInt(String.valueOf(o));
    }

    public Long getExposure() {
        Object o = exposure.getSelectedItem();
        if(o == null)return 4000000000L;
        else return Long.parseLong(String.valueOf(o))*1000000000L;
    }
}