package com.example.taskmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Izin notifikasi diberikan.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Tanpa izin, Anda tidak akan melihat notifikasi.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnKelola = findViewById(R.id.btnKelola);
        Button btnDaftar = findViewById(R.id.btnDaftar);

        // Hanya minta izin notifikasi, tidak ada lagi penjadwalan alarm yang rumit
        askForNotificationPermission();

        btnKelola.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TaskListActivity.class)));
        btnDaftar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DeadlineActivity.class)));
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}