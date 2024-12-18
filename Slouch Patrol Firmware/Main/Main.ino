// main.ino
#include <WiFi.h>
#include <WebServer.h>
//#include <I2Cdev.h>
#include <MPU6050_6Axis_MotionApps20.h>
#include <ESP32_SoftWire.h>
#include <Wire.h>
#include "StarCommands.h"
#include "MEASCommands.h"
#include "SysCommands.h"
#include "Constants.h"
#include <map>
#include <vector>
#include <Arduino.h>


const char* ssid = "SSID";
const char* password = "WIFIPASSWORD";

WebServer server(80); // Create a web server object that listens on port 80

static String AppData = "IMUs are not initialised or not calibrated";

// Function to handle the root URL ("/")
void handleRoot() {
    server.send(200, "text/plain", AppData);    // Send the sensor data
}

void setup() {
  std::vector<String> result;
  Serial.begin(115200);
  esp_log_level_set("*", ESP_LOG_NONE);         // This silences all logs

  //
  // START WIFI SERVER WITH LIMITED RETRIES
  //
  WiFi.begin(ssid, password);                   // Connect to Wi-Fi
  int attempts = 0;
  const int maxAttempts = 5;

  while (WiFi.status() != WL_CONNECTED && attempts < maxAttempts) {
      delay(1000);
      Serial.print("Attempting to connect to WiFi (");
      Serial.print(attempts + 1);
      Serial.println(" of 5)");
      attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
      Serial.println("WiFi connected");
      Serial.print("ESP32 IP address: ");
      Serial.println(WiFi.localIP());           // Print the IP address

      server.on("/", handleRoot);               // Associate root URL with handler
      server.begin();                           // Start the server
      Serial.println("Web server started");
  } else {
      Serial.println("WiFi connection failed after 5 attempts. Using Serial for communication.");
  }

  //
  // END WIFI SETUP
  //

  // Initialize command map
  setupStarCommandMap();
  setupMEASCommandMap();
  setupSysCommandMap();

  // Set the conversion factors
  setConversionFactors(ACCEL_FS_RANGE, GYRO_FS_RANGE);  

  I2C_1.begin(21, 22);                                        // I2C Bus 0: SDA=21, SCL=22
  I2C_1.setClock(400000);                                     // Set clock speed to 400kHz for I2C Bus 0
  
  I2C_2.begin(16, 17);                                        // I2C Bus 1: SDA=16, SCL=17
  I2C_2.setClock(400000);                                     // Set clock speed to 400kHz for I2C Bus 1

  // Declare Software
  swI2C1.setPins(4, 5);                                       // SDA=GPIO4, SCL=GPIO5
  swI2C1.begin();                                             // Software I2C Bus 1: SDA=4, SCL=5

  // Loop to initialize and configure all sensors
  for (int i = 0; i < numSensor; i++) {
    SensorData &sensor = sensors[i];                          // Reference to the current sensor        

    sensor.accelGyro.reset();
    delay(100);  // Give some time for the reset to complete

    sensor.accelGyro.initialize();    
    sensor.accelGyro.dmpInitialize(); 
    sensor.accelGyro.setDMPEnabled(false);                    // Turn off DMP initially          

    if (debug) {
      uint8_t devStatus = sensor.accelGyro.dmpInitialize();   // DMP Initialization
  
      if (devStatus == 0) {                                   // devStatus 0 means DMP initialization successful
          Serial.print("DMP for sensor ");
          Serial.print(i);
          Serial.println(" initialized successfully.");
          sensor.accelGyro.setDMPEnabled(true);               // Enable DMP if initialized correctly
      } else {
          Serial.print("DMP Initialization failed for sensor ");
          Serial.print(i);
          Serial.print(" with status code: ");
          Serial.println(devStatus);                          // Non-zero status means initialization failed
      }
  
      // Check if the sensor is properly connected
      if (!sensor.accelGyro.testConnection()) {
        Serial.print(F("MPU6050 not connected for sensor "));
        Serial.println(i);
        while (1);  // Halt the program if connection fails
      }
  
      if (sensor.accelGyro.getDMPEnabled()) {
          Serial.print("DMP for sensor ");
          Serial.print(i);
          Serial.println(" is enabled.");
      } else {
          Serial.print("DMP for sensor ");
          Serial.print(i);
          Serial.println(" failed to enable.");
      }
    }
    
    sensor.accelGyro.setClockSource(MPU6050_CLOCK_PLL_XGYRO);                 // Set clock source to X-axis gyroscope for stability   
    sensor.accelGyro.setSleepEnabled(false);                                  // Disable sleep mode      
    
    delay(100);                                                               // Delay to avoid overwhelming the I2C bus during initialization
    result = Set_Calibration("QX");                                           // Perform simple calibration on startup
  }
  
  if (debug){  Serial.println(F("\nConfig completed for all sensors"));  }    // For troubleshooting
}


void loop() {
  unsigned long startTime = millis();
    
  if (Serial.available() > 0) {
    String command = Serial.readStringUntil('\n');
    command.trim();                                                           // Remove any leading/trailing whitespace
    std::vector<String> data = handleCommand(command);
    
    if (data[0] != "") {                                                      // Check if there is an error message in the first element
      Serial.println(data[0]);                                                // If there is an error, print it
    } 
    else if (data.size() > 1) {
      String output = "";
      for (size_t i = 1; i < data.size(); i++) {
        output += data[i];
        if (i < data.size() - 1) { output += ","; }                           // Add commas between values      
      }
      Serial.println(output);
    } 
  }else {      
    for (int i = 0; i < numSensor; ++i) {                                     // keeps data related to YPR accurate by measuring often when on standby
      SensorData &sensor = sensors[i]; 
      Quaternion q; VectorFloat gravity; float ypr[3];  
    
      if (sensor.accelGyro.getDMPEnabled()) {   
        if (sensor.accelGyro.dmpGetCurrentFIFOPacket(sensor.fifoBuffer)) {  
          sensor.accelGyro.dmpGetQuaternion(&q, sensor.fifoBuffer);           // Get the latest quaternion data
          sensor.accelGyro.dmpGetGravity(&gravity, &q);                       // Get gravity vector using the quaternion
          sensor.accelGyro.dmpGetYawPitchRoll(ypr, &q, &gravity);             // Calculate yaw, pitch, and roll
          
          sensor.yaw = ypr[0] * RAD_TO_DEG;
          sensor.pitch = ypr[1] * RAD_TO_DEG;
          sensor.roll = ypr[2] * RAD_TO_DEG;

          // Add to the string that will be delivered
          AppData += String(sensor.yaw) + "," + String(sensor.pitch) + "," + String(sensor.roll) + ";" ;

        }
      }    
    }    
  }
  
  server.handleClient();                                                      // Handle client requests
  
  // Reset the string for next transmission
  AppData = "";

  // Calculate time spent during this loop iteration
  unsigned long endTime = millis();
  unsigned long elapsedTime = endTime - startTime;
  if (elapsedTime < intervalTime) {
      unsigned long remainingTime = intervalTime - elapsedTime;
      delay(remainingTime);                                                   // Pause for the remaining time to match the intervalTime
  }  
}
