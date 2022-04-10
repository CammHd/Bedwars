package me.camm.productions.bedwars.Listeners;

import me.camm.productions.bedwars.Arena.GameRunning.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Entities.ActiveEntities.DreamDefender;
import me.camm.productions.bedwars.Entities.ActiveEntities.ThrownFireball;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Util.Helpers.InventoryOperationHelper;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static me.camm.productions.bedwars.Util.Locations.BlockRegisterType.BASE;
import static me.camm.productions.bedwars.Util.Locations.BlockRegisterType.GENERATOR;


public class ItemUseListener implements Listener
{
    private final Plugin plugin;
    private final PacketHandler handler;
    private final EntityActionListener entityListener;


    private final HashMap<UUID, Long> coolDown;
    private final Arena arena;

    private static final int DELAY;
    private static final int DIVISION;

    private final static HashSet<UUID> messaged;

    static {
        DELAY = 500;
        DIVISION = 1000;
        messaged = new HashSet<>();
    }


    public ItemUseListener(Plugin plugin, Arena arena, PacketHandler handler, EntityActionListener entityListener)
    {
        this.plugin = plugin;
        this.coolDown = new HashMap<>();
        this.arena = arena;
        this.handler = handler;
        this.entityListener = entityListener;

    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event)
    {
        //For when a player drinks an invis potion.
        ItemStack stack = event.getItem();
        Player player = event.getPlayer();

        if (stack==null || stack.getType()==null || stack.getItemMeta() == null)
            return;

        if (!isRegistered(player))
            return;

        if (stack.getType()==Material.POTION) {

            //If it is an invis potion.

            //Unfinished. Use packets to see if the player should be set to visible
            //and also do it in the damage listener.
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            if (meta.hasCustomEffect(PotionEffectType.INVISIBILITY)) {
                BattlePlayer battlePlayer = arena.getPlayers().get(player.getUniqueId());
                battlePlayer.togglePotionInvisibility(true, handler);
            }

            new BukkitRunnable(){
                @Override
                public void run() {
                    ItemStack item = player.getItemInHand();
                    if (!ItemHelper.isItemInvalid(item) && item.getType()==Material.GLASS_BOTTLE)
                        player.setItemInHand(null);
                        cancel();
                }
            }.runTaskLater(plugin,1);
        }
        else if (stack.getType()==Material.MILK_BUCKET)
        {
            event.setCancelled(true);
            BattlePlayer battlePlayer = arena.getPlayers().get(player.getUniqueId());
            battlePlayer.setLastMilkTime(System.currentTimeMillis());

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack item = player.getItemInHand();
                    if (!ItemHelper.isItemInvalid(item) && (item.getType()==Material.BUCKET||item.getType()==Material.MILK_BUCKET))
                        player.setItemInHand(null);
                    cancel();
                }
            }.runTaskLater(plugin,1);

