import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.TreeMap;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class App {
    private static final File savedWorldsFolder = new File("./saved-worlds");
    private static final File currentWorldKeyTxt = new File("./saved-worlds/currentWorldKey.txt");
    private static final File THEworldFolder = new File("./world");
    private static final File defaultServerProps = new File("./saved-worlds/default-server.properties");
    private static final File THEserverPropsFile = new File("./server.properties");
    private static final File THEserverIconFile = new File("./server-icon.png");
    private static final File THEwhitelistFile = new File("./whitelist.json");
    private static final TreeMap<String, File> savedWorlds = new TreeMap<String, File>(); //all keys are stripped w/ original capitalization, value is relative path of its world folder
    private static String currentWorldKey;
    private static File currentWorldFolder;
    private static String selectedWorldKey;
    private static boolean createdNewWorld = false;
    private static boolean useDefaultProps = false;
    private static Scanner sysScan;
    
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("save")) {
            if (!initFiles()) return;
            createSavedWorldsHashMap();
            if (savedWorlds.isEmpty()) return;
            if (!getCurrentWorldKey()) return;
            if (!getCurrentWorldFolder()) return;
            if (!moveWorldIntoSaved()) return;
            selectedWorldKey = "";
            storeSelectedWorldKey();
            return;
        }
        if (!initFiles()) return;
        // makes hashmap of all worlds found in "saved-worlds" folder
        createSavedWorldsHashMap();
        //intialize system scanner
        sysScan = new Scanner(System.in);
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
            if (!getCurrentWorldKey()) return;
        }
        // store current world folder
        if (!getCurrentWorldFolder()) return;
        // asks what world to load / create new one
        byte numWorlds = 0;
        System.out.println("Which world would you like to load?");
        System.out.println("0: Create New World");
        for (String worldName : savedWorlds.keySet()) {
            System.out.println((++numWorlds)+": " + worldName);
        }
        //last place
        int selection = -1;
        try {
            selection = sysScan.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Enter just the number of your selection. Not the name.");
        }
        // choose world to load or create new world
        while (!(selection <= numWorlds && selection >= 0)) {
            System.out.println("That is not a valid option. Enter one of the listed choices above.");
            try {
                selection = sysScan.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Enter just the number of your selection. Not the name.");
            }
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
            File keepFile = new File(newWorldFolder.getPath()+"/.keep");
            try {
                keepFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Program could not create .keep file in new folder. Exiting program.");
                sysScan.close();
                return;
            }
            savedWorlds.put(newWorldName, newWorldFolder);
            selectedWorldKey = newWorldName;
        } else {
            selectedWorldKey = (String) savedWorlds.keySet().toArray()[--selection];
        }
        sysScan.close();
        // move current world to its folder
        if (!moveWorldIntoSaved()) return;
        // move selected world out
        if (!createdNewWorld) { // creating a new world dont need to move anything out
            try {
                File savedWorld = savedWorlds.get(selectedWorldKey);
                // move world folder
                Files.move(savedWorld.toPath().resolve("world"), THEworldFolder.toPath(), StandardCopyOption.ATOMIC_MOVE);
                // move server properties file
                Path selectedWorldProps = savedWorld.toPath().resolve("server.properties");
                if (Files.exists(selectedWorldProps, LinkOption.NOFOLLOW_LINKS)) {
                    Files.move(selectedWorldProps, THEserverPropsFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                } else { // if server props file was deleted, enable boolean to copy over default-server.properties
                    useDefaultProps = true;
                }
                // if server icon exists, move that too
                File serverIcon = savedWorld.toPath().resolve("server-icon.png").toFile();
                if (serverIcon.exists()) {
                    Files.move(serverIcon.toPath(), THEserverIconFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                }
                // if whitelist file exists, move that too
                File whitelistFile = savedWorld.toPath().resolve("whitelist.json").toFile();
                if (whitelistFile.exists()) {
                    Files.move(whitelistFile.toPath(), THEwhitelistFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
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
        if (!storeSelectedWorldKey()) return;
        //end program
    }

    private static boolean initFiles() {
        // create saved-worlds folder and default-server.properties file if it does not exist
        savedWorldsFolder.mkdir();
        if (!defaultServerProps.exists()) {
            try {
                Files.copy(THEserverPropsFile.toPath(), defaultServerProps.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Unable to create default-server.properties file because current server.properties file does not exist. Create a world normally before using this program. Exiting program.");
                return false;
            }
            try {
                File newDefaultServerPropFile = new File("./saved-worlds/server.properties");
                newDefaultServerPropFile.renameTo(defaultServerProps);
                if (!defaultServerProps.exists()) {
                    System.out.println("Program was not able to rename new default-server.properties file in 'saved-worlds' from 'server.properties' to 'default-server.properties'. Exiting program.");
                }
            } catch (Exception e) {
                System.out.println("Program was not able to rename new default-server.properties file in 'saved-worlds' from 'server.properties' to 'default-server.properties'. Exiting program.");
                return false;
            }
        }
        // create txt file containing the current worlds key
        try {
            currentWorldKeyTxt.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create currentWorldKey.txt file. Exiting program.");
            return false;
        }
        return true;
    }

    private static void createSavedWorldsHashMap() {
        for (File savedWorld : savedWorldsFolder.listFiles()) {
            if (savedWorld.isDirectory()) {
                savedWorlds.put(savedWorld.getName(), savedWorld);
            }
        }
    }

    private static boolean getCurrentWorldKey() {
        // create scanner and store the key
        try {
            Scanner keyScan = new Scanner(currentWorldKeyTxt);
            currentWorldKey = keyScan.nextLine();
            keyScan.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Program could not read currentWorldKey.txt file. Exiting program.");
            sysScan.close();
            return false;
        } catch (NoSuchElementException e) {
            currentWorldKey = "";
        }
        return true;
    }

    private static boolean getCurrentWorldFolder() {
        if (currentWorldKey.isEmpty()) {
            return true;
        }
        currentWorldFolder = savedWorlds.get(currentWorldKey);
        if (currentWorldFolder == null) {
            System.out.println("Could not find current world folder. Check the currentWorldKey.txt file to make sure it has a corresponding folder in saved-worlds. Exiting program.");
            sysScan.close();
            return false;
        }
        return true;
    }

    private static boolean moveWorldIntoSaved() {
        if (currentWorldKey.isBlank()) {
            return true;
        }
        try {
            // move world folder
            Files.move(THEworldFolder.toPath(), currentWorldFolder.toPath().resolve("world"), StandardCopyOption.ATOMIC_MOVE);
            // move server properties
            Files.move(THEserverPropsFile.toPath(), currentWorldFolder.toPath().resolve("server.properties"), StandardCopyOption.ATOMIC_MOVE);
            // if server icon exists move it
            if (THEserverIconFile.exists()) {
                Files.move(THEserverIconFile.toPath(), currentWorldFolder.toPath().resolve("server-icon.png"), StandardCopyOption.ATOMIC_MOVE);
            }
            // if whitelist file exists move it
            if (THEwhitelistFile.exists()) {
                Files.move(THEwhitelistFile.toPath(), currentWorldFolder.toPath().resolve("whitelist.json"), StandardCopyOption.ATOMIC_MOVE);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exiting program.");
            return false;
        }
    }

    private static boolean storeSelectedWorldKey() {
        try {
            FileWriter fileWriter = new FileWriter(currentWorldKeyTxt);
            fileWriter.write(selectedWorldKey);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Program could not write new key to 'currentWorldKey.txt'. Exiting program.");
            return false;
        }
        return true;
    }
}