package me.camm.productions.bedwars.Files;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.PlayerManagers.HotbarManager;
import me.camm.productions.bedwars.Game.PlayerManagers.PlayerInventoryManager;
import me.camm.productions.bedwars.Game.Teams.TeamColor;
import me.camm.productions.bedwars.BedWars;
import me.camm.productions.bedwars.Items.ItemProperties.ItemCategory;
import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;
import me.camm.productions.bedwars.Items.ShopItemSet;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.HOT_BAR_END;

/*
 * @author CAMM
 * Class for creating folders in the server files for configuration
 */
public class FileManager
{
   public enum Files {

        GENERATORS("Generators.json"),
        //INSTRUCTIONS("Instructions.txt"),
        TEAM_DATA("TeamData.json"),
        WORLD_DATA("WorldData.json"),

      // EVENTS("Events.json"),
        CREDITS("ContributorList.txt");

        final String data;

        Files (String data) {
            this.data = data;
        }
    }

   public enum Directories {
        DATA("BedWars Data"),
        TEAMS("Team Data"),
        PLAYERS("Player Data");

        final String data;

        Directories (String data) {
            this.data = data;
        }
    }


    private static final String INVENTORY = "Inventory.txt";
    private static final String HOTBAR = "Hotbar.txt";



    public static String getDataPath(){
        Plugin p = BedWars.getInstance();
        return p.getDataFolder().getParentFile().getAbsolutePath()+ "\\" + Directories.DATA.data;
    }

    public static String getTeamDataPath() {
        return getDataPath() + "\\" + Directories.TEAMS.data;
    }

    public static String getPlayerDataPath() {
        return getDataPath() + "\\" + Directories.PLAYERS.data;
    }

    public static String getPlayerFolder(UUID id) {
        return getPlayerDataPath() +"\\" + id.toString();
    }

