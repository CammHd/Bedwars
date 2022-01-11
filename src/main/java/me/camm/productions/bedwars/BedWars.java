package me.camm.productions.bedwars;

import me.camm.productions.bedwars.Arena.GameRunning.Arena;
import me.camm.productions.bedwars.Arena.GameRunning.Commands.CommandKeyword;
import me.camm.productions.bedwars.Arena.GameRunning.Commands.SetUp;
import me.camm.productions.bedwars.Arena.GameRunning.GameRunner;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Entities.ActiveEntities.GameDragon;
import me.camm.productions.bedwars.Listeners.PacketHandler;
import me.camm.productions.bedwars.Files.FileCreators.DirectoryCreator;
import me.camm.productions.bedwars.Files.FileStreams.GameFileWriter;
import me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.QuickBuySection;
import me.camm.productions.bedwars.Util.DataSets.ItemSet;
import me.camm.productions.bedwars.Util.Helpers.StringToolBox;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public final class BedWars extends JavaPlugin
{
    private SetUp initialization;
    private final String name = "EnderDragon";
    private final int id = 63;

    @Override @SuppressWarnings("unchecked")
    public void onEnable()
    {
        sendMessage(ChatColor.GREEN+"[BEDWARS] - STARTING UP");
        sendMessage(ChatColor.AQUA+"[BEDWARS] - It is recommended that you make a backup of this world before starting the game, if you haven't done so.");
        DirectoryCreator fileCreator = new DirectoryCreator(this);

       if (!fileCreator.createFolders())
       {
           sendMessage(ChatColor.RED+"[BEDWARS] [ERROR]: FILES ARE NOT CONFIGURED. CANNOT CONTINUE UNTIL CONFIGURATION IS COMPLETE.");
           sendMessage(ChatColor.RED+"[BEDWARS] PLEASE RELOAD THE PLUGIN AFTER CONFIGURATION IS COMPLETE.");
       }
       else
       {

           try {
               Field c = EntityTypes.class.getDeclaredField("c");
               c.setAccessible(true);
               Map<String, Class<? extends Entity>> cMap = (Map<String, Class<? extends Entity>>) c.get(EntityTypes.class);
               cMap.remove(name);

               Field e = EntityTypes.class.getDeclaredField("e");
               e.setAccessible(true);
               Map<Integer, Class<? extends Entity>> eMap = (Map<Integer, Class<? extends Entity>>) e.get(EntityTypes.class);
               eMap.remove(id);

               Method aMethod = EntityTypes.class.getDeclaredMethod("a",Class.class,String.class,int.class);
               aMethod.setAccessible(true);
               aMethod.invoke(EntityTypes.class, GameDragon.class,name,id);

               initialization = new SetUp(this);
               getCommand(CommandKeyword.SETUP.getWord()).setExecutor(initialization);
               getCommand(CommandKeyword.REGISTER.getWord()).setExecutor(initialization);
               getCommand(CommandKeyword.START.getWord()).setExecutor(initialization);

           }
           catch (Exception e)
           {
               sendMessage(ChatColor.RED+"[BEDWARS]Attempted to register custom entities for EnderDragons. Failed. Game cannot proceed at this point.");
               e.printStackTrace();
           }
       }



    }

    @Override @SuppressWarnings("unchecked")
    public void onDisable()
    {
        try {
            Field c = EntityTypes.class.getDeclaredField("c");
            c.setAccessible(true);
            Map<String, Class<? extends Entity>> cMap = (Map<String, Class<? extends Entity>>) c.get(EntityTypes.class);
            cMap.remove(name);

            Field e = EntityTypes.class.getDeclaredField("e");
            e.setAccessible(true);
            Map<Integer, Class<? extends Entity>> eMap = (Map<Integer, Class<? extends Entity>>) e.get(EntityTypes.class);
            eMap.remove(id);

            Method aMethod = EntityTypes.class.getDeclaredMethod("a",Class.class,String.class,int.class);
            aMethod.setAccessible(true);
            aMethod.invoke(EntityTypes.class, EntityEnderDragon.class,name,id);

        }
        catch (Exception e)
        {
            sendMessage(ChatColor.RED+"[BEDWARS]Attempted to unregister custom entities for the EnderDragons. Failed.");
            e.printStackTrace();
        }



        if (initialization==null)
            return;

        if (initialization.getRunner()==null)
            return;

        GameRunner runner = initialization.getRunner();

        runner.setIsRunning(false);
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
                    e.printStackTrace();
                }
            }
        }


        Arena arena = initialization.getArena();
        if (arena == null)
            return;


            StringToolBox box = new StringToolBox(this);
            Collection<BattlePlayer> registered = arena.getPlayers().values();

        //writing to bar file
            registered.forEach(battlePlayer -> {
               ItemCategory[] barItems = battlePlayer.getBarManager().getLayout();
                GameFileWriter barWriter = new GameFileWriter(box.getHotBarPath(battlePlayer.getRawPlayer()),this);
                barWriter.clear();
                ArrayList<String> valueList = new ArrayList<>();

               Arrays.stream(barItems).forEach(item -> valueList.add(
                       item == null ? "" :item.toString()));
               String[] barList = valueList.toArray(new String[valueList.size()]);
               barWriter.write(barList,false);


               //writing to shop file
                QuickBuySection playerShop = battlePlayer.getShopManager().getQuickBuy();
               ArrayList<ItemSet> shopSet = playerShop.packageInventory(playerShop);

               GameFileWriter shopWriter = new GameFileWriter(box.getInventoryPath(battlePlayer.getRawPlayer()),this);
               ArrayList<String> shopList = new ArrayList<>();
               shopSet.forEach(pack -> shopList.add( pack == null ? "" :pack.toString()));
               String[] shopWriteList = shopList.toArray(new String[shopList.size()]);
               shopWriter.write(shopWriteList,false);

            });


        //Save all the players things here and put them into their files.
    }

    private void send(Packet<?> packet, Player player)
    {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    private void sendMessage(String message)
    {
        getServer().getConsoleSender().sendMessage(message);
    }

    /*
TODO: (In the very near future)
 - Inventories for quick buy. Create. [DONE]. Do the team inventories now. [Add the functionality later]
 - Bridge egg. [DONE] Please Integrate after enderpearls.
 - One of the events, the bed destroyed one does not play a title for certain players. Fix.
 - Do the research for overriding the EntityEnderDragon class [IN PROGRESS] - remember to register it!
 - Find a better solution to the explosives problem. [IN PROGRESS]
 - Do testing while the player is in spectator
 - haste has 2 levels, not 1. Apply that in Battleplayer class
 - also apply modifiers when the player relogs.
 - add tracker shop
 - add code to not activate traps if a player is not online
 - add code for dream defender and bedbug targeting and health


 NOTE: The check for opposition in the CLASS "Setup" is currently ----   ---- disabled for testing purposes.

 */



}
