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



import java.util.HashMap;

import java.util.Map;
import java.util.UUID;


public class ItemListener implements Listener
{
    private final Arena arena;
    private final HashMap<String, Forge> forges;

    public ItemListener(Arena arena)
    {
        this.arena = arena;
        forges = new HashMap<>();

        for (BattleTeam team: arena.getTeams().values()) {
            Forge forge = team.getForge();
            forges.put(forge.getId().toString(),forge);
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


        if (!players.containsKey(id))
            return;

        BattlePlayer player = players.get(id);

            if (!player.getIsAlive() || player.getIsEliminated())
            {
                event.setCancelled(true);
                return;
            }

            String name = event.getItem().getName();
            ItemStack picked = event.getItem().getItemStack().clone();


            if (ItemHelper.isItemInvalid(picked))
                return;

            TieredItem tiered = ItemHelper.isTieredItem(ItemHelper.getAssociate(picked));


            if (tiered != null)
            {
                if (ItemHelper.isSword(tiered.getItem())) {
                    ItemHelper.clearAll(ShopItem.WOODEN_SWORD.sellMaterial,player.getRawPlayer().getInventory());
                }

            }
            else if (ItemHelper.isCurrencyItem(picked))
            {

                Location loc = player.getTeam().getForge().getForgeLocation();
                double distanceSquared = player.getTeam().getForge().getDistance();
                distanceSquared *= distanceSquared;

                Item pickup = event.getItem();
                if (!pickup.hasMetadata(Forge.getKeyword()))
                    return;

                Forge forge = null;
                if (name != null) {
                    forge = forges.getOrDefault(name, null);
                }

                if (forge != null)
                    forge.updateChildren(picked.getType(),picked.getAmount());

                for (BattlePlayer current : player.getTeam().getPlayers().values()) {
                    if (!(current.getTeam().equals(player.getTeam()) && !current.equals(player))) {
                    continue;
                    }

                    if (current.getRawPlayer().getLocation().distanceSquared(loc) > distanceSquared)
                        continue;

                    if (current.getRawPlayer().getInventory().firstEmpty() != -1 &&
                                (current.getIsAlive() && !current.getIsEliminated()))
                            current.getRawPlayer().getInventory().addItem(picked);


                }
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

        if (!current.getIsAlive()) {
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
