// MEASCommands.h
#ifndef MEASCOMMANDS_H
#define MEASCOMMANDS_H

//#include <ArxContainer.h>
#include <map>
#include <vector>
#include <Arduino.h>

// Declare the individual handler functions
std::vector<String> handleMEASCommands(String command);
std::vector<String> Get_Acc(String arg);
//vector<String> handleMEASCommands(String command);
//vector<String> Get_Acc(String arg);


// Function pointer type for command handlers
//typedef void (*CommandHandler)();
typedef std::vector<String> (*CommandHandler)(String);

// Map to associate command strings with handler functions
extern std::map<String, CommandHandler> MEASCommandMap;

// Setup function to initialize the command map
void setupMEASCommandMap();


#endif // MEASCOMMANDS_H
