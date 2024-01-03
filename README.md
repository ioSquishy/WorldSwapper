# Minecraft Server World Swapper
This program makes it easy to create and switch between multiple worlds in a self-hosted Minecraft server. The program stores all worlds in the folder "saved-worlds" and swaps it out with the currently used world folder when requested.

Additionally, it will swap out the server.properties file and use a default one when creating new worlds.
## File safety concerns:
The program does not delete any files/folders. It simply moves the current world folder/server.properties file to its corresponding folder in saved-worlds and moves the requested world/properties file into the main directory.

The program WILL create folders/files when needed such as when running the program for the first time, when creating a new world, and when a server.properties file is missing.

If any operation fails it will throw and error and end promptly.

## Future additions?
- DataPack selection when creating a new world.