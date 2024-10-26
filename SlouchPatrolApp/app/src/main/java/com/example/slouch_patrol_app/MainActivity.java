package com.example.slouch_patrol_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SensorDataFetcher dataFetcher = new SensorDataFetcher();
    private final Handler handler = new Handler();
    private static final int FETCH_INTERVAL_MS = 100; // Fetch data every 0.1 seconds

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

        // Settings button setup
        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v -> routeToSettings());

        Button buttonData = findViewById(R.id.buttonData);
        buttonData.setOnClickListener(v -> routeToData());

        // Start periodic fetching of sensor data
        startFetchingSensorData();
    }

    private void routeToSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void routeToData() {
        Intent intent = new Intent(MainActivity.this, DataActivity.class);
        startActivity(intent);
    }

    private void startFetchingSensorData() {
        handler.post(fetchSensorDataRunnable); // Start the periodic fetching
    }

    private final Runnable fetchSensorDataRunnable = new Runnable() {
        @Override
        public void run() {
            fetchSensorData();
            handler.postDelayed(this, FETCH_INTERVAL_MS); // Repeat after the specified interval
        }
    };

    private void fetchSensorData() {
        new Thread(() -> {
            try {
                String sensorData = dataFetcher.getSensorData(); // Fetch data in the background
                runOnUiThread(() -> {
                    TextView sensorInput = findViewById(R.id.textViewScore);
                    sensorInput.setText(sensorData); // Update UI with the fetched data
                });
            } catch (IOException e) {
                e.printStackTrace(); // Log the error
                runOnUiThread(() -> {
                    TextView sensorInput = findViewById(R.id.textViewScore);
                    sensorInput.setText("Error fetching data: " + e.getMessage()); // Handle error
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchSensorDataRunnable); // Stop fetching when the activity is destroyed
    }
}