    public static boolean clearFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("");
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    //writes to the file.
    public static boolean write(List<String> lines, boolean append, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))){

            for (String s : lines) {
                writer.write(s + "\n");
            }
        } catch (IOException e) {

          return false;
        }
        return true;
    }




    public static void createFiles() throws IOException, IllegalStateException
    {
        String interiorPath = getDataPath() +"\\";

        createDirectory(interiorPath);
       createDirectory(getPlayerDataPath());
       createDirectory(getTeamDataPath());


       for (Files f: Files.values()) {

          if (f == Files.TEAM_DATA)
            createTeamFiles();
          else {
              boolean success = copyResource(f.data, interiorPath + f.data).exists();
              if (!success)
                  throw new IllegalStateException("Could not create file "+f.data+". Terminating.");
          }
       }
    }


    /*
    Creates the UUID directory and
    the directory, inventory and hotbar files for the player if they don't exist,
    returns references to them.
     */
    public static File[] getPlayerFiles(Player player) throws IOException {

        String dataFolder = getPlayerFolder(player.getUniqueId());
        File folder = new File(dataFolder);
        boolean success =  (folder.isDirectory() || folder.mkdir()) &&
                           (folder.exists() || folder.createNewFile());

        if (!success)
            throw new FileNotFoundException("Could not create folder for player "+player.getName());

        File inventory = new File(dataFolder +"\\"+ INVENTORY);
        File hotbar = new File(dataFolder +"\\"+ HOTBAR);

        boolean invExists = inventory.exists() || inventory.createNewFile();
        boolean hotbarExists = hotbar.exists() || hotbar.createNewFile();

        if (!invExists)
            throw new FileNotFoundException("Could not create inventory file for "+player.getName());

        if (!hotbarExists)
            throw new FileNotFoundException("Could not create hotbar file for "+player.getName());

        return new File[]{inventory, hotbar};

    }






    private static void createTeamFiles() throws IOException {

        String dirPath = getTeamDataPath() +"\\";



        for (TeamColor color: TeamColor.values()) {
            String path = dirPath + color.getName() + ".json";

            File teamFile = new File(path);
            if (teamFile.exists())
                continue;

            File copied = copyResource(Files.TEAM_DATA.data, path + Files.TEAM_DATA.data);
           boolean success = copied.renameTo(new File(path)) || copied.exists();

           if (!success)
               throw new IOException("Could not create file for team "+color.getName() +". Terminating.");

        }
    }



    private static void createDirectory(String path) throws IOException {
        File main = new File(path);
        boolean isDirectory = main.mkdir() || main.isDirectory();
        boolean created = main.exists() || main.createNewFile();
        if (! (isDirectory && created) )
            throw new IllegalStateException("Could not create main folder");
    }




    private static File copyResource(String resourceName, String pathName) throws IOException {
        InputStream stream = BedWars.getInstance().getResource(resourceName);

        if (stream == null) {
            throw new NoSuchFileException("File "+resourceName+" not found");
        }

        Path filePath = Path.of(pathName);
        File file = new File(filePath.toString());

        try {
            java.nio.file.Files.copy(stream, filePath);
        }
        catch (FileAlreadyExistsException ignored) {
        }

      return file;

    }



    public static HotbarManager readBarFile(File barFile, Player player, Arena arena)
    {
        HotbarManager manager;
        ChatSender sender = ChatSender.getInstance();

        try (BufferedReader reader = new BufferedReader(new FileReader(barFile)))
        {

            ItemCategory[] set = new ItemCategory[HOT_BAR_END.getValue()];
            BarContext context = new BarContext();
            int line = 0;
            String current = reader.readLine();

            while (current!=null && line < set.length)
            {
                line ++;
                try {
                    ItemCategory category = context.process(current);
                    set[line-1] = category;
                }
                catch (IllegalArgumentException e) {
                    sender.sendConsoleMessage(e.getMessage() +" at line "+(line-1) +" in " +
                            ""+player.getName()+"'s hot bar file", Level.INFO);
                    set[line-1] = null;
                }
                current = reader.readLine();
            }
            reader.close();

            manager = new HotbarManager(set,arena);
            return manager;
        }
        catch (IOException e)
        {
            manager = new HotbarManager(arena);
            return manager;
        }
    }

    /*
    Reads the inventory file and returns a manager for this
     */
    public static PlayerInventoryManager readInvFile(File inventoryFile, BattlePlayer player, boolean inflated, Arena arena)
    {
        ChatSender sender = ChatSender.getInstance();
        try (BufferedReader reader = new BufferedReader(new FileReader(inventoryFile)))
        {

            //deal with the possibility of restricted items in the manager.
            ArrayList<ShopItemSet> items = new ArrayList<>();
            String current = reader.readLine();
            int line = 0;
            InventoryContext context = new InventoryContext();

            while (current != null) {
                line ++;
                try {
                    ShopItemSet set = context.process(current);
                    items.add(set);
                }
                catch (IllegalArgumentException e) {
                    sender.sendConsoleMessage(e.getMessage()+" at line "+(line-1)+" in "+player.getRawPlayer().getName()+"'s inv file",Level.INFO);
                }
                current = reader.readLine();
            }

            return new PlayerInventoryManager(items, inflated, arena, player);
        }
        catch (IOException e)
        {
            return new PlayerInventoryManager(inflated, arena, player);
        }

    }



    static class InventoryContext{
        public ShopItemSet process(String string) throws IllegalArgumentException {
            StringTokenizer tk = new StringTokenizer(string);
            String key = null;
            String slotNumber;

            try {
                key = tk.nextToken();
                slotNumber = tk.nextToken();

                int slot = Integer.parseInt(slotNumber);
                if (slot < 0)
                    throw new IllegalArgumentException("Slot must be >= 0");

                ShopItem item = ShopItem.valueOf(key);
                return new ShopItemSet(item, slot);
            }
            catch (NoSuchElementException e) {
                throw new IllegalArgumentException("Incorrect string format "+string);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(key == null ? "Item not found":"Unknown item "+key);
            }
        }
    }

    static class BarContext {
        public @Nullable ItemCategory process(String string) throws IllegalArgumentException {
            try {
                string = string.trim();
                if (string.equals("null"))
                    return null;

                ItemCategory cat = ItemCategory.valueOf(string);
                if (cat==ItemCategory.NONE||cat==ItemCategory.NAV) {
                    return null;
                }
                return cat;
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown item category "+string);
            }
        }
    }






}
