package me.camm.productions.bedwars;

import me.camm.productions.bedwars.Arena.GameRunning.Arena;
import me.camm.productions.bedwars.Arena.GameRunning.Commands.CommandKeyword;
import me.camm.productions.bedwars.Arena.GameRunning.Commands.GameIntializer;
import me.camm.productions.bedwars.Arena.GameRunning.GameRunner;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.Managers.HotbarManager;
import me.camm.productions.bedwars.Arena.Players.Managers.PlayerInventoryManager;
import me.camm.productions.bedwars.Entities.ActiveEntities.GameDragon;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Listeners.PacketHandler;
import me.camm.productions.bedwars.Files.FileManager;
import me.camm.productions.bedwars.Files.GameFileWriter;
import me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.QuickBuyInventory;
import me.camm.productions.bedwars.Util.DataSets.ShopItemSet;
import me.camm.productions.bedwars.Util.Helpers.ChatSender;
import me.camm.productions.bedwars.Util.Helpers.StringHelper;
import me.camm.productions.bedwars.Validation.BedWarsException;
import me.camm.productions.bedwars.Validation.RegistrationException;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

public final class BedWars extends JavaPlugin
{
    private GameIntializer initialization;
    private final String DRAGON_NAME = "EnderDragon";
    private final int DRAGON_ID = 63;
    private final Class<? extends Entity> gameDragon = GameDragon.class;
    private final Class<? extends Entity> dragon = EntityEnderDragon.class;

    private static Plugin plugin;

    public BedWars(){
        plugin = this;
    }

    public static Plugin getPlugin(){
        return plugin;
    }

    @Override
    public void onEnable()
    {

        ChatSender sender = ChatSender.getInstance();
        //we init it right away so that we can use it anywhere.


        sender.sendConsoleMessage("Starting up!",Level.INFO);
        sender.sendConsoleMessage("It is recommended that you make a backup of this world as the game may destroy and change it.",Level.INFO);

       try {
           FileManager fileManager = new FileManager();
           fileManager.createFiles();

           replaceClass(gameDragon, DRAGON_NAME, DRAGON_ID);
           sender.sendConsoleMessage("Registered custom ender dragon...", Level.INFO);

           initialization = new GameIntializer(this);

           for (CommandKeyword word : CommandKeyword.values()) {
               getCommand(word.getWord()).setExecutor(initialization);
           }


       }
       catch (Exception e) {
           sender.sendConsoleMessage(e.getMessage(), Level.WARNING);
       }
    }







    @SuppressWarnings("unchecked")
    public void replaceClass(Class<? extends Entity> replace, String name, int id) throws BedWarsException {


        try {
            Field c = EntityTypes.class.getDeclaredField("c");
            c.setAccessible(true);
            Map<String, Class<? extends Entity>> map = (Map<String, Class<? extends Entity>>) c.get(EntityTypes.class);
            map.remove(name);


            Field e = EntityTypes.class.getDeclaredField("e");
            e.setAccessible(true);
            Map<Integer, Class<? extends Entity>> eMap = (Map<Integer, Class<? extends Entity>>) e.get(EntityTypes.class);
            eMap.remove(id);

            Method aMethod = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            aMethod.setAccessible(true);
            aMethod.invoke(EntityTypes.class, replace, name, id);
        }
        catch (Exception e) {
         throw new RegistrationException("Could not replace "+replace.getSimpleName()+"");
        }

    }



    @Override
    public void onDisable()
    {
        ChatSender sender = ChatSender.getInstance();
        try {
          replaceClass(dragon, DRAGON_NAME,DRAGON_ID);
        }
        catch (BedWarsException e)
        {
            sender.sendConsoleMessage("Failed to unregister Ender Dragon Override.", Level.WARNING);
        }
        

        if (initialization==null)
            return;

        if (initialization.getArena() == null)
            return;


        if (initialization.getRunner()==null)
            return;

        GameRunner runner = initialization.getRunner();

        runner.setRunning(false);
        initialization.getArena().getTeams().forEach((string, team) -> {
            if (team!=null&&team.getForge()!=null)
                team.getForge().disableForge();
        });
      PacketHandler handler = runner.getPacketHandler();

      if (handler!=null) {
          for (Player player : Bukkit.getOnlinePlayers())
              handler.removePlayer(player);
      }

      if (runner.getLoader() != null)
            runner.getLoader().stop();

        World world = initialization.getArena().getWorld();
        Scoreboard initial = ((CraftWorld)world).getHandle().getScoreboard();
        Collection<ScoreboardObjective> objectives = initial.getObjectives();
        for (ScoreboardObjective objective: objectives)
        {
            for (Player player: Bukkit.getOnlinePlayers()) {
                try {
                    initial.unregisterObjective(objective);
                    initial.handleObjectiveRemoved(objective);
                    send(new PacketPlayOutScoreboardObjective(objective, 1), player);
                } catch (IllegalArgumentException | IllegalStateException e) {
                 sender.sendConsoleMessage("Error occurred trying to unregister objective "+objective.getName()+" for "+player.getName(), Level.WARNING);
                }
            }
        }


        Arena arena = initialization.getArena();
        writeToFiles(arena);

        //Save all the players things here and put them into their files.
    }

    private void send(Packet<?> packet, Player player)
    {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }


    private void writeToFiles(Arena arena) {
        Collection<BattlePlayer> registered = arena.getPlayers().values();

        //writing to bar file
        registered.forEach(battlePlayer -> {

            HotbarManager barManager = battlePlayer.getBarManager();
            if (barManager != null) {
                ItemCategory[] barItems = barManager.getLayout();

                GameFileWriter barWriter = new GameFileWriter(StringHelper.getHotBarPath(battlePlayer.getRawPlayer()), this);
                barWriter.clear();
                ArrayList<String> valueList = new ArrayList<>();

                Arrays.stream(barItems).forEach(item -> valueList.add(
                        item == null ? null : item.toString()));
                barWriter.write(valueList, false);
            }


            //writing to shop file
            PlayerInventoryManager invManager = battlePlayer.getShopManager();
            if (invManager!=null) {
                QuickBuyInventory playerShop = invManager.getQuickBuy();
                ArrayList<ShopItemSet> shopSet = playerShop.packageInventory();

                GameFileWriter shopWriter = new GameFileWriter(StringHelper.getInventoryPath(battlePlayer.getRawPlayer()), this);
                shopWriter.clear();
                ArrayList<String> shopList = new ArrayList<>();
                shopSet.forEach(pack -> shopList.add(pack == null ? ShopItem.EMPTY_SLOT.name() : pack.toString()));
                shopWriter.write(shopList, false);
            }
        });
    }
}
