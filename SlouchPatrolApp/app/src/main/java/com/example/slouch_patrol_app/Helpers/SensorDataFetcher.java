package com.example.slouch_patrol_app.Helpers;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SensorDataFetcher {

    private static final String ESP32_URL = "http://10.0.0.66/";

    OkHttpClient client = new OkHttpClient();

    public String getSensorData() throws IOException {
        Request request = new Request.Builder()
                .url(ESP32_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();  // Return sensor data as a string
        }
    }
}
