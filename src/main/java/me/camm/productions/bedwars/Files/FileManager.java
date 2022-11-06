package me.camm.productions.bedwars.Files;

import me.camm.productions.bedwars.Arena.Teams.TeamColor;
import me.camm.productions.bedwars.BedWars;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/*
 * @author CAMM
 * Class for creating folders in the server files for configuration
 */
public class FileManager
{
   public enum Files {

        GENERATORS("Generators.json"),
        INSTRUCTIONS("Instructions.txt"),
        TEAM_DATA("TeamData.json"),
        WORLD_DATA("WorldData.json"),
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

    private final Plugin plugin;
    private final String dataPath;

    private static final String INVENTORY = "Inventory.txt";
    private static final String HOTBAR = "Hotbar.txt";

    public FileManager()
    {
        plugin = BedWars.getPlugin();
        dataPath = plugin.getDataFolder().getParentFile().getAbsolutePath()+ "\\";
    }



    public static String getDataPath(){
        Plugin p = BedWars.getPlugin();
        return p.getDataFolder().getParentFile().getAbsolutePath()+ "\\" + Directories.DATA.data +"\\";
    }



    public static String getPlayerFolder(UUID id) {
        return getDataPath() + Directories.PLAYERS.data +"\\"+ id.toString();
    }




    public void createFiles() throws IOException, IllegalStateException
    {
        String interiorPath = dataPath + Directories.DATA.data;
        createDirectory(interiorPath);

        interiorPath += "\\";

       createDirectory(interiorPath + Directories.PLAYERS.data);

       createDirectory(interiorPath + Directories.TEAMS.data);

       for (Files f: Files.values()) {

          if (f == Files.TEAM_DATA)
            createTeamFiles(interiorPath);
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






    private void createTeamFiles(String original) throws IOException {
        String origin = original + Directories.TEAMS +"\\"+ Files.TEAM_DATA.data;

        for (TeamColor color: TeamColor.values()) {
            String path = original + color.getName() + ".json";

            File teamFile = new File(path);
            if (teamFile.exists())
                continue;

            File copied = copyResource(Files.TEAM_DATA.data, origin);
           boolean success = copied.renameTo(new File(path)) || copied.exists();

           if (!success)
               throw new IOException("Could not create file for team "+color.getName() +". Terminating.");

        }
    }



    private void createDirectory(String path) throws IOException {
        File main = new File(path);
        boolean isDirectory = main.mkdir() || main.isDirectory();
        boolean created = main.exists() || main.createNewFile();
        if (! (isDirectory && created) )
            throw new IllegalStateException("Could not create main folder");
    }




    private File copyResource(String resourceName, String pathName) throws IOException {
        InputStream stream = plugin.getResource(resourceName);

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


}
