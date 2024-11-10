package com.example.slouch_patrol_app.Features;

import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.Helpers.SensorDataFetcher;

import java.io.IOException;
import java.util.Arrays;

public class PostureCalculator {

    private SensorDataFetcher sensorDataFetcher;
    private DatabaseHelper databaseHelper;

    public PostureCalculator(SensorDataFetcher sensorDataFetcher, DatabaseHelper databaseHelper) {
        this.sensorDataFetcher = sensorDataFetcher;
        this.databaseHelper = databaseHelper;
    }

    // Parses sensor data string into an array of IMU angles (Yaw, Pitch, Roll)
    public double[][] parseSensorData(String sensorData) {
        // Expected format: IMU1:Yaw,Pitch,Roll;IMU2:Yaw,Pitch,Roll;...
        String[] imuData = sensorData.split(";");
        double[][] parsedData = new double[imuData.length][3];

        for (int i = 0; i < imuData.length; i++) {
            String[] angles = imuData[i].split(",");
            parsedData[i] = Arrays.stream(angles).mapToDouble(Double::parseDouble).toArray();
        }
        return parsedData;
    }

    // Calculate shoulder rounding based on difference between IMUs 1 and 2
    private int calcShoulderRound(double[][] imuData) {
        double diff = Math.abs(imuData[0][1] - imuData[1][1]); // Pitch difference
        return (int) Math.min(25, diff);
    }

    // Calculate spine rounding using IMUs 3 and 4, and a pseudo IMU
    private int calcSpineRound(double[][] imuData) {
        double pseudoYaw = (imuData[0][0] + imuData[1][0]) / 2;
        double pseudoPitch = (imuData[0][1] + imuData[1][1]) / 2;
        double diff = Math.abs(imuData[3][1] - pseudoPitch); // Pitch difference with pseudo IMU
        return (int) Math.min(30, diff);
    }

    // Calculate shoulder asymmetry (individual)
    private int calcShoulderAlone(double[][] imuData, int imuIndex) {
        double rollValue = Math.abs(imuData[imuIndex][2]);
        return (int) Math.min(20, rollValue);
    }

    // Calculate the posture score
    public int calculatePostureScore(String wifiString) {

        double [][] imuData = parseSensorData(wifiString);

        int case1 = calcShoulderRound(imuData);
        int case2 = calcSpineRound(imuData);
        int case3 = calcShoulderAlone(imuData, 0);
        int case4 = calcShoulderAlone(imuData,1);

        int finalScore = 100 - (case1 + case2 + case3 + case4);
        finalScore = Math.max(0, finalScore); // Ensure the score is not below 0

        return finalScore;
    }

    // Fetch and process sensor data
    public void processPostureData(int userId) {
        //try {
        //    String sensorData = sensorDataFetcher.getSensorData();
        //    double[][] imuData = parseSensorData(sensorData);

        //    int postureScore = calculatePostureScore(userId, imuData);
        //    System.out.println("Posture Score: " + postureScore);
        //} catch (IOException e) {
        //    System.err.println("Error fetching sensor data: " + e.getMessage());
        //}
    }
}

