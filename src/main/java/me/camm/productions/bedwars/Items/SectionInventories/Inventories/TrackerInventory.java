package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.Managers.PlayerTrackerManager;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.ResourceConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class TrackerInventory extends InventoryOptionable {

    private static final ItemStack ITEM = ItemHelper.toBarItem(ItemCategory.TRACKER);
    private PlayerTrackerManager manager;
    private static final ShopItem TRACKER = ShopItem.TRACKER_ITEM;

    public TrackerInventory(Arena arena) {
        super(arena, InventoryName.TRACKER.getTitle());
    }


    public void setManager(PlayerTrackerManager manager) {
        this.manager = manager;
    }


    @Override
    protected void handleResult(BattlePlayer battlePlayer, BattleTeam preference) {


        boolean allBroken = true;
        for (BattleTeam team: arena.getTeams().values()) {


            if (team.equals(battlePlayer.getTeam()))
                continue;


            if (team.doesBedExist())
            {
                allBroken = false;
                break;
            }
        }

        if (!allBroken) {
            battlePlayer.sendMessage(ChatColor.RED+"Trackers can only be purchased when all enemy beds are broken!");
            return;
        }


        if (manager == null)
            return;

        //the tracker item
        if (!battlePlayer.getRawPlayer().getInventory().contains(ITEM)) {
            return;
        }

        if (battlePlayer.getTracking() != null)
            return;

        boolean paid = ItemHelper.didPay(battlePlayer, ShopItem.TRACKER_ITEM.costMaterial,TRACKER.cost);

        if (paid) {
            battlePlayer.setTracking(preference);
            manager.addEntry(battlePlayer);
            //then we start tracking the team here.
        }
    }


    @Override
    protected void handleResult(BattlePlayer clicked, ResourceConfig config) {
        throw new UnsupportedOperationException();
    }
}
