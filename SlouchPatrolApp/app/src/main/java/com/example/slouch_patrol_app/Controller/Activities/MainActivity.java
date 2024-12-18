package com.example.slouch_patrol_app.Controller.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.example.slouch_patrol_app.Controller.Fragments.*;
import com.example.slouch_patrol_app.Features.PostureCalculator;
import com.example.slouch_patrol_app.Helpers.*;
import com.example.slouch_patrol_app.Model.SessionData;
import com.example.slouch_patrol_app.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Random;

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
    private ImageView officerState;

    private int userID;
    private String username;

    // SENSOR OBJECTS
    private SensorDataFetcher dataFetcher = new SensorDataFetcher();
    private final Handler handler = new Handler();
    private static final int FETCH_INTERVAL_MS = 200; // Fetch data every 0.5 seconds

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_activity), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve user credentials securely in the background thread
        username = getCurrentUser();

        // Initialize Helper Classes
        databaseHelper = new DatabaseHelper(this);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        postureCalculator = new PostureCalculator(dataFetcher,databaseHelper);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // String username = sharedPreferences.getString("username", null);

        // Initialize Button + Score Display
        ImageButton stopButton = findViewById(R.id.stop_button);
        officerState = findViewById(R.id.Officer_Image);
        officerState.setImageResource(R.drawable.happy_officer);
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
    public void onSaveFragmentEvent(String event, String sessionName, String sessionNotes, String sessionType) {
        switch (event) {
            case "discard":
                // TODO: DISCARD SESSION
                //       clear relevant DB data
                //       return to main activity
                Toast.makeText(this, "Session discarded", Toast.LENGTH_SHORT).show();
                databaseHelper.clearPostureTable();
                // go back to home activity
                routeToHome();
                break;
            case "resume":
                // TODO: DISPLAY SCORE
                //       APP IS RUNNING -> DISPLAY SCORE
                startFetchingSensorData();
                break;
            case "save":  // if (event.equals("save"))
                // Get required information to be saved in activity table
                String username = getCurrentUser();
                userID = databaseHelper.getUserIdByUsername(username);
                int[] postureScores;
                int averageScore;
                String runtime;
                String date;
                try {
                    postureScores = databaseHelper.getPostureScoresByUserID(userID);
                    averageScore = databaseHelper.getAverageScore(userID);
                    runtime = databaseHelper.getRuntimeFromPTable(userID);
                    date = databaseHelper.getDateFromPTable(userID);
                } catch (Exception e) {
                    e.printStackTrace();
                    postureScores = new int[0];
                    averageScore = 0;
                    runtime = "00:00:00";
                    date = "00/00/0000";
                    Toast.makeText(this, "Error retrieving posture scores", Toast.LENGTH_SHORT).show();
                    routeToHome();
                    return;
                }
                Log.d("MainActivity", "Saving session to activity log");
                // serialize data to be saved in activity table
                SessionData sessionData = new SessionData(sessionType, sessionName, sessionNotes, postureScores);
                Gson gson = new Gson();
                String sessionDataJSON = gson.toJson(sessionData);
                assert sessionDataJSON!=null;
                // save to database
                boolean success = databaseHelper.insertActivity(userID, sessionDataJSON, averageScore, runtime, date);
                if (success) {
                    Toast.makeText(this, "Session saved to activity log", Toast.LENGTH_SHORT).show();
                    databaseHelper.clearPostureTable();
                } else {
                    Toast.makeText(this, "Error saving session to activity log", Toast.LENGTH_SHORT).show();
                }
                routeToHome();
                break;
        }
    }

    @Override
    public void onStopFragmentEvent(String event) {
        if (event.equals("resume")) {
            startFetchingSensorData();
        } else if (event.equals("recalibrate")) {
            startCalibrationFragment();
        } else if (event.equals("finish")){
            // LET USER SAVE SESSION
            startSaveSessionFragment();
        }
    }

    private void startCalibrationFragment() {
        CalibrationFragment calibrationFragment = new CalibrationFragment();
        calibrationFragment.show(getSupportFragmentManager(), "fragment_calibration");
    }

    private void startStoppedFragment() {
        // Stop fetching sensor data
        handler.removeCallbacks(fetchSensorDataRunnable);
        // Start stopped fragment
        StopSessionFragment stopSessionFragment = new StopSessionFragment();
        Bundle bundle = new Bundle();
        try {
            bundle.putInt("userID", userID);
            bundle.putInt("avgScore", databaseHelper.getAverageScore(userID));
            bundle.putString("runtime", databaseHelper.getRuntimeFromPTable(userID));
        } catch (Exception e) {
            e.printStackTrace();
            bundle.putInt("userID", userID);
            bundle.putInt("avgScore", 0);
            bundle.putString("runtime", "00:00:00");
        }

        stopSessionFragment.setArguments(bundle);

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

    /*private void displayUserScore(String username) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor userCursor = db.rawQuery(
                "SELECT " + DatabaseHelper.getUserIdColumn() + " FROM " + DatabaseHelper.getUserTable() + " WHERE " + DatabaseHelper.getUsernameColumn() + " = ?",
                new String[]{username});

        if (userCursor != null && userCursor.moveToFirst()) {
            int userId = userCursor.getInt(userCursor.getColumnIndexOrThrow(DatabaseHelper.getUserIdColumn()));

            Cursor scoreCursor = databaseHelper.getPostureScoresByUserIDCursor(userId);

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
    }*/

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
                // Fetch data and calculate score
                String sensorData = dataFetcher.getSensorData();
                int postureScore = postureCalculator.calculatePostureScore(sensorData);

                // Add posture score to database
                if(username != null)
                {
                    databaseHelper.addPostureScoreForCurrentUser(username, postureScore, String.valueOf(System.currentTimeMillis()));
                }

                if(postureScore > 75)
                {
                    officerState.setImageResource(R.drawable.happy_officer);
                }
                else if(postureScore > 50)
                {
                    officerState.setImageResource(R.drawable.mad_officer);
                }
                else
                {
                    officerState.setImageResource(R.drawable.extreme_officer);
                }

                runOnUiThread(() -> {
                    textViewScore.setText(String.valueOf(postureScore));
                });
            } /*catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> textViewScore.setText("Error fetching data"));
            }*/ catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> textViewScore.setText("Unexpected error occurred: " + e.getMessage()));
            }
        }).start();
    }


    /*
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
    }*/

    public String getCurrentUser() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if the "loggedIn" flag is true
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Log.d("MainActivity", "User is logged in");
            return sharedPreferences.getString("username", null);  // Return username if logged in
        } else {
            Log.d("MainActivity", "User not logged in");
            return null;  // Return null if not logged in
        }
    }
}
