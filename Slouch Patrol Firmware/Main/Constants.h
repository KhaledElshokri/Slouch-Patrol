// Constants.h
#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <I2Cdev.h> 
#include <MPU6050_6Axis_MotionApps20.h>
#include <ESP32_SoftWire.h>
#include <map>
#include <vector>
#include <Arduino.h>


// Global variables
extern const int8_t numSensor;                  // Number of sensors
extern boolean debug;
extern char G_or_MS2;    
    
extern int ACCEL_FS_RANGE, GYRO_FS_RANGE;   
extern const float G_TO_MS2; 
extern unsigned long intervalTime;
extern float accelConversionFactor, gyroConversionFactor;

String removeFirstSection(String command);   
void setConversionFactors(int accelFSRange, int gyroFSRange);
bool isValidInteger(const String& str);
std::vector<String> handleCommand(String command) ;


// Declare hardware I2C 
extern TwoWire I2C_1;      // Hardware I2C Bus 1
extern TwoWire I2C_2;      // Hardware I2C Bus 2
extern SoftWire swI2C1;    // Software I2C Bus 1


// Define the SensorData structure with MPU6050 integration
struct SensorData {
    MPU6050 accelGyro;      // MPU6050 object 
    
    int16_t ax, ay, az;     // Accelerometer data
    int16_t gx, gy, gz;     // Gyroscope data
    int16_t cax, cay, caz;  // Calibration accelerometer data
    int16_t cgx, cgy, cgz;  // Calibration gyroscope data
    float pitch, roll, yaw; // Orientation data
    uint8_t fifoBuffer[64]; // DMP FIFO buffer

    // Constructor for hardware I2C
    SensorData(TwoWire* i2c, uint8_t address) : accelGyro(address, i2c) {
        // Initialize with specified I2C bus and address
    }
    
    /*
    SensorData(uint8_t address) : accelGyro(address) {
        // Initialize with hardware I2C address
    }*/

    // Constructor for software I2C
    /*SensorData(SoftwareWire* i2c, uint8_t address) : accelGyro(i2c, address) {
        // Initialize with software I2C and address
    }*/

     SensorData(SoftWire* i2c, uint8_t address) : accelGyro(address, i2c) {
        // Initialize with specified software I2C bus and address
    }
};

// Declare the array of sensors
extern SensorData sensors[];


// Define *IDN infos
extern const char MANUFACTURER[];
extern const char MODEL[];
extern const char DEVICE_SERIAL[];
extern const char FIRMWARE[];



/*
// Hardware built-in I2C bus with default addresses
extern MPU6050 accelGyro1;  // Global MPU6050 sensor for hardware I2C
extern MPU6050 accelGyro2;  // (Optional) Second sensor for hardware I2C

// Software I2C buses (Optional)
extern SoftwareWire myI2C2;
extern SoftwareWire myI2C3;
extern SoftwareWire myI2C4;

// MPU6050 objects on software I2C buses (Optional)
extern MPU6050 accelGyro3;
extern MPU6050 accelGyro4;
extern MPU6050 accelGyro5;
*/

#endif // CONSTANTS_H
