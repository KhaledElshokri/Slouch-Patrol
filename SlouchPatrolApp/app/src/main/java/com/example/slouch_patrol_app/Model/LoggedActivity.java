package com.example.slouch_patrol_app.Model;

public class LoggedActivity {
    private final String serializedActivity;
    private final int averageScore;
    private final String runtime;
    private final String date;

    public LoggedActivity(String serializedActivity, int averageScore, String runtime, String date) {
        this.serializedActivity = serializedActivity;
        this.averageScore = averageScore;
        this.runtime = runtime;
        this.date = date;
    }

    public String getSerializedActivity() {
        return serializedActivity;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getDate() {
        return date;
    }
}