            new BukkitRunnable(){
                @Override
                public void run()
                {
                    if (System.currentTimeMillis()- battlePlayer.getLastMilk() > 30000)
                    battlePlayer.sendMessage(ChatColor.RED+"Your Magic Milk ran out!");
                    cancel();
                }
            }.runTaskLater(plugin,600);
        }
    }


    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {

        Map<UUID, BattlePlayer> players = arena.getPlayers();
        Player player = event.getPlayer();
        ItemStack stack = player.getItemInHand();
        Block block = event.getClickedBlock();

        if (!players.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        BattlePlayer currentPlayer = players.get(player.getUniqueId());

        if (block != null) {

            if (block.getType()==Material.CHEST) {

                for (BattleTeam team : arena.getTeams().values()) {
                    if (team.getChest().isBlock(arena.getWorld(), block)) {

                        if (currentPlayer.getIsEliminated()) {
                            event.setCancelled(true);
                            return;
                        }

                        if ((currentPlayer.getTeam().equals(team))) {
                            return;
                        }

                        if (team.isEliminated())
                            return;
                        else {
                            event.setCancelled(true);
                            player.sendMessage(team.getTeamColor().getChatColor() + team.getTeamColor().getName() + " is not eliminated yet!");
                        }
                        return;
                    }
                }
            }

            if (block.getType() == Material.WORKBENCH)
                event.setCancelled(true);
    }






        if (stack == null)
            return;

        Material mat = stack.getType();

        if (mat == null)
            return;

        Action action = event.getAction();
        switch (mat)
        {
            case FIREBALL:
               if (action==Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
               {
                   event.setCancelled(true);
                   updateMap(event.getPlayer());
               }
                break;

            case MONSTER_EGG:
                if (action == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() != null)
                {
                    EntityType type = ((SpawnEgg) stack.getData()).getSpawnedType();
                    if (type != EntityType.IRON_GOLEM)
                        return;

                    DreamDefender golem = new DreamDefender(currentPlayer.getTeam(), currentPlayer,arena,event.getClickedBlock().getLocation(),entityListener);
                    golem.spawn();
                    updateInventory(player,Material.MONSTER_EGG);
                }
                break;


            case WATER_BUCKET:
            {
                if (action == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() != null && block.getType() != Material.AIR)
                {

                    Location blockLoc = event.getClickedBlock().getLocation();
                    BlockFace face = event.getBlockFace();
                    blockLoc.add(face.getModX(),face.getModY(),face.getModZ());
                    Block waterPlace = blockLoc.getBlock();
                    if (waterPlace.hasMetadata(GENERATOR.getData())||waterPlace.hasMetadata(BASE.getData())) {
                       event.setCancelled(true);
                       return;
                    }

                    if (player.getGameMode() == GameMode.CREATIVE)
                        return;

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ItemStack stack = player.getInventory().getItemInHand();
                            if (!ItemHelper.isItemInvalid(stack) && stack.getType()==Material.BUCKET)
                                player.setItemInHand(null);
                            cancel();
                        }
                    }.runTaskLater(plugin,1);
                }
            }
            break;

        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event)
    {
        Entity clicked = event.getRightClicked();
        if (clicked.getType()!= EntityType.ARMOR_STAND)
        {
            if (event.getPlayer().getInventory().getItemInHand().getType()==Material.FIREBALL&&isRegistered(event.getPlayer())) {
                event.setCancelled(true);
                //updating the hashmap and also shooting a fireball if possible.
                updateMap(event.getPlayer());
            }
        }

        //add code here for the golems and the snowballs and enderpearls.
        //also put in above method.
    }



    public void updateMap(Player player)
    {
        Map<UUID, BattlePlayer> players = arena.getPlayers();
        if (!players.containsKey(player.getUniqueId()))
            return;

        BattlePlayer currentlyRegistered = players.get(player.getUniqueId());


        if (!coolDown.containsKey(player.getUniqueId())) //if shot for first time
        {
            coolDown.put(player.getUniqueId(),(System.currentTimeMillis()+DELAY));  //add cooldown


            new ThrownFireball(plugin,currentlyRegistered); //create a fireball
            updateInventory(player,Material.FIREBALL);

        }
        else  //if the map contains the player
        {
            Long value = coolDown.get(player.getUniqueId());
            if (System.currentTimeMillis()>=value)  //if the cooldown has run out [system time is greater]
            {
                updateInventory(player,Material.FIREBALL);
                coolDown.replace(player.getUniqueId(),System.currentTimeMillis()+DELAY);
                new ThrownFireball(plugin,currentlyRegistered);
            }
            else //otherwise if the cooldown has not run out yet
            {
                player.sendMessage(ChatColor.RED+"You must wait "+(((double)(coolDown.get(player.getUniqueId())-System.currentTimeMillis()))
                        /DIVISION)+" seconds first!");
            }
        }

    }


    public void updateInventory(Player player, Material toDecrease)
    {

        if (player.getGameMode() == GameMode.CREATIVE) {
            if (!messaged.contains(player.getUniqueId()))
            {
                player.sendMessage(ChatColor.YELLOW + "Hey! Just a notice, you're in creative. [Is this a development environment??]");
                messaged.add(player.getUniqueId());
            }
            return;
        }

        PlayerInventory inv = player.getInventory();


        if (inv.getItemInHand()!=null&&inv.getItemInHand().getItemMeta()!=null) {
            ItemStack update = player.getInventory().getItemInHand();

            if (update.getType() == toDecrease)
            {
                update.setAmount(update.getAmount() - 1);
                player.setItemInHand(update);

            }
            else
            {
                for (int slot=0;slot<player.getInventory().getSize();slot++)
                {
                    if (player.getInventory().getItem(slot).getType()==toDecrease)
                    {
                        update = player.getInventory().getItem(slot);
                        update.setAmount(update.getAmount()-1);
                        player.getInventory().setItem(slot,update);
                        break;
                    }
                }
            }//else
            player.updateInventory();


        }//if not null

    }//method

    private synchronized boolean isRegistered(Player player)
    {
        return arena.getPlayers().containsKey(player.getUniqueId());
    }
}