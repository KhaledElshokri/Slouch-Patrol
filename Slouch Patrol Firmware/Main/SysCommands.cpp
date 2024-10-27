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
  int loop_start = 0, loop_stop = 0, sampleSize;  
  std::vector<String> result; result.push_back("");       // First position is reserved for error, empty means no error
  char Type;
  unsigned long startTime = 0;

  // Start timing the calibration if debugging is active
  
  // Put all sensors to sleep
  for (int i = 0; i < numSensor; ++i) {
    sensors[i].accelGyro.setSleepEnabled(true);               
    if (debug) {
      Serial.print("Sensor "); 
      Serial.print(i); 
      Serial.println(" is now asleep.");
    }
  }

  // check for args and split
  if (arg.length() >= 2) {
    Type = arg.charAt(0);

    // Determine sensor range based on arg
    if (arg.substring(1).equalsIgnoreCase("X")) {
        loop_stop = numSensor;
    } else if (isValidInteger(arg.substring(1))) {
        int ID = arg.substring(1).toInt();
        if (ID >= 0 && ID < numSensor) {
            loop_start = ID;
            loop_stop = ID + 1;
        } else { result[0] = "Invalid sensor ID"; }
    } else { result[0] = "Invalid argument: must be 'Q' or 'D' followed by 'X' or an integer between 0 and " + String(numSensor - 1); }
  }

  // Check for type to assign sample qty
  switch (Type) {
    case 'Q': 
      sampleSize = 1;
      break; 
    case 'D':
      sampleSize = 10;
      break; 
    default:
      result[0] = "ERROR: Invalid type";
      break;
  }

  // Begin calibration of no errors
  if (result[0].isEmpty()){
    for (int i = loop_start; i < loop_stop; ++i) {
      if (debug) {
        Serial.print("\nCalibrating sensor "); 
        Serial.print(i);         
        startTime = millis();
      }

      SensorData &sensor = sensors[i];                  // Access the relevant sensor
      sensor.accelGyro.setSleepEnabled(false);          // Wake up sensor to be calibrated
      sensor.accelGyro.setDMPEnabled(false);            // Disable DMP during calibration   
      sensor.accelGyro.resetFIFO();        
      
      sensor.accelGyro.CalibrateAccel(sampleSize);        
      sensor.accelGyro.CalibrateGyro(sampleSize);             
          
      //sensor.accelGyro.resetFIFO();               
      sensor.accelGyro.setDMPEnabled(true);
      sensors[i].accelGyro.setSleepEnabled(true);       // Put sensor back to sleep 
      if (debug) {
        unsigned long endTime = millis();
        
        switch (Type) {
          case 'Q': 
            Serial.print("\nQuick"); 
            break; 
          case 'D':
            Serial.print("\nDeep");
            break; 
        }        
        Serial.print(" Calibration for sensor");
        Serial.print(i);
        Serial.print(" completed in ");
        Serial.print((endTime - startTime));
        Serial.println(" ms.");

        std::vector<String> calibrationData = Get_Calibration(String(i));
        for (const auto& data : calibrationData) {
          Serial.print(data);
          Serial.print("  ");
        }
      }       
    } 
  }

  // Wake up all sensors post calibration
  for (int i = 0; i < numSensor; ++i) {
    sensors[i].accelGyro.setSleepEnabled(false);     
    if (debug) {
      Serial.print("Sensor "); 
      Serial.print(i); 
      Serial.println(" is now awake.");
    }
  }
  
  return result; 
}


