package com.example.slouch_patrol_app;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private SharedPreferences sharedPreferences;
    private DeviceSettings deviceSettings;

    public SharedPreferencesHelper(Context context) {
        deviceSettings = new DeviceSettings();

        sharedPreferences = context.getSharedPreferences("DevicePreferences", Context.MODE_PRIVATE);
        deviceSettings.setDeviceName(sharedPreferences.getString("DeviceName", null));
        deviceSettings.setLastInitialized(sharedPreferences.getString("LastInitialized", ""));
        deviceSettings.setPushNotifications(sharedPreferences.getBoolean("PushNotifications", false));
        deviceSettings.setConnectionStatus(sharedPreferences.getString("ConnectionStatus", "NOT CONNECTED"));
    }

    public void updateDeviceSettings(Context context) {
        sharedPreferences = context.getSharedPreferences("DevicePreferences", Context.MODE_PRIVATE);
        deviceSettings.setDeviceName(sharedPreferences.getString("DeviceName", null));
        deviceSettings.setLastInitialized(sharedPreferences.getString("LastInitialized", ""));
        deviceSettings.setPushNotifications(sharedPreferences.getBoolean("PushNotifications", false));
        deviceSettings.setConnectionStatus(sharedPreferences.getString("ConnectionStatus", "NOT CONNECTED"));
    }

    public DeviceSettings getDeviceSettings() {
        return deviceSettings;
    }

    public void setDeviceSettings(DeviceSettings deviceSettings) {
        this.deviceSettings = deviceSettings;
    }

    public void saveDeviceSettings(DeviceSettings deviceSettings) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("DeviceName", deviceSettings.getDeviceName());
        editor.putString("LastInitialized", deviceSettings.getLastInitialized());
        editor.putBoolean("PushNotifications", deviceSettings.isPushNotifications());
        editor.putString("ConnectionStatus", deviceSettings.getConnectionStatus());
        editor.apply();
    }

}


