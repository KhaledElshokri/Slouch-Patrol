package com.example.slouch_patrol_app.Controller.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.slouch_patrol_app.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if logged in
        if(!isLoggedIn()) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //Edge-to-edge handling with system bars
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Buttons
        Button buttonStartActivity = findViewById(R.id.startSessionButton);
        Button buttonActivityLog = findViewById(R.id.activityLogButton);
        Button buttonChatBot = findViewById(R.id.ChatBotButton);
        ImageButton buttonSettings = findViewById(R.id.settingsButton);

        // Initialize Button Actions
        buttonStartActivity.setOnClickListener(v -> routeToMain());
        buttonActivityLog.setOnClickListener(v -> routeToActivityLog());
        buttonSettings.setOnClickListener(v -> routeToSettings());
        buttonChatBot.setOnClickListener(v -> routeToChatBot());

    }

    protected void onResume() {
        super.onResume();
        // DB Refresh, etc.
    }

    private boolean isLoggedIn() {
        // TODO: check if logged in
        return true;
    }

    private void routeToMain() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void routeToSettings() {
        Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void routeToActivityLog() {
        Intent intent = new Intent(HomeActivity.this, DataActivity.class);
        startActivity(intent);
    }

    private void routeToChatBot() {
        Intent intent = new Intent(HomeActivity.this, ChatBotActivity.class);
        startActivity(intent);
    }
}
