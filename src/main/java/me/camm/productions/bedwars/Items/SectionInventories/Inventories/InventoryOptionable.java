package me.camm.productions.bedwars.Items.SectionInventories.Inventories;


import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;

import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.TeamColor;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.ResourceConfig;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamOptionConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;

import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/*
 * @author CAMM
 * This inventory models an inventory for players where they are given options for tracker, or
 * for quick chat, where they choose resources or teams for a message
 */
public abstract class InventoryOptionable extends CraftInventoryCustom implements IGameInventory
{

    protected final Map<String, BattleTeam> dictionary;
    protected static final int LENGTH = TeamColor.values().length;
    protected static final ItemStack AIR = new ItemStack(Material.AIR);
    protected static final int ROW = InventoryProperty.LARGE_ROW_TWO_START.getValue();




    protected final Arena arena;




    public InventoryOptionable(Arena arena, String title) {
        super(null, InventoryProperty.LARGE_SHOP_SIZE.getValue(), title);
      dictionary = new HashMap<>();
      this.arena = arena;


      for (TeamOptionConfig config: TeamOptionConfig.values()){
          for (int slot: config.getSlots()) {
              setItem(slot, config.create());
          }
      }
    }




    public void addEntry(@NotNull BattleTeam team){
        TeamColor color = team.getTeamColor();
        dictionary.put(color.format(), team);

    }

    public void addEntries(Collection<BattleTeam> teams, BattleTeam exclusion){
        for (BattleTeam next : teams) {
            if (next.equals(exclusion))
                continue;

            addEntry(next);
        }
    }


    public void removeEntry(@NotNull BattleTeam team){

        dictionary.remove(team.getTeamColor().getName());
        updateInventory();
    }

    public void updateInventory(){
        Iterator<BattleTeam> iter = dictionary.values().iterator();



        int slot = 1;

        //clear
        while (slot < LENGTH) {
            setItem(slot+ROW,AIR);
            slot++;
        }

        slot = 1;


        while (slot < LENGTH && iter.hasNext()) {



                BattleTeam team = iter.next();
                if (team.isEliminated()) {
                    continue;
                }

                TeamColor color = team.getTeamColor();

                ItemStack trackStack = ItemHelper.createWool((byte)color.getValue());
                  ItemMeta meta = trackStack.getItemMeta();
                  meta.setDisplayName(color.format());
                  trackStack.setItemMeta(meta);
                  setItem(slot+ROW, trackStack);

            slot++;
        }
    }


    @Override
    public boolean equals(Inventory other)
    {
        return super.equals(other);
    }

    @Override
    public void operate(InventoryClickEvent event) {

        Map<UUID, BattlePlayer> players = arena.getPlayers();
       BattlePlayer battlePlayer = players.getOrDefault(event.getWhoClicked().getUniqueId(), null);

       InventoryOperationHelper.handleDefaultRestrictions(event,arena);
       if (InventoryOperationHelper.triedToPlaceIn(event, this))
           event.setCancelled(true);



       ItemStack clicked = event.getCurrentItem();
       if (ItemHelper.isItemInvalid(clicked))
           return;



       if (!event.getClickedInventory().equals(this)) {
           return;
       }

        event.setCancelled(true);


       String name = clicked.getItemMeta().getDisplayName();

       if (name == null)
           return;


       BattleTeam preference = dictionary.getOrDefault(name, null);


        if (preference == null) {

            if (clicked.equals(TeamOptionConfig.RETURN.create())) {
                event.setCancelled(true);
                battlePlayer.getRawPlayer().openInventory((Inventory)arena.getSelectionInv());
            }
            return;
        }


        if (preference.isEliminated()) {
            removeEntry(preference);
            return;
        }


        handleResult(battlePlayer, preference);


    }

    @Override
    public void operate(InventoryDragEvent event) {

        if (InventoryOperationHelper.didTryToDragIn(event, this)) {
            event.setCancelled(true);
            return;
        }
        InventoryOperationHelper.handleDefaultRestrictions(event, arena);
    }

    protected abstract void handleResult(BattlePlayer player, BattleTeam preference);
    protected abstract void handleResult(BattlePlayer player, ResourceConfig config);



}
