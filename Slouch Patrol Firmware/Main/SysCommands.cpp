// SysCommands.cpp
//#include <ArxContainer.h>
#include <map>
#include <vector>
#include <Arduino.h>
#include "Constants.h"


#include "SysCommands.h"
// #include <EEPROM.h>
#include "Constants.h"

//extern unsigned int __bss_end;
//extern unsigned int *__brkval;

// Define the map to associate command strings with handler functions
std::map<String, CommandHandler> sysCommandMap;

void setupSysCommandMap() {
  sysCommandMap["MEM?"] = Get_Memory;
  sysCommandMap["CAL?"] = Get_Calibration;
  sysCommandMap["CAL"] = Set_Calibration;

}


std::vector<String> handleSysCommands(String command) {
  String baseCommand;
  String arg = ""; 

  // Check for args 
  int spaceIndex = command.indexOf(' ');
  if (spaceIndex > 0) {
    baseCommand = command.substring(0, spaceIndex);
    arg = command.substring(spaceIndex + 1);
  } 
  else {
    baseCommand = command;
  }
  
  if (debug){
    Serial.println(baseCommand);
    Serial.println(arg);
  }
  
  // Remove "SYS:" from the command
  baseCommand = removeFirstSection(baseCommand);
  if (sysCommandMap.find(baseCommand) != sysCommandMap.end()) {
    return (sysCommandMap[baseCommand](arg));       // Call the associated function
  } 
  else {
    Serial.println(F("ERROR: Unknown SYS Command"));
  } 

}


std::vector<String> Get_Memory(String arg) {
  /*int free_memory;
  if ((int)__brkval == 0) {
    free_memory = ((int)&free_memory) - ((int)&__bss_end);
  } else {
    free_memory = ((int)&free_memory) - ((int)__brkval);
  }  
  Serial.println(free_memory);
  */
  std::vector<String> result;
  int free_memory = ESP.getFreeHeap();

  if (free_memory >= 0) {
    result.push_back("");                                   
    result.push_back(String(free_memory));                          // Free memory in bytes
  } 
  else {result.push_back("ERROR: Failed to retrieve memory"); }

  return result;
}


// Get calibration data of the sensor
std::vector<String> Get_Calibration(String arg) {   
  std::vector<String> result;    
  result.push_back("");  // First position is reserved for error, empty means no error
  
  if (isValidInteger(arg)) {
    int ID = arg.toInt();

    if (ID >= 0 && ID < numSensor) {
      SensorData &sensor = sensors[ID];      
      
      // Read the offsets from the MPU6050
      sensor.cax = sensor.accelGyro.getXAccelOffset();
      sensor.cay = sensor.accelGyro.getYAccelOffset();
      sensor.caz = sensor.accelGyro.getZAccelOffset();      
      sensor.cgx = sensor.accelGyro.getXGyroOffset();
      sensor.cgy = sensor.accelGyro.getYGyroOffset();
      sensor.cgz = sensor.accelGyro.getZGyroOffset();

      // Append the offsets to the result vector
      result.push_back(String(sensor.cax));
      result.push_back(String(sensor.cay));
      result.push_back(String(sensor.caz));      
      result.push_back(String(sensor.cgx));
      result.push_back(String(sensor.cgy));
      result.push_back(String(sensor.cgz));
      
    } else { result[0] = "Invalid sensor ID"; }
  } else { result[0] = "Invalid argument: must be an integer between 0 and " + String(numSensor - 1); }   

  return result;
}


