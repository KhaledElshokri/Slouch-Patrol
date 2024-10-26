// MEASCommands.cpp
//#include <ArxContainer.h>
#include <map>
#include <vector>
#include <Arduino.h>
#include "MEASCommands.h"
//#include <MPU6050.h> 
#include <I2Cdev.h> 
#include <MPU6050_6Axis_MotionApps20.h>
// #include <EEPROM.h>
#include "Constants.h"

// Define the maps to associate command strings with handler functions
std::map<String, CommandHandler> measCommandMap;


void setupMEASCommandMap() {
  measCommandMap["ACC?"] = Get_Acc;

}


std::vector<String> handleMEASCommands(String command) {
  String baseCommand;
  String arg = ""; 
  std::vector<String> result;
  result.push_back("");  // First position is reserved for error, empty means no error 

  // Todo optimize in a generic function for all
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
    Serial.print(F("\nBase command ")); 
    Serial.print(baseCommand);
    Serial.print(F("  arg ")); 
    Serial.print(arg); 
  }
  
  // Remove "MEAS:" from the command
  baseCommand = removeFirstSection(baseCommand);  

  if (measCommandMap.find(baseCommand) != measCommandMap.end()) {
    result = measCommandMap[baseCommand](arg);       // Call the associated function
  } 
  else { result[0] = "ERROR: Unknown MEAS Command"; }    
  return result;
}


std::vector<String> Get_Acc(String arg) {  
  char Type;
  int ID; 
  Quaternion q;                                                   // Quaternion data
  VectorFloat gravity;                                            // Gravity vector
  float ypr[3];                                                   // Array to hold yaw, pitch, and roll
  std::vector<String> result;
  result.push_back("");                                           // First position is reserved for error, empty means no error
  unsigned long startTime;
  unsigned long timeout = 500;                                    // Timeout after 500ms

  if (arg.length() >= 2) {
    Type = arg.charAt(0);
    ID = arg.substring(1).toInt();
  }
  
  if (debug){
    Serial.println(F("ID is "));
    Serial.println(ID); 
    Serial.println(F("Type is "));
    Serial.println(Type); 
  }
  
  if (ID >= 0 && ID < numSensor) {                                // Ensure that the sensor pointer is valid
    SensorData &sensor = sensors[ID];                             // Access the relevant sensor

    switch (Type) {
      case 'A':  // For accelerometer data
        sensor.accelGyro.getMotion6(&sensor.ax, &sensor.ay, &sensor.az, &sensor.gx, &sensor.gy, &sensor.gz);
        
        if (G_or_MS2 == 'G') {
          result.push_back(String((sensor.ax) * accelConversionFactor).c_str());
          result.push_back(String((sensor.ay) * accelConversionFactor).c_str());
          result.push_back(String((sensor.az) * accelConversionFactor).c_str());
        } else {
          result.push_back(String((sensor.ax) * accelConversionFactor * G_TO_MS2).c_str());
          result.push_back(String((sensor.ay) * accelConversionFactor * G_TO_MS2).c_str());
          result.push_back(String((sensor.az) * accelConversionFactor * G_TO_MS2).c_str());
        }
        break; 
           
      case 'G':  // For gyroscope data
        sensor.accelGyro.getMotion6(&sensor.ax, &sensor.ay, &sensor.az, &sensor.gx, &sensor.gy, &sensor.gz);
        result.push_back(String((sensor.gx) * gyroConversionFactor).c_str());
        result.push_back(String((sensor.gy) * gyroConversionFactor).c_str());
        result.push_back(String((sensor.gz) * gyroConversionFactor).c_str());
        break;
         
      case 'F':  // For freedom of movement        
        //sensor.accelGyro.resetFIFO();                                 // Clear all previous data in the FIFO     
        //delay(20);         
        if (sensor.accelGyro.getDMPEnabled()) {    
          // Wait until the FIFO contains enough data for one complete packet
          startTime = millis();
          while (sensor.accelGyro.getFIFOCount() < sensor.accelGyro.dmpGetFIFOPacketSize()) {
            if (millis() - startTime > timeout) {                      // Handle timeout error            
              result[0] = ("Timeout waiting for FIFO");
              break;
            }
          }  
          
          // Check if a packet is available and process the data
          if (sensor.accelGyro.dmpGetCurrentFIFOPacket(sensor.fifoBuffer)) {  
            sensor.accelGyro.dmpGetQuaternion(&q, sensor.fifoBuffer);  // Get the latest quaternion data
            sensor.accelGyro.dmpGetGravity(&gravity, &q);  // Get gravity vector using the quaternion
            sensor.accelGyro.dmpGetYawPitchRoll(ypr, &q, &gravity);  // Calculate yaw, pitch, and roll
            
            sensor.yaw = ypr[0] * RAD_TO_DEG;
            sensor.pitch = ypr[1] * RAD_TO_DEG;
            sensor.roll = ypr[2] * RAD_TO_DEG;
    
            // Push yaw, pitch, roll into the result vector
            result.push_back(String(sensor.yaw).c_str());
            result.push_back(String(sensor.pitch).c_str());
            result.push_back(String(sensor.roll).c_str());
          } else { result[0] = "Error: No valid packet in FIFO"; }    
        } else { result[0] = "Error: DMP not calibrated"; }  
          
        break;
              
      default:
        result[0] = "ERROR: Invalid sensor ID";
        break;
    }
  }
    
  return result;
}
