import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.TreeMap;
import java.util.Scanner;

public class App {
    private static final File savedWorldsFolder = new File("./saved-worlds");
    private static final File currentWorldKeyTxt = new File("./saved-worlds/currentWorldKey.txt");
    private static final File THEworldFolder = new File("./world");
    private static final File defaultServerProps = new File("./saved-worlds/default-server.properties");
    private static final File THEserverPropsFile = new File("./server.properties");
    private static final TreeMap<String, File> savedWorlds = new TreeMap<String, File>(); //all keys are stripped w/ original capitalization, value is relative path of its world folder
    private static String currentWorldKey;
    private static File currentWorldFolder;
    private static String selectedWorldKey;
    private static boolean createdNewWorld = false;
    private static boolean useDefaultProps = false;
    public static void main(String[] args) {
        // create saved-worlds folder if it does not exist
        savedWorldsFolder.mkdir();
        // create txt file containing the current worlds key
        try {
            currentWorldKeyTxt.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create currentWorldKey.txt file. Exiting program.");
            return;
        }
        // makes hashmap of all worlds found in "saved-worlds" folder
        for (File savedWorld : savedWorldsFolder.listFiles()) {
            if (savedWorld.isDirectory()) {
                savedWorlds.put(savedWorld.getName(), savedWorld);
            }
        }
        //intialize system scanner
        Scanner sysScan = new Scanner(System.in);
        // checks if program has been run before
        if (savedWorlds.isEmpty()) {
            System.out.println("Running first use setup.\nWhat would you like to save your current world as?");
            String name = sysScan.nextLine().strip();
            while (name.isBlank()) {
                System.out.println("World name cannot be empty. Enter a different one.");
                name = sysScan.nextLine().strip();
            }
            File newWorld = new File(savedWorldsFolder.getPath()+"/"+name);
            newWorld.mkdir();
            savedWorlds.put(name, newWorld);
            currentWorldKey = name;
            System.out.println("Current world name set to: " + name);
        } else {
            // create scanner and store the key
            try {
                Scanner keyScan = new Scanner(currentWorldKeyTxt);
                currentWorldKey = keyScan.nextLine();
                keyScan.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Program could not read currentWorldKey.txt file. Exiting program.");
                sysScan.close();
                return;
            }
        }
        // store current world folder
        currentWorldFolder = savedWorlds.get(currentWorldKey);
        if (currentWorldFolder == null) {
            System.out.println("Could not find current world folder. Check the currentWorldKey.txt file to make sure it has a corresponding folder in saved-worlds. Exiting program.");
            sysScan.close();
            return;
        }
        // asks what world to load / create new one
        byte numWorlds = 0;
        System.out.println("Which world would you like to load?");
        System.out.println("0: Create New World");
        for (String worldName : savedWorlds.keySet()) {
            System.out.println((++numWorlds)+": " + worldName);
        }
        //last place
        int selection = sysScan.nextInt();
        // choose world to load or create new world
        while (!(selection <= numWorlds && selection >= 0)) {
            System.out.println("That is not a valid option. Enter one of the listed choices above.");
            selection = sysScan.nextInt();
        }
        // if creating new world
        if (selection == 0) {
            createdNewWorld = true;
            useDefaultProps = true;
            System.out.println("Enter new world name:");
            sysScan.nextLine(); // scan nextline because when you scan ints with a scanner before lines, it preloads an empty line or smth
            String newWorldName = sysScan.nextLine().strip();
            while (newWorldName.isBlank() || savedWorlds.containsKey(newWorldName)) {
                System.out.println("Name cannot be blank or already used. Enter a different one.");
                newWorldName = sysScan.nextLine().strip();
            }
            File newWorldFolder = new File(savedWorldsFolder.getPath()+"/"+newWorldName);
            newWorldFolder.mkdir();
            savedWorlds.put(newWorldName, newWorldFolder);
            selectedWorldKey = newWorldName;
        } else {
            selectedWorldKey = (String) savedWorlds.keySet().toArray()[--selection];
        }
        sysScan.close();
        // move current world to its folder
        try {
            Files.move(THEworldFolder.toPath(), currentWorldFolder.toPath().resolve("world"), StandardCopyOption.ATOMIC_MOVE);
            Files.move(THEserverPropsFile.toPath(), currentWorldFolder.toPath().resolve("server.properties"), StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exiting program.");
            return;
        }
        // move selected world out
        if (!createdNewWorld) { // creating a new world dont need to move anything out
            try {
                Files.move(savedWorlds.get(selectedWorldKey).toPath().resolve("world"), THEworldFolder.toPath(), StandardCopyOption.ATOMIC_MOVE);
                Path selectedWorldProps = savedWorlds.get(selectedWorldKey).toPath().resolve("server.properties");
                if (Files.exists(selectedWorldProps, LinkOption.NOFOLLOW_LINKS)) {
                    Files.move(selectedWorldProps, THEserverPropsFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                } else { // if server props file was deleted, enable boolea to copy over default-server.properties
                    useDefaultProps = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exiting program.");
                return;
            }
        }
        // create copy of default-server.properties if needed
        if (useDefaultProps) {
            try {
                Files.copy(defaultServerProps.toPath(), THEserverPropsFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                File copiedServerPropFile = new File("./default-server.properties");
                copiedServerPropFile.renameTo(THEserverPropsFile);
                if (!THEserverPropsFile.exists()) { // check manually because renameTo() method returns false even when it works for some reason
                    System.out.println("Program could not rename new server.properties file. Program will still change currentWorldKey.txt, but make sure to rename the 'default-server.properties' file (NOT IN THE saved-worlds FOLDER) to 'server.properties'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Program could not copy default-server.properties file. Exiting program.");
            }
        }
        // save new current key in currentWorldKey.txt
        try {
            FileWriter fileWriter = new FileWriter(currentWorldKeyTxt);
            fileWriter.write(selectedWorldKey);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Program could not write new key to 'currentWorldKey.txt'. Exiting program.");
            return;
        }
        //end program
    }
}