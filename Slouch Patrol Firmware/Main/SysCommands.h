// SysCommands.h
#ifndef SYSCOMMANDS_H
#define SYSCOMMANDSS_H

//#include <ArxContainer.h>
#include <map>
#include <vector>
#include <Arduino.h>

// Function declarations for handling SCPI sys commands
std::vector<String> handleSysCommands(String command);
std::vector<String> Get_Memory(String arg);
std::vector<String> Get_Calibration(String arg);
std::vector<String> Set_Calibration(String arg);
std::vector<String> Get_Variable(String arg);
std::vector<String> Set_Variable(String arg);


//void Deep_Calibration(int ID);


// Function pointer type for command handlers
//typedef void (*CommandHandler)();
typedef std::vector<String> (*CommandHandler)(String);

// Map to associate command strings with handler functions
extern std::map<String, CommandHandler> sysCommandMap;

// Setup function to initialize the command map
void setupSysCommandMap();

#endif // STARCOMMANDS_H
