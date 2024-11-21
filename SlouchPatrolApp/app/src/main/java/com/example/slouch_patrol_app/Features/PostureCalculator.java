package com.example.slouch_patrol_app.Features;

import com.example.slouch_patrol_app.Helpers.DatabaseHelper;
import com.example.slouch_patrol_app.Helpers.SensorDataFetcher;

import java.io.IOException;
import java.util.Arrays;

public class PostureCalculator {

    private SensorDataFetcher sensorDataFetcher;
    private DatabaseHelper databaseHelper;

    private static final int CENTER_SPINE = 0;   // Firmware IMU 1
    private static final int LEFT_SHOULDER = 1;  // Firmware IMU 2
    private static final int RIGHT_SHOULDER = 2; // Firmware IMU 3


    public PostureCalculator(SensorDataFetcher sensorDataFetcher, DatabaseHelper databaseHelper) {
        this.sensorDataFetcher = sensorDataFetcher;
        this.databaseHelper = databaseHelper;
    }

    //Parses sensor data string into an array of IMU angles (Yaw, Pitch, Roll)
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

    //Calculate shoulder rounding based on difference between IMUs 1 and 2
    private int calcShoulderRound(double[][] imuData) {
        if (imuData == null || imuData.length <= RIGHT_SHOULDER || imuData[LEFT_SHOULDER].length < 2 || imuData[RIGHT_SHOULDER].length < 2) {
            System.err.println("Error: Missing or invalid data for shoulder IMUs (IMU 2 and IMU 3)");
            return 0;
        }

        double leftPitch = imuData[LEFT_SHOULDER][1];
        double rightPitch = imuData[RIGHT_SHOULDER][1];
        double diff = Math.abs(leftPitch - rightPitch);

        System.out.println("Left Shoulder Pitch: " + leftPitch + ", Right Shoulder Pitch: " + rightPitch + ", Difference: " + diff);

        return (int) Math.min(25, diff);
    }


    //Calculate spine rounding using IMUs 3 and 4, and a pseudo IMU
    private int calcSpineRound(double[][] imuData) {
        if (imuData == null || imuData.length <= RIGHT_SHOULDER || imuData[CENTER_SPINE].length < 2) {
            System.err.println("Error: Missing or invalid data for spine IMUs (IMU 1, IMU 2, or IMU 3)");
            return 0;
        }

        //Calculate the average pitch of the shoulder IMUs
        double averageShoulderPitch = (imuData[LEFT_SHOULDER][1] + imuData[RIGHT_SHOULDER][1]) / 2.0;

        //Get the pitch of the center spine IMU
        double centerSpinePitch = imuData[CENTER_SPINE][1];

        //Calculate the difference
        double diff = Math.abs(centerSpinePitch - averageShoulderPitch);

        System.out.println("Average Shoulder Pitch: " + averageShoulderPitch);
        System.out.println("Center Spine Pitch: " + centerSpinePitch);
        System.out.println("Spine Rounding Difference: " + diff);

        return (int) Math.min(30, diff);
    }

    //Calculate shoulder asymmetry (individual)
    private int calcShoulderAlone(double[][] imuData, int imuIndex) {
        if (imuData == null || imuIndex >= imuData.length || imuData[imuIndex].length < 3) {
            System.err.println("Error: Missing or invalid data for IMU " + (imuIndex + 1));
            return 0;
        }

        double rollValue = Math.abs(imuData[imuIndex][2]);
        System.out.println("IMU " + (imuIndex + 1) + " Roll: " + rollValue);

        return (int) Math.min(20, rollValue);
    }

    //Calculate the posture score
    public int calculatePostureScore(String wifiString) {
        double[][] imuData = parseSensorData(wifiString);

        int case1 = calcShoulderRound(imuData); //Shoulder rounding
        int case2 = calcSpineRound(imuData);   //Spine rounding
        int case3 = calcShoulderAlone(imuData, LEFT_SHOULDER); //Left shoulder asymmetry
        int case4 = calcShoulderAlone(imuData, RIGHT_SHOULDER); //Right shoulder asymmetry

        int finalScore = 100 - (case1 + case2 + case3 + case4);
        finalScore = Math.max(0, finalScore);

        System.out.println("Final Posture Score: " + finalScore);
        return finalScore;
    }
}

