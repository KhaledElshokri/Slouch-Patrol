package com.example.slouch_patrol_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private SensorDataFetcher dataFetcher = new SensorDataFetcher();
    private final Handler handler = new Handler();
    private SharedPreferencesHelper sharedPreferencesHelper;
    private DeviceSettings deviceSettings;
    private static final int FETCH_INTERVAL_MS = 100; // Fetch data every 0.1 seconds

    private DatabaseHelper databaseHelper;
    private TextView textViewScore, messageText, textViewConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in, otherwise redirect to LoginActivity
        if (!isUserLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //Edge-to-edge handling with system bars
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize helper classes
        databaseHelper = new DatabaseHelper(this);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        deviceSettings = sharedPreferencesHelper.getDeviceSettings();

        textViewScore = findViewById(R.id.textViewScore);
        messageText = findViewById(R.id.message_text);
        textViewConnection = findViewById(R.id.textViewConnection);

        //Fetch the username of the logged-in user from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            //Fetch and display the user's score
            displayUserScore(username);
        } else {
            textViewScore.setText("");
            messageText.setText("");
        }

        // TODO: CHECK CONNECTION STATUS, CHECK IF INITIALIZED


        //Button to navigate to Settings
        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v -> routeToSettings());

        //Button to navigate to Data
        Button buttonData = findViewById(R.id.buttonData);
        buttonData.setOnClickListener(v -> routeToData());

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Fetch device settings from SharedPreferences
        sharedPreferencesHelper.updateDeviceSettings(this);
        deviceSettings = sharedPreferencesHelper.getDeviceSettings();

        // Start periodic fetching of sensor data
        if (Objects.equals(deviceSettings.getConnectionStatus(), "CONNECTED")) {
            startFetchingSensorData();
            messageText.setText("");
        }
        else {
            textViewScore.setText("");
            messageText.setText("Device not connected or initialized.");
        }
        textViewConnection.setText(deviceSettings.getConnectionStatus());
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    //Function to query the database and display the user's score
    private void displayUserScore(String username) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor userCursor = db.rawQuery(
                "SELECT " + DatabaseHelper.getUserIdColumn() + " FROM " + DatabaseHelper.getUserTable() + " WHERE " + DatabaseHelper.getUsernameColumn() + " = ?",
                new String[]{username});

        if (userCursor != null && userCursor.moveToFirst()) {
            int userId = userCursor.getInt(userCursor.getColumnIndexOrThrow(DatabaseHelper.getUserIdColumn()));

            Cursor scoreCursor = databaseHelper.getPostureScoresByUserId(userId);

            if (scoreCursor != null && scoreCursor.moveToFirst()) {
                int scoreColumnIndex = scoreCursor.getColumnIndex("score");
                if (scoreColumnIndex != -1) {
                    float score = scoreCursor.getFloat(scoreColumnIndex);
                    textViewScore.setText(String.valueOf(score));

                    //Display a message based on the score(will be changed later)
                    if (score > 90) {
                        messageText.setText("Your posture is good, keep it up!");
                    } else if (score > 70) {
                        messageText.setText("Your posture is okay, but could be better!");
                    } else {
                        messageText.setText("Your posture needs improvement.");
                    }
                } else {
                    textViewScore.setText("No score available");
                    messageText.setText("");
                }
            } else {
                //No scores found for the user
                textViewScore.setText("No score available");
                messageText.setText("");
            }

            if (scoreCursor != null) {
                scoreCursor.close();
            }
        }

        if (userCursor != null) {
            userCursor.close();
        }
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
                int index = sensorData.indexOf('.');
                String displayValue = (index !=- 1) ? sensorData.substring(0, index) : sensorData ;
                runOnUiThread(() -> {
                    TextView sensorInput = findViewById(R.id.textViewScore);
                    sensorInput.setText(displayValue); // Update UI with the fetched data
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
