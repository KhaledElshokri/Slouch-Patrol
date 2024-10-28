package com.example.slouch_patrol_app;

public class DeviceSettings {

    private String deviceName;
    private String lastInitialized;
    private Boolean pushNotifications;
    private String connectionStatus;

    public DeviceSettings() {}

    public DeviceSettings(String deviceName, String lastInitialized, Boolean pushNotifications, String connectionStatus) {
        this.deviceName = deviceName;
        this.lastInitialized = lastInitialized;
        this.pushNotifications = pushNotifications;
        this.connectionStatus = connectionStatus;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getLastInitialized() {
        return lastInitialized;
    }

    public void setLastInitialized(String lastInitialized) {
        this.lastInitialized = lastInitialized;
    }

    public Boolean isPushNotifications() {
        return pushNotifications;
    }

    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
}