// Super duper excessive calibration
//void Deep_Calibration(int ID) {
//  const int NFast = 1000, NSlow = 10000, usDelay = 3150;
//  const int maxIterations = 1000;  // Set a max limit for iterations
//  const int tolerance = 10;        // Define tolerance for offset convergence
//  int Smoothed[6], LowOffset[6] = {0, 0, 0, 0, 0, 0}, HighOffset[6] = {0, 0, 0, 0, 0, 0};
//  int NextLowOffset[6], NextHighOffset[6];
//  int Target[6] = {0, 0, 16384, 0, 0, 0}; // Target values for accelerometer and gyro
//  int16_t raw[6];
//  long sums[6] = {0, 0, 0, 0, 0, 0};
//  int N;
//
//  const int iAx = 0, iAy = 1, iAz = 2, iGx = 3, iGy = 4, iGz = 5;  // Declare indices for axes
//
//  bool done = false;
//  int iterationCount = 0;
//  
//  // Local helper function to set offsets
//  auto SetOffsets = [&]() {
//    sensors[ID].accelGyro.setXAccelOffset(LowOffset[iAx]);
//    sensors[ID].accelGyro.setYAccelOffset(LowOffset[iAy]);
//    sensors[ID].accelGyro.setZAccelOffset(LowOffset[iAz]);
//    sensors[ID].accelGyro.setXGyroOffset(LowOffset[iGx]);
//    sensors[ID].accelGyro.setYGyroOffset(LowOffset[iGy]);
//    sensors[ID].accelGyro.setZGyroOffset(LowOffset[iGz]);
//  };
//
//  // Local helper function to get smoothed values
//  auto GetSmoothed = [&]() {
//    for (int j = 0; j < 6; j++) sums[j] = 0; // Reset sums for new calculation
//    for (int i = 0; i < N; i++) {
//        sensors[ID].accelGyro.getMotion6(&raw[0], &raw[1], &raw[2], &raw[3], &raw[4], &raw[5]);
//        for (int j = 0; j < 6; j++) {
//            sums[j] += raw[j];
//        }
//        delayMicroseconds(usDelay);
//    }
//    for (int j = 0; j < 6; j++) {
//        Smoothed[j] = sums[j] / N;
//    }
//  };
//
//  // Phase 1 (NFast) to get initial smoothed values
//  N = NFast;
//  GetSmoothed(); // Call GetSmoothed to initialize smoothed values
//
//  // Initialize NextLowOffset and NextHighOffset
//  for (int i = 0; i < 6; i++) {
//    if (Smoothed[i] > Target[i]) { 
//      LowOffset[i] -= 1000; 
//      NextLowOffset[i] = LowOffset[i];
//    } else {
//      HighOffset[i] += 1000;
//      NextHighOffset[i] = HighOffset[i];
//    }
//  }
//
//  done = true;
//  
//  // Calibration Phase 2 (NSlow)
//  N = NSlow;
//  while (!done && iterationCount < maxIterations) {
//    iterationCount++;
//    done = true;
//    
//    // Get low offsets
//    SetOffsets(); // Set the low offsets
//    GetSmoothed(); // Get smoothed values
//    for (int i = 0; i < 6; i++) {
//      if (Smoothed[i] >= Target[i]) { 
//        done = false;
//        NextLowOffset[i] = LowOffset[i] - 1000;
//      } else {
//        NextLowOffset[i] = LowOffset[i];
//      }
//    }
//
//    // Get high offsets
//    SetOffsets(); // Set the high offsets
//    GetSmoothed(); // Get smoothed values
//    for (int i = 0; i < 6; i++) {
//      if (Smoothed[i] <= Target[i]) {
//        done = false;
//        NextHighOffset[i] = HighOffset[i] + 1000;
//      } else {
//        NextHighOffset[i] = HighOffset[i];
//      }
//    }
//    
//    // Update LowOffset and HighOffset after checking smoothed values
//    for (int i = 0; i < 6; i++) {
//      LowOffset[i] = NextLowOffset[i];
//      HighOffset[i] = NextHighOffset[i];
//      
//      // Stop if the difference between LowOffset and HighOffset is within tolerance
//      if (abs(HighOffset[i] - LowOffset[i]) > tolerance) {
//        done = false;
//      }
//    }
//
//    // Print debugging output every 10 iterations
//    if (debug && iterationCount % 10 == 0) {
//      Serial.print("Iteration: ");
//      Serial.print(iterationCount);
//      Serial.print(" - Done: ");
//      Serial.println(done ? "Yes" : "No");
//
//      for (int i = 0; i < 6; i++) {
//        Serial.print("Axis ");
//        Serial.print(i);
//        Serial.print(": LowOffset = ");
//        Serial.print(LowOffset[i]);
//        Serial.print(", HighOffset = ");
//        Serial.print(HighOffset[i]);
//        Serial.print(", Difference = ");
//        Serial.println(abs(HighOffset[i] - LowOffset[i]));
//      }
//    }
//
//    // Break out if max iterations reached
//    if (iterationCount >= maxIterations) {
//      Serial.println("Max iterations reached without convergence.");
//      break;
//    }
//
//    
//  }
//
//  // Apply the final offsets using the SetOffsets helper function
//  SetOffsets();  
//}
