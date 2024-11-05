package com.example.slouch_patrol_app.Controller.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.slouch_patrol_app.Controller.Fragments.*;
import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.Helpers.SensorDataFetcher;
import com.example.slouch_patrol_app.Helpers.SharedPreferencesHelper;
import com.example.slouch_patrol_app.R;

import java.io.IOException;

public class MainActivity
        extends AppCompatActivity
        implements
        CalibrationFragment.onCalibrateListener,
        StopSessionFragment.onStopFragmentEventListener,
        SaveSessionFragment.onSaveFragmentEventListener {


    private TextView score;
    private DatabaseHelper databaseHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;

    // SENSOR OBJECTS
    private SensorDataFetcher dataFetcher = new SensorDataFetcher();
    private final Handler handler = new Handler();
    private static final int FETCH_INTERVAL_MS = 100; // Fetch data every 0.1 seconds

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_activity), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Helper Classes
        databaseHelper = new DatabaseHelper(this);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPreferences.getString("username", null);

        // Initialize Button + Score Display
        ImageButton stopButton = findViewById(R.id.stop_button);
        score = findViewById(R.id.textViewScore);

        // Define Button Actions
        stopButton.setOnClickListener(v -> stopSession());

        // Calibrate Device
        startCalibrationFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchSensorDataRunnable); // Stop fetching when the activity is destroyed
    }

    /// SESSION FLOW RELATED METHODS

    @Override
    public void onCalibrate(Boolean isConnected) {
        if (isConnected) {
            //TODO: DISPLAY SCORE
            // APP IS RUNNING -> DISPLAY SCORE
        } else {
            Toast.makeText(this, "Device Unable to be Calibrated", Toast.LENGTH_SHORT).show();
        }
    }

    // STOP SESSION
    private void stopSession() {
        startStoppedFragment();
    }

    @Override
    public void onSaveFragmentEvent(String event) {
        if (event.equals("discard")) {
            // TODO: DISCARD SESSION
            //       clear relevant DB data
            //       return to main activity
            Toast.makeText(this, "Session discarded", Toast.LENGTH_SHORT).show();
            // go back to home activity
            routeToHome();
        } else if (event.equals("resume")) {
            // TODO: DISPLAY SCORE
            //       APP IS RUNNING -> DISPLAY SCORE
        } else { // if (event.equals("save")) -- note: default is to save if something goes wrong
            // TODO: SAVE SESSION
            //       Name, type, notes (if any)
            //       into the activity log db
            //       then return to main activity
            Toast.makeText(this, "Session saved", Toast.LENGTH_SHORT).show();
            // back to home activity
            routeToHome();
        }
    }

    @Override
    public void onStopFragmentEvent(String event) {
        if (event.equals("resume")) {
            startFetchingSensorData();
        } else if (event.equals("recalibrate")) {
            startCalibrationFragment();
        } else {
            // LET USER SAVE SESSION
            startSaveSessionFragment();
        }
    }

    private void startCalibrationFragment() {
        CalibrationFragment calibrationFragment = new CalibrationFragment();
        calibrationFragment.show(getSupportFragmentManager(), "fragment_calibration");
    }

    private void startStoppedFragment() {
        StopSessionFragment stopSessionFragment = new StopSessionFragment();
        stopSessionFragment.show(getSupportFragmentManager(), "fragment_stopped");
    }

    private void startSaveSessionFragment() {
        SaveSessionFragment saveSessionFragment = new SaveSessionFragment();
        saveSessionFragment.show(getSupportFragmentManager(), "fragment_save_session");
    }

    private void routeToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    /// SCORE DISPLAY RELATED METHODS

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
                    this.score.setText(String.valueOf(score));
                } else {
                    score.setText("No score available");
                }
            } else {
                //No scores found for the user
                this.score.setText("No score available");
            }

            if (scoreCursor != null) {
                scoreCursor.close();
            }
        }

        if (userCursor != null) {
            userCursor.close();
        }
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
}