// Set calibration data
std::vector<String> Set_Calibration(String arg) {                  
  int loop_start = 0, loop_stop = 0, sampleSize = 6;  
  std::vector<String> result; result.push_back("");       // First position is reserved for error, empty means no error

  for (int i = 0; i < numSensor; ++i) {
    sensors[i].accelGyro.setSleepEnabled(true);           // Put sensor to sleep    
    if (debug) {
      Serial.print("Sensor "); 
      Serial.print(i); 
      Serial.println(" is now asleep.");
    }
  }

  // Determine sensor range based on arg
  if (arg.equalsIgnoreCase("X")) {
      loop_stop = numSensor;
  } else if (isValidInteger(arg)) {
      int ID = arg.toInt();
      if (ID >= 0 && ID < numSensor) {
          loop_start = ID;
          loop_stop = ID + 1;
      } else { result[0] = "Invalid sensor ID"; }
  } else { result[0] = "Invalid argument: must be 'X' or an integer between 0 and " + String(numSensor - 1); }
  
  if (result[0].isEmpty()){
    for (int i = loop_start; i < loop_stop; ++i) {
      if (debug) {
        Serial.print("Calibrating sensor "); 
        Serial.print(i); 
      }
      SensorData &sensor = sensors[i];                  // Access the relevant sensor
      sensors[i].accelGyro.setSleepEnabled(false);      // Wake up sensor to be calibrated
      
      sensor.accelGyro.setDMPEnabled(false);            // Disable DMP during calibration   
      sensor.accelGyro.resetFIFO();         
      sensor.accelGyro.CalibrateAccel(sampleSize);
      //delay (sampleSize * 100 + 250); 
      sensor.accelGyro.CalibrateGyro(sampleSize);    
      //delay (sampleSize * 100 + 250); 
      sensor.accelGyro.CalibrateAccel(1);
      sensor.accelGyro.CalibrateGyro(1);
      sensor.accelGyro.dmpInitialize();                 // Enable DMP post calibration                    
      sensor.accelGyro.setDMPEnabled(true);
      sensors[i].accelGyro.setSleepEnabled(true);       // Put sensor back to sleep      
    } 
  }

  for (int i = 0; i < numSensor; ++i) {
    sensors[i].accelGyro.setSleepEnabled(false);        // wake up all sensor    
    if (debug) {
      Serial.print("Sensor "); 
      Serial.print(i); 
      Serial.println(" is now awake.");
    }
  }
  
  return result; 
}






/*
const int usDelay = 3150;  // Delay to maintain ~200Hz sampling
const int NFast = 1000, NSlow = 10000;
int LowOffset[6], HighOffset[6], Smoothed[6], Target[6] = {0, 0, 0, 0, 0, 0};
int N;

// Initialize and smooth sensor data
void getSmoothedData() {
    int16_t raw[6];
    long sums[6] = {0, 0, 0, 0, 0, 0};

    for (int i = 0; i < N; i++) {
        accelgyro.getMotion6(&raw[iAx], &raw[iAy], &raw[iAz], &raw[iGx], &raw[iGy], &raw[iGz]);
        for (int j = iAx; j <= iGz; j++) sums[j] += raw[j];
        delayMicroseconds(usDelay);
    }
    for (int i = iAx; i <= iGz; i++) Smoothed[i] = (sums[i] + N / 2) / N;
}

// Set offsets for accelerometer and gyroscope
void setOffsets(int offsets[6]) {
    accelgyro.setXAccelOffset(offsets[iAx]);
    accelgyro.setYAccelOffset(offsets[iAy]);
    accelgyro.setZAccelOffset(offsets[iAz]);
    accelgyro.setXGyroOffset(offsets[iGx]);
    accelgyro.setYGyroOffset(offsets[iGy]);
    accelgyro.setZGyroOffset(offsets[iGz]);
}

// Adjust offsets by expanding and narrowing bracket ranges
void adjustOffsets() {
    bool done = false;

    while (!done) {
        done = true;
        setOffsets(LowOffset);
        getSmoothedData();
        for (int i = iAx; i <= iGz; i++) {
            if (Smoothed[i] >= Target[i]) {
                done = false;
                LowOffset[i] -= 1000;
            }
        }

        setOffsets(HighOffset);
        getSmoothedData();
        for (int i = iAx; i <= iGz; i++) {
            if (Smoothed[i] <= Target[i]) {
                done = false;
                HighOffset[i] += 1000;
            }
        }

        for (int i = iAx; i <= iGz; i++) {
            LowOffset[i] = (LowOffset[i] + HighOffset[i]) / 2;
        }
    }
}

// Initialize MPU6050 and calibrate with fast averaging
void setup() {
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin();
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif

    accelgyro.initialize();

    // Targets: Z acceleration should be around 16384 (1g)
    Target[iAz] = 16384;

    N = NFast;
    adjustOffsets();  // Perform offset adjustments
}*/
