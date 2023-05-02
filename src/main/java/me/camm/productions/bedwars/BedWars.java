package me.camm.productions.bedwars;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Game.Commands.CommandKeyword;
import me.camm.productions.bedwars.Arena.Game.GameIntializer;
import me.camm.productions.bedwars.Arena.Game.GameRunner;
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
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
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

    public static Plugin getInstance(){
        return plugin;
    }

    @Override
    public void onEnable()
    {
        ChatSender sender = ChatSender.getInstance();

       try {

         FileManager.createFiles();

           replaceClass(gameDragon, DRAGON_NAME, DRAGON_ID);

           initialization = new GameIntializer(this);

           for (CommandKeyword word : CommandKeyword.values()) {
               getCommand(word.getWord()).setExecutor(initialization);
           }


       }
       catch (Exception e) {
           sender.sendConsoleMessage(e.getMessage(), Level.WARNING);
           e.printStackTrace();
       }
    }







    @SuppressWarnings("unchecked")
    public void replaceClass(Class<? extends Entity> replace, String name, int id) throws Exception {

            ChatSender sender = ChatSender.getInstance();
            Field entityMap = EntityTypes.class.getDeclaredField("c");
            entityMap.setAccessible(true);
            Map<String, Class<? extends Entity>> map = (Map<String, Class<? extends Entity>>) entityMap.get(EntityTypes.class);
            map.remove(name);

            Field EntityIDMap = EntityTypes.class.getDeclaredField("e");
            EntityIDMap.setAccessible(true);
            Map<Integer, Class<? extends Entity>> eMap = (Map<Integer, Class<? extends Entity>>) EntityIDMap.get(EntityTypes.class);
            eMap.remove(id);

            Method methodRegisterEntity = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            methodRegisterEntity.setAccessible(true);
            methodRegisterEntity.invoke(EntityTypes.class, replace, name, id);
            sender.sendConsoleMessage("Replaced entity definition "+name+" with class "+replace.getName(), Level.INFO);

    }



    @Override
    public void onDisable()
    {
        ChatSender sender = ChatSender.getInstance();
        try {
            replaceClass(dragon, DRAGON_NAME, DRAGON_ID);

            if (initialization == null)
                return;

            if (initialization.getArena() == null)
                return;

            if (initialization.getRunner() == null)
                return;

            GameRunner runner = initialization.getRunner();

            runner.setRunning(false);
            initialization.getArena().getTeams().forEach((string, team) -> {
                if (team != null && team.getForge() != null)
                    team.getForge().disableForge();
            });
            PacketHandler handler = runner.getPacketHandler();

            if (handler != null) {
                for (Player player : Bukkit.getOnlinePlayers())
                    handler.removePlayer(player);
            }

            if (runner.getLoader() != null)
                runner.getLoader().stop();

            World world = initialization.getArena().getWorld();
            Scoreboard initial = ((CraftWorld) world).getHandle().getScoreboard();
            Collection<ScoreboardObjective> objectives = initial.getObjectives();
            for (ScoreboardObjective objective : objectives) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        initial.unregisterObjective(objective);
                        initial.handleObjectiveRemoved(objective);
                        send(new PacketPlayOutScoreboardObjective(objective, 1), player);
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        sender.sendConsoleMessage("Error occurred trying to unregister objective " + objective.getName() + " for " + player.getName(), Level.WARNING);
                    }
                }
            }

            Arena arena = initialization.getArena();
            writeToFiles(arena);

            //Save all the players things here and put them into their files.
        }
        catch (Exception e) {
            sender.sendConsoleMessage(e.getMessage(),Level.WARNING);
        }
    }

    private void send(Packet<?> packet, Player player)
    {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }


    private void writeToFiles(Arena arena) throws IOException {
        Collection<BattlePlayer> registered = arena.getPlayers().values();
        ChatSender sender = ChatSender.getInstance();

        //writing to bar file
        for (BattlePlayer bp: registered) {

            File[] playerFiles = FileManager.getPlayerFiles(bp.getRawPlayer());
            HotbarManager barManager = bp.getBarManager();
            String name = bp.getRawPlayer().getName();
            if (barManager != null) {
                List<String> barEntries = barManager.getFileEntries();
               boolean success = FileManager.write(barEntries, false, playerFiles[1]);
               if (!success)
                   sender.sendConsoleMessage("Could not write hot bar manager data for player "
                           +name+". Skipping.",Level.WARNING);
            }

            //writing to shop file
            PlayerInventoryManager invManager = bp.getShopManager();
            if (invManager != null) {
                QuickBuyInventory playerShop = invManager.getQuickBuy();
                List<String> inventoryData = playerShop.getFileEntries();
               boolean success = FileManager.write(inventoryData, false, playerFiles[0]);

                if (!success)
                    sender.sendConsoleMessage("Could not write quick buy customization data for player "
                            +name+". Skipping.",Level.WARNING);

            }
        }
    }
}
