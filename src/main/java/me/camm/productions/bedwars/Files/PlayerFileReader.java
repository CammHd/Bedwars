package me.camm.productions.bedwars.Files;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.Managers.HotbarManager;
import me.camm.productions.bedwars.Arena.Players.Managers.PlayerInventoryManager;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory;
import me.camm.productions.bedwars.Util.DataSets.ShopItemSet;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.HOT_BAR_END;
import static me.camm.productions.bedwars.Util.Helpers.StringHelper.*;



/*
 * @author CAMM
 * Reader to read the info from the player config files
 */
public class PlayerFileReader
{
    private final Plugin plugin;
    private final boolean isInflated;

    private final File inventoryFile;
    private final File barFile;
    private final Arena arena;

    private final BattlePlayer player;


    public PlayerFileReader(Plugin plugin, BattlePlayer player, boolean isInflated, Arena arena)
    {
        this.plugin = plugin;
        this.isInflated = isInflated;
        this.arena = arena;
        this.player = player;

        this.inventoryFile = new File(getInventoryPath(player.getRawPlayer()));
        this.barFile = new File(getHotBarPath(player.getRawPlayer()));
    }

    /*
       Reads the bar config file and returns a manager.
     */
    public HotbarManager readBarFile()
    {
        HotbarManager manager;

        try (BufferedReader reader = new BufferedReader(new FileReader(barFile)))
        {
            ArrayList<String> lines = new ArrayList<>();
            String current = reader.readLine();

            while (current!=null)
            {
                current = checkForComments(current);
                lines.add(current);
                current = reader.readLine();
            }
            reader.close();

            ItemCategory[] set = new ItemCategory[HOT_BAR_END.getValue()];

            int currentSlot = 0;
            for (String string: lines)
            {

               try {
                   string = string.trim();
                   ItemCategory currentCategory = ItemCategory.valueOf(string);
                   if (currentCategory==ItemCategory.NONE||currentCategory==ItemCategory.NAV) {
                       set[currentSlot] = null;
                       currentSlot++;
                       continue;
                   }

                   set[currentSlot] = currentCategory;
                   currentSlot++;

                   if (currentSlot==set.length-1)
                       break;
               }
               catch (RuntimeException e)
               {
                   set[currentSlot] = null;
                   currentSlot++;
               }
            }
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
    public PlayerInventoryManager readInvFile()
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(inventoryFile)))
        {

            //deal with the possibility of restricted items in the manager.
            ArrayList<ShopItemSet> items = new ArrayList<>();
            String current = reader.readLine();


            while (current!=null)
            {
                //check for comments. returns null if entire line is a comment
                current = checkForComments(current);

                //if the entire line is a comment
                if (current==null) {
                   current = reader.readLine();
                    continue;
                }

                StringTokenizer tk = new StringTokenizer(current);
                String key;
                String slotNumber;

                //skip the key
                if (tk.hasMoreTokens())
                   key = tk.nextToken();
                else
                {
                    current = reader.readLine();
                    continue;
                }

                if (tk.hasMoreTokens())
                   slotNumber = tk.nextToken();
                else
                {
                    current = reader.readLine();
                    continue;
                }

               try
               {
                   int slot = Integer.parseInt(slotNumber);
                   ShopItem item = ShopItem.valueOf(key);
                   items.add(new ShopItemSet(item,slot));
               }
               catch (RuntimeException ignored)
               {

               }

                current = reader.readLine();
            }

            return new PlayerInventoryManager(items, isInflated, arena, this.player);
        }
        catch (IOException e)
        {
            return new PlayerInventoryManager(isInflated, arena, this.player);
        }

    }

}
