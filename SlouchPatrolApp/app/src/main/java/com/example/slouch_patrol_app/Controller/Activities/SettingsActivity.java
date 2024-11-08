package com.example.slouch_patrol_app.Controller.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;

import com.example.slouch_patrol_app.Model.DeviceSettings;
import com.example.slouch_patrol_app.R;
import com.example.slouch_patrol_app.Helpers.SharedPreferencesHelper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferencesHelper sharedPreferencesHelper;
    private DeviceSettings deviceSettings;
    private EditText deviceNameTextView;
    private TextView lastInitializedTextView;
    private CheckBox notifSettingsCheckBox;
    private Button initializeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Set Up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set Up Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Shared Preferences Helper
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        deviceSettings = sharedPreferencesHelper.getDeviceSettings();

        // Initialize Dynamic UI Elements
        deviceNameTextView = findViewById(R.id.editDeviceName);
        lastInitializedTextView = findViewById(R.id.lastInitialized);
        notifSettingsCheckBox = findViewById(R.id.notifCheckbox);
        initializeButton = findViewById(R.id.buttonInitialize);

        // Set Up Button Click Listener
        initializeButton.setOnClickListener(new View.OnClickListener() {
            // Trigger Initialize on Click
            public void onClick(View v) { onClickInitialize(sharedPreferencesHelper); }
        });
    }

    //allow toolbar to route back to parent activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If User Enables/Disables Notifications, Update Device Settings
                deviceSettings.setPushNotifications(notifSettingsCheckBox.isChecked());
                sharedPreferencesHelper.saveDeviceSettings(deviceSettings);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update Device Settings
        deviceSettings = sharedPreferencesHelper.getDeviceSettings();

        // Update UI Based on Device Settings
        updateUIFromDeviceSettings();
    }

    private void onClickInitialize(SharedPreferencesHelper sharedPreferencesHelper) {
        // Require Device Name
        if (deviceNameTextView.getText().toString().isEmpty()){
            displayToastMessage("A Device Name is Required");
        }
        else {
            // Save Device Settings
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            String lastInitialized = dateFormat.format(timestamp);

            deviceSettings.setDeviceName(deviceNameTextView.getText().toString());
            deviceSettings.setLastInitialized(lastInitialized);
            deviceSettings.setPushNotifications(notifSettingsCheckBox.isChecked());
            deviceSettings.setConnectionStatus("CONNECTED");
            sharedPreferencesHelper.saveDeviceSettings(deviceSettings);

            lastInitializedTextView.setText(deviceSettings.getLastInitialized());

            // Init the Device -> Calibration, etc.
            // TODO: Implement fragment to allow user to calibrate device + redirect to main activity
        }
    }

    private void displayToastMessage(String textToDisplay) {
        Toast.makeText(this, textToDisplay, Toast.LENGTH_LONG).show();
    }

    private void updateUIFromDeviceSettings() {
        // Load any existing settings
        if (deviceSettings.getDeviceName()!=null){
            // Set Device Name
            deviceNameTextView.setText(deviceSettings.getDeviceName());
        }
        if (deviceSettings.getLastInitialized()!=null){
            // Set Last Initialized
            lastInitializedTextView.setText(deviceSettings.getLastInitialized());
        }
        if (deviceSettings.isPushNotifications()){
            // Set Notif Settings
            notifSettingsCheckBox.setChecked(deviceSettings.isPushNotifications());
        }
    }

}