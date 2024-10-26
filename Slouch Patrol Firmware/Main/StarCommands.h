// StarCommands.h
#ifndef STARCOMMANDS_H
#define STARCOMMANDS_H

//#include <ArxContainer.h>
#include <map>
#include <vector>
#include <Arduino.h>

// Function declarations for handling SCPI star commands
std::vector<String> handleStarCommands(String command);
std::vector<String> Get_IDN(String arg);
std::vector<String> handleRST(String arg);


// Function pointer type for command handlers
//typedef void (*CommandHandler)();
typedef std::vector<String> (*CommandHandler)(String);

// Map to associate command strings with handler functions
extern std::map<String, CommandHandler> starCommandMap;

// Setup function to initialize the command map
void setupStarCommandMap();

#endif // STARCOMMANDS_H
