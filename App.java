import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Scanner;

public class App {
    private static final File savedWorldsFolder = new File("./saved-worlds");
    private static final File currentWorldKeyTxt = new File("./saved-worlds/currentWorldKey.txt");
    private static final File THEworldFolder = new File("./world");
    private static final HashMap<String, File> savedWorlds = new HashMap<String, File>(); //all keys are stripped w/ original capitalization, value is relative path of its world folder
    private static String currentWorldKey;
    private static File currentWorldFolder;
    private static boolean createdNewWorld = false;
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
        Scanner scan = new Scanner(System.in);
        // checks if program has been run before
        if (savedWorlds.isEmpty()) {
            System.out.println("Running first use setup.\nWhat would you like to save your current world as?");
            String name = scan.nextLine().strip();
            while (name.equalsIgnoreCase("new")) {
                System.out.println("World name cannot be 'new'. Enter a different one.");
                name = scan.nextLine().strip();
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
                scan.close();
                return;
            }
        }
        // store current world folder
        currentWorldFolder = savedWorlds.get(currentWorldKey);
        if (currentWorldFolder == null) {
            System.out.println("Could not find current world folder. Check the currentWorldKey.txt file to make sure it has a corresponding folder in saved-worlds. Exiting program.");
            scan.close();
            return;
        }
        // asks what world to load / create new one
        System.out.println("Which world would you like to load?\n" + savedWorlds.keySet() + " or 'new'");
        String selection = scan.nextLine().strip();
        // choose world to load or create new world
        while (!(savedWorlds.containsKey(selection) || selection.equalsIgnoreCase("new"))) {
            System.out.println("That is not a valid option. Enter one of the listed choices above with exact spacing/capitalization.");
            selection = scan.nextLine().strip();
        }
        // if creating new world
        if (selection.equals("new")) {
            createdNewWorld = true;
            System.out.println("Enter new world name:");
            selection = scan.nextLine().strip();
            while (selection.isBlank() || savedWorlds.containsKey(selection) || selection.equalsIgnoreCase("new")) {
                System.out.println("Name cannot be blank, already used, or 'new'. Enter a different one.");
                selection = scan.nextLine().strip();
            }
            File newWorldFolder = new File(savedWorldsFolder.getPath()+"/"+selection);
            newWorldFolder.mkdir();
            savedWorlds.put(selection, newWorldFolder);
        }
        // move current world to its folder
        try {
            Files.move(THEworldFolder.toPath(), currentWorldFolder.toPath().resolve("world"), StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exiting program.");
            scan.close();
            return;
        }
        // move selected world out
        if (!createdNewWorld) { // creating a new world dont need to move anything out
            try {
                Files.move(savedWorlds.get(selection).toPath().resolve("world"), THEworldFolder.toPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exiting program.");
                scan.close();
                return;
            }
        }
        // save new current key in currentWorldKey.txt
        try {
            FileWriter fileWriter = new FileWriter(currentWorldKeyTxt);
            fileWriter.write(selection);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Program could not write new key to 'currentWorldKey.txt'. Exiting program.");
            scan.close();
            return;
        }
        //end program
        scan.close();
    }
}