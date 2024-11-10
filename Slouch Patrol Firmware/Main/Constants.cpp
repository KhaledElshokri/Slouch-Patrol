// Constants.cpp
#include "Constants.h"
#include "StarCommands.h"
#include "MEASCommands.h"
#include "SysCommands.h"
//#include <ArxContainer.h>
#include <map>
#include <vector>
#include <Arduino.h>

// Global  variables
const int8_t numSensor = 4;             // Number of sensors
boolean debug = false;                  // Activate debug mode
char G_or_MS2 = 'G';                    // Global variable for unit selection (g or m/s²), default is 'G'

int ACCEL_FS_RANGE = 2;                 // Define the full-scale range for accelerometer (Options: 2, 4, 8, 16 (g))
int GYRO_FS_RANGE = 250;                // Define the full-scale range for gyroscope (Options: 250, 500, 1000, 2000 (°/s))
unsigned long intervalTime = 10;        // Define the interval between every sweeps for commands and sensor refresh (ms)
const float G_TO_MS2 = 9.80665;         // Global variable for unit acceleration conversion
float accelConversionFactor, gyroConversionFactor;


// Declare the I2C instances
TwoWire I2C_1 = TwoWire(0);   // Hardware I2C Bus 1
TwoWire I2C_2 = TwoWire(1);   // Hardware I2C Bus 2
SoftWire swI2C1;


// Initialize the sensor array with I2C addresses and buses
SensorData sensors[5] = {
    SensorData(&I2C_1, 0x68),         // Sensor 0: I2C_1 (Wire)
    SensorData(&I2C_1, 0x69),         // Sensor 1: I2C_1 (Wire)
    SensorData(&I2C_2, 0x68),         // Sensor 2: I2C_2
    SensorData(&I2C_2, 0x69),         // Sensor 3: I2C_2
    SensorData(&swI2C1, 0x68)         // Sensor 4: Software I2C
};


// Define the *IDN info
const char MANUFACTURER[] = "390-Team1";
const char MODEL[] = "PM-5";
const char DEVICE_SERIAL[] = "PM-001";
const char FIRMWARE[] = "1.0.0";


String removeFirstSection(String command) {
    int colonIndex = command.indexOf(':');
    
    // Check if a colon exists
    if (colonIndex > 0) {
      return command.substring(colonIndex + 1);
    } else { return command; }
}


// Handle SCPI commands
std::vector<String> handleCommand(String command) {
  command.trim();         // Ensure no extra spaces
  command.toUpperCase();  // Convert to uppercase if necessary
  std::vector<String> result;
  
  if (debug) {
    Serial.println(command);
    if (command.length() == 0) {
      result.push_back("ERROR: Command is empty!");
      return result;                                  // Exit the function early
    }
  }
  
  if (command.startsWith("*"))            { result = handleStarCommands(command); }
  else if (command.startsWith("MEAS:"))   { result = handleMEASCommands(command); } 
  else if (command.startsWith("SYS:"))    { result = handleSysCommands(command); } 
  else { result.push_back("ERROR: Unknown Command"); }

  return result;
}


// Function to set the conversion factors based on the full-scale range
void setConversionFactors(int accelFSRange, int gyroFSRange) {
    // Set accelerometer conversion factor
    switch (accelFSRange) {
        case 2:
            accelConversionFactor = 1.0f / 16384.0f;  // ±2g
            break;
        case 4:
            accelConversionFactor = 1.0f / 8192.0f;   // ±4g
            break;
        case 8:
            accelConversionFactor = 1.0f / 4096.0f;   // ±8g
            break;
        case 16:
            accelConversionFactor = 1.0f / 2048.0f;   // ±16g
            break;
        default:
            Serial.println(F("ERROR: Invalid accelerometer range"));
            break;
    }

    // Set gyroscope conversion factor
    switch (gyroFSRange) {
        case 250:
            gyroConversionFactor = 1.0f / 131.0f;     // ±250°/s
            break;
        case 500:
            gyroConversionFactor = 1.0f / 65.5f;      // ±500°/s
            break;
        case 1000:
            gyroConversionFactor = 1.0f / 32.8f;      // ±1000°/s
            break;
        case 2000:
            gyroConversionFactor = 1.0f / 16.4f;      // ±2000°/s
            break;
        default:
            Serial.println(F("ERROR: Invalid gyroscope range"));
            break;
    }
}


// Check if input is a valid integer
bool isValidInteger(const String& str) {
    if (str.length() == 0) return false;

    int start = 0;
    if (str[0] == '-') {
        if (str.length() == 1) return false;
        start = 1;
    }

    for (int i = start; i < str.length(); i++) {
        if (!isdigit(str[i])) return false;
    }
    
    return true;
}
