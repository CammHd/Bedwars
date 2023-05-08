package me.camm.productions.bedwars.Listeners;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Generators.Forge;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Items.ItemDatabases.TieredItem;

import me.camm.productions.bedwars.Util.Helpers.BlockTagManager;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.Location;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;


import java.util.HashMap;

import java.util.Map;
import java.util.UUID;


public class ItemListener implements Listener
{
    private final Arena arena;
    private final HashMap<Integer, Forge> forges;

    public ItemListener(Arena arena)
    {
        this.arena = arena;
        forges = new HashMap<>();

        for (BattleTeam team: arena.getTeams().values()) {
            Forge forge = team.getForge();
            forges.put(forge.getId(),forge);
        }
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent event)
    {
        String forge = Forge.getKeyword();
        if (event.getEntity().hasMetadata(forge)||event.getTarget().hasMetadata(forge))
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event)
    {
        BlockTagManager manager = BlockTagManager.get();
        if (manager.isInbounds(event.getLocation().getBlock())) {
            event.setCancelled(true);
            Item item = event.getEntity();
            item.setTicksLived(1);
        }
    }

    @EventHandler
    public void onItemPickUp(PlayerPickupItemEvent event)
    {
        Map<UUID, BattlePlayer> players = arena.getPlayers();
        UUID id = event.getPlayer().getUniqueId();

        BattlePlayer initiator = players.getOrDefault(id,null);

        if (initiator == null)
            return;

        if (!initiator.isAlive() || initiator.isEliminated()) {
                event.setCancelled(true);
                return;
        }

            ItemStack picked = event.getItem().getItemStack();
            if (ItemHelper.isItemInvalid(picked))
                return;

            TieredItem tiered = ItemHelper.isTieredItem(ItemHelper.getAssociate(picked));

            if (tiered != null)
            {
                if (ItemHelper.isSword(tiered.getItem())) {
                    ItemHelper.clearAll(ShopItem.WOODEN_SWORD.sellMaterial,initiator.getRawPlayer().getInventory());
                    return;
                }
            }

            if (!ItemHelper.isCurrencyItem(picked)) {
                return;
            }

                Item pickup = event.getItem();
                if (!pickup.hasMetadata(Forge.getKeyword()))
                    return;

                Forge pickupForge = null;
                for (MetadataValue value: pickup.getMetadata(Forge.getKeyword())) {
                    if (!(value.value() instanceof Integer))
                        continue;
                    pickupForge = forges.getOrDefault((Integer)value.value(),null);
                }

                if (pickupForge == null) {
                    return;
                }

                if (picked.getType() == Material.IRON_INGOT) {
                    pickupForge.updateIronCount(picked.getAmount());
                }
                else if (picked.getType() == Material.GOLD_INGOT) {
                    pickupForge.updateGoldCount(picked.getAmount());
                }

            Location forgeLocation = initiator.getTeam().getForge().getForgeLocation();
            final double pickUpDistanceSquared = Math.pow(initiator.getTeam().getForge().getPickupDistance(),2);

                for (BattlePlayer teamMate : initiator.getTeam().getPlayers().values()) {
                    if (teamMate.equals(initiator))
                        continue;

                    Player rawPlayer = teamMate.getRawPlayer();

                    if (rawPlayer.getLocation().distanceSquared(forgeLocation) > pickUpDistanceSquared)
                        continue;

                    if (rawPlayer.getInventory().firstEmpty() != -1 &&
                                (teamMate.isAlive() && !teamMate.isEliminated()))
                            teamMate.getRawPlayer().getInventory().addItem(picked);
                }


    }



    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        Map<UUID, BattlePlayer> registered = arena.getPlayers();
        if (!registered.containsKey(player.getUniqueId()))
            return;

        Item dropped = event.getItemDrop();

        ItemStack stack = dropped.getItemStack();
        if (ItemHelper.isItemInvalid(stack))
            return;

        BattlePlayer current = registered.get(player.getUniqueId());

        if (!current.isAlive()) {
            event.setCancelled(true);
            return;
        }
        ShopItem item = ItemHelper.getAssociate(stack);

        if ( (ItemHelper.isAxe(stack) || ItemHelper.isPick(stack))) {
            event.setCancelled(true);
            return;
        }

        if (stack.getType() == Material.COMPASS || stack.getType() == Material.SHEARS) {
            event.setCancelled(true);
            return;
        }

        /*
        Navigators should not be dropped, but they also should not end up in the
        player inv.
         */
        if (ItemHelper.getNavigator(stack) != null) {

            event.getItemDrop().remove();
            return;
        }

        if (!ItemHelper.isSword(item))
            return;

        if (ItemHelper.isInventoryPlaceRestrict(stack)) {
            event.setCancelled(true);
            return;
        }

        InventoryOperationHelper.operateSwordCount(current);

    }
}
