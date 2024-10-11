package com.example.slouch_patrol_app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    /*
        Main activity is parent activity to data and settings activities.

        For the collection of data, i think we're best off having a polling function to the sensors,
        which then updates the database, inserts into the algorithm and changes all displays.
        This way we can MAYBE avoid having to have complicated back and forth requests.
        Not sure really, - Kyle
     */

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

        //settings button set up
        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v -> routeToSettings());

        Button buttonData = findViewById(R.id.buttonData);
        buttonData.setOnClickListener(v -> routeToData());
    }

    private void routeToSettings(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void routeToData(){
        Intent intent = new Intent(MainActivity.this, DataActivity.class);
        startActivity(intent);
    }
}