// StarCommands.cpp
//#include <ArxContainer.h>
#include <map>
#include <vector>
#include <Arduino.h>

#include "StarCommands.h"
// #include <EEPROM.h>
#include "Constants.h"

// Define the map to associate command strings with handler functions
std::map<String, CommandHandler> starCommandMap;

void setupStarCommandMap() {
  starCommandMap["*IDN?"] = Get_IDN;
  starCommandMap["*RST"] = handleRST;
  // Add other star commands and their handlers here
}

std::vector<String> handleStarCommands(String command) {
  auto it = starCommandMap.find(command);
  String baseCommand;
  String arg = "";
  std::vector<String> result; 

  // Check for args 
  int spaceIndex = command.indexOf(' ');
  if (spaceIndex > 0) {
    baseCommand = command.substring(0, spaceIndex);
    arg = command.substring(spaceIndex + 1);
  } 
  else { baseCommand = command; }
  
  baseCommand = removeFirstSection(baseCommand);
  
  if (starCommandMap.find(baseCommand) != starCommandMap.end()) {
    result = starCommandMap[baseCommand](arg);       // Call the associated function
  } 
  else { result.push_back("ERROR: Unknown Star Command"); } 
  
  return result;
}

std::vector<String> Get_IDN(String arg) {
  std::vector<String> result;

  result.push_back(""); // No errors in location 0
  result.push_back(MANUFACTURER);
  result.push_back(MODEL);
  result.push_back(DEVICE_SERIAL);
  result.push_back(FIRMWARE);

  return result;  // Return the vector containing the device information
}

std::vector<String> handleRST(String arg) {
  // Handle the *RST command (reset the device)
  Serial.println(F("Device reset."));
  // Add code to reset your device here
}
