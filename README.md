# Minecraft Server World Swapper
This program makes it easy to create and switch between multiple worlds in a self-hosted Minecraft server. The program stores all worlds in the folder "saved-worlds" and swaps it out with the currently used world folder when requested.

## File safety concerns:
The program does not delete any files/folders. It simply moves the current world folder to its corresponding folder in saved-worlds and moves the requested world into the main directory.

The program WILL create folders when needed such as when running the program for the first time and when creating a new world.

If any operation fails it will throw and error and end promptly.

## Future additions?
- Ability to also swap the server.properties file.
- DataPack selection when creating a new world.