package com.example.slouch_patrol_app.Controller.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.slouch_patrol_app.Controller.Fragments.*;
import com.example.slouch_patrol_app.Features.PostureCalculator;
import com.example.slouch_patrol_app.Helpers.*;
import com.example.slouch_patrol_app.R;

import java.io.IOException;

public class MainActivity
        extends AppCompatActivity
        implements
        CalibrationFragment.onCalibrateListener,
        StopSessionFragment.onStopFragmentEventListener,
        SaveSessionFragment.onSaveFragmentEventListener {


    private TextView textViewScore;
    private RelativeLayout relativeLayout;
    private DatabaseHelper databaseHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private PostureCalculator postureCalculator;

    // SENSOR OBJECTS
    private SensorDataFetcher dataFetcher = new SensorDataFetcher();
    private final Handler handler = new Handler();
    private static final int FETCH_INTERVAL_MS = 500; // Fetch data every 0.5 seconds

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
        postureCalculator = new PostureCalculator(dataFetcher,databaseHelper);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPreferences.getString("username", null);

        // Initialize Button + Score Display
        ImageButton stopButton = findViewById(R.id.stop_button);
        textViewScore = findViewById(R.id.textViewScore);
        relativeLayout = findViewById(R.id.relativeLayoutFields);

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
            startFetchingSensorData();
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
            startFetchingSensorData();
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
                    this.textViewScore.setText(String.valueOf(score));
                } else {
                    textViewScore.setText("No score available");
                }
            } else {
                //No scores found for the user
                this.textViewScore.setText("No score available");
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
                // Retrieve user credentials securely in the background thread
                String username = getCurrentUser();

                // Fetch data and calculate score
                String sensorData = dataFetcher.getSensorData();
                int postureScore = postureCalculator.calculatePostureScore(sensorData);

                // Add posture score to database
                if(username != null)
                {
                    databaseHelper.addPostureScoreForCurrentUser(username, postureScore, String.valueOf(System.currentTimeMillis()));
                }

                runOnUiThread(() -> {
                    textViewScore.setText(String.valueOf(postureScore));
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> textViewScore.setText("Error fetching data"));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> textViewScore.setText("Unexpected error occurred: " + e.getMessage()));
            }
        }).start();
    }


    // TODO: TEST THIS
    private int setBackgroundColor(int score) {
        View view = this.getWindow().getDecorView();

        // red      0xB30231
        // green    0x2D9F13
        // orange   0xFF6F4B
        // yellow   0xF7F93C


        if (score >= 90) {
            return 0x2D9F13; // green
        } else if (score >= 70) {
            return 0xF7F93C; // yellow
        } else if (score >= 40) {
            return 0xFF6F4B; // orange
        } else {
            return 0xB30231; // red
        }
    }

    public String getCurrentUser() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if the "loggedIn" flag is true
        boolean isLoggedIn = sharedPreferences.getBoolean("loggedIn", false);

        if (isLoggedIn) {
            return sharedPreferences.getString("username", null);  // Return username if logged in
        } else {
            return null;  // Return null if not logged in
        }
    }

}
