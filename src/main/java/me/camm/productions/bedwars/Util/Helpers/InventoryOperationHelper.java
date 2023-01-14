package me.camm.productions.bedwars.Util.Helpers;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Arena.Players.Managers.HotbarManager;
import me.camm.productions.bedwars.Arena.Players.Managers.PlayerInventoryManager;
import me.camm.productions.bedwars.Arena.Teams.BattleTeam;
import me.camm.productions.bedwars.Arena.Teams.TeamTraps.*;
import me.camm.productions.bedwars.Generators.Forge;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.HotbarEditorInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory;
import me.camm.productions.bedwars.Items.ItemDatabases.TeamItem;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.QuickBuyEditorInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.QuickBuyInventory;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.HotBarConfig;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.TeamInventoryConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import net.minecraft.server.v1_8_R3.Tuple;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCrafting;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static me.camm.productions.bedwars.Items.ItemDatabases.ShopItem.TRACKER_ITEM;


public class InventoryOperationHelper
{
    private enum Values {
        SLOT, HOTBAR, MOVE, SWAP, PLACE, COLLECT, DROP, PICKUP,
    }

    private final static ShopItem[] gameItems;
    private final static TeamInventoryConfig[] config;
    private final static ItemStack EMPTY;

   static {
       EMPTY = ItemHelper.toDisplayItem(ShopItem.EMPTY_SLOT,false);
       gameItems = ShopItem.values();
       config = TeamInventoryConfig.values();


   }



   public static boolean triedToTakeOut(InventoryClickEvent event, Inventory restrictedInv){

       InventoryAction action = event.getAction();
       String name = action.name();

       Inventory clickedInv = event.getClickedInventory();
       Inventory topInv = event.getInventory();



       //if both inventories are not possible to be restricted, then return
       if (!topInv.equals(restrictedInv) &&
               !event.getView().getBottomInventory().equals(restrictedInv) &&
               !clickedInv.equals(restrictedInv)) {

           return false;
       }





       if (clickedInv.equals(restrictedInv)) {

           return name.contains(Values.PICKUP.name()) ||
                   name.contains(Values.SWAP.name()) ||
                   name.contains(Values.MOVE.name()) || name.contains(Values.HOTBAR.name());

       }


       return false;


   }


    /*
    Returns whether a player has tried to place an item into a restricted inventory.

    @param event The event in question
    @param restrictedInv The inventory you do not want the player to place items into
    @author CAMM

     */
    public static boolean triedToPlaceIn(InventoryClickEvent event, Inventory restrictedInv)
    {
        InventoryAction action = event.getAction();
        String name = action.name();

        Inventory clickedInv = event.getClickedInventory();
        Inventory topInv = event.getInventory();



        //if both inventories are not possible to be restricted, then return
        if (!topInv.equals(restrictedInv) &&
                !event.getView().getBottomInventory().equals(restrictedInv) &&
                !clickedInv.equals(restrictedInv)) {

           return false;
        }




        if (event.isShiftClick()) {
            if (topInv.equals(restrictedInv)) {
                if (!(topInv instanceof CraftInventoryCrafting) && !clickedInv.equals(topInv)){



                    return true;


                }
            }
            else {

                if (clickedInv.equals(topInv)) {

                    return true;
                }

                //the bottom inv is the restricted one
            }
        }


        if (name.contains(Values.HOTBAR.name())) {
            int significand = topInv.getSize() -1;
            int rawSlot = event.getRawSlot();

            if (rawSlot <= significand)
                return true;
        }



        if (!clickedInv.equals(restrictedInv)) {

            if (name.contains(Values.PLACE.name())) {

                return false;
            }


            if (name.contains(Values.DROP.name())) {
                return false;
            }
        }
        else {



            if (name.contains(Values.PLACE.name()))
                return true;


            if (name.contains(Values.DROP.name()))
                return true;
        }






        return false;
    }



    public static boolean didTryToDragIn(InventoryDragEvent event, Inventory restrictedInv)
    {
        Inventory top = event.getView().getTopInventory();
        Inventory bottom = event.getView().getBottomInventory();


        int topSize, bottomSize;
        topSize = top.getSize();
        bottomSize = bottom.getSize();

        int topEnd = topSize -1;
        int bottomEnd = topEnd + bottomSize;


        Set<Integer> slots = event.getRawSlots();
        Tuple<Integer, Integer> range = getSlotPair(slots);

        if (range == null)
        {
            event.setCancelled(true);
            return true;
        }


        boolean equals = false;
        int significantStart = -1;
        int significantEnd = -1;

        if (restrictedInv.equals(top)) {
            equals = true;
            significantEnd = topEnd;
            significantStart = 0;
        }

        if (restrictedInv.equals(bottom)) {
            equals = true;
            significantStart = topSize;  //start of bottom inv
            significantEnd = bottomEnd;
        }


        if (!equals)
            return false;

        int dragStart = range.a();
        int dragEnd = range.b();

        return dragStart >= significantStart || dragEnd <= significantEnd;


    }





    /*
    This method handles restrictions default for inventories, and cancels the inventory click event if
    any restrictions are triggered.

    Restrictions countered:
     - trying to place restricted items into inventories
     - armor
     - trying to use restricted inventories which are NOT instances of the custom inventories
     - trying to place navigators into the player inventory


     This method also handles the sword count in the player inventory.
     Checks should already have been performed to determine if the player has clicked on a game inventory or not.


    @author CAMM
    @param event: The event in question
    @param arena: The arena for which the game is taking in
    @pre: The managers for the player should be set up first before calling this method, or there may be a
    null pointer exception when handling sword counts
    @return: Whether the event was cancelled



//good
     */
    public static void handleDefaultRestrictions(InventoryClickEvent event, Arena arena){

        BattlePlayer clicked = arena.getPlayers().getOrDefault(event.getWhoClicked().getUniqueId(),null);

        if (clicked == null)
            return;



        Inventory topInv = event.getInventory();
        Inventory playerInv = clicked.getRawPlayer().getInventory();


        ItemStack clickedItem = event.getCurrentItem();
        // the item which was in the slot of the inventory
        //at the time of the click. It likely is not still in the slot, but now in the cursor

        ItemStack cursorItem = event.getCursor();
        //the item which was on the cursor at the time of the click. Likely is now in the slot




        Map<Integer, IGameInventory> accessibleInvs = clicked.getAccessibleInventories();

        IGameInventory customInv = accessibleInvs.getOrDefault(topInv.hashCode(), null);
        if (customInv == null) {

            handleDefaultItemRestrictions(clickedItem, cursorItem, event, topInv, playerInv);
            return;
        }

        //if they tried to place an item into their inventory, then we gotta do checks to make sure it's not
        // from the hb editor
        if (triedToPlaceIn(event, playerInv)) {

            if (customInv instanceof HotbarEditorInventory && ItemHelper.getNavigator(cursorItem) != null) {
                event.setCancelled(true);
                return;
            }
        }

        if (triedToPlaceIn(event, topInv)) {
            event.setCancelled(true);
        }



    }


    private static void handleDefaultItemRestrictions(ItemStack clickedItem, ItemStack cursorItem, InventoryClickEvent event, Inventory topInv, Inventory playerInv){

        //at this point, it could still be any inventory, but the difference is that they won't be placing into their inv,
        //they'll only be placing out into the top inv



        Class<? extends Inventory> clazz = topInv.getClass();


        if (!(clazz.equals(CraftInventory.class) || clazz.equals(CraftInventoryCrafting.class))) {

            if (InventoryOperationHelper.triedToPlaceIn(event, topInv)) {

                event.setCancelled(true);
                return;
            }
        }


        ItemStack trackStack = ItemHelper.toSimpleItem(TRACKER_ITEM.sellMaterial,TRACKER_ITEM.name);




        if (event.getHotbarButton() != -1) {
            ItemStack hotbar = playerInv.getItem(event.getHotbarButton());

            int significand = event.getView().getTopInventory().getSize()-1;
            int raw = event.getRawSlot();



            if (hotbar != null && hotbar.isSimilar(trackStack)) {

                if (raw <= significand) {
                    event.setCancelled(true);
                    return;
                }
            }
            else if (ItemHelper.isInventoryPlaceRestrict(hotbar)) {
                if (raw <= significand) {
                    event.setCancelled(true);
                    return;
                }
            }
        }


        boolean trackSimilar = trackStack.isSimilar(clickedItem) || trackStack.isSimilar(cursorItem);

        if (ItemHelper.isInventoryPlaceRestrict(cursorItem) ||
                ItemHelper.isInventoryPlaceRestrict(clickedItem) || trackSimilar
        ) {



            if (InventoryOperationHelper.triedToPlaceIn(event, topInv)) {
                event.setCancelled(true);
                return;
            }

            boolean armor = false;
            if (cursorItem != null && clickedItem != null) {
                armor = ItemHelper.isArmor(cursorItem.getType()) || ItemHelper.isArmor(clickedItem.getType());
            }

            if (event.getClickedInventory().equals(playerInv) && armor) {

                event.setCancelled(true);
                return;
            }
        }


        if (clazz.equals(CraftInventoryCrafting.class)) {

            InventoryAction action = event.getAction();
            String name = action.name().toLowerCase();

            boolean place = (name.contains("hotbar") || name.contains("move") || name.contains("place"))
                    && event.getClickedInventory().equals(topInv);

            if (place)
                event.setCancelled(true);



            //we need to check if it is placing into the crafting inv.
        }


    }



    public static void operateSwordCount(BattlePlayer clicked)
    {
        Inventory playerInv = clicked.getRawPlayer().getInventory();

        int amount = ItemHelper.getPresent(Material.WOOD_SWORD,playerInv);
        if (amount != 1) {
            ItemStack shopItem = ItemHelper.toSoldItem(ShopItem.WOODEN_SWORD,clicked);
            ItemHelper.clearAll(shopItem,playerInv);
            //we clear all wooden swords

            int swords = ItemHelper.countSwords(playerInv);
            if (swords == 0) {
                //and if they still have swords (they are not wooden, then we return, else we give them one)
                clicked.getBarManager().set(shopItem, ShopItem.WOODEN_SWORD, clicked.getRawPlayer());
            }
        }

    }



    public static void handleDefaultRestrictions(InventoryDragEvent event, Arena arena)
    {

        BattlePlayer clicked = arena.getPlayers().getOrDefault(event.getWhoClicked().getUniqueId(),null);

        if (clicked == null)
            return;


        ItemStack dragged = event.getOldCursor();
        Inventory topInv = event.getView().getTopInventory();
        Inventory bottomInv = event.getView().getBottomInventory();


        if (ItemHelper.isItemInvalid(dragged))
            return;

        Map<Integer, IGameInventory> accessibles = clicked.getAccessibleInventories();

        IGameInventory customInv = accessibles.getOrDefault(topInv.hashCode(), null);

        if (customInv == null) {
            handleDefaultItemRestrictions(dragged, topInv, bottomInv, event);
            return;
        }

        if (customInv instanceof HotbarEditorInventory) {
            if (didTryToDragIn(event, bottomInv)) {
                event.setCancelled(true);
            }
        }
    }


    private static void handleDefaultItemRestrictions(ItemStack dragged, Inventory top, Inventory bottom, InventoryDragEvent event) {



        Set<Integer> slots = event.getRawSlots();
        Tuple<Integer, Integer> slotPair = getSlotPair(slots);

        if (slotPair == null) {
            event.setCancelled(true);
            return;
        }

        int topSize, bottomSize;
        topSize = top.getSize();
        bottomSize = bottom.getSize();




        int topEnd = topSize - 1;
        int bottomStart = topEnd + bottomSize;



        if ((slotPair.a() <= topEnd || slotPair.b() <= topEnd)) {

            Class<? extends Inventory> clazz = top.getClass();
            //if it's just a normal chest
            if (clazz.equals(CraftInventory.class)) {

                if (ItemHelper.isInventoryPlaceRestrict(dragged))
                    event.setCancelled(true);
            }
            else event.setCancelled(true);

            return;
        }

        if ((slotPair.a() >= bottomStart || slotPair.b() >= bottomStart) && ItemHelper.isArmor(dragged.getType())) {
            event.setCancelled(true);
        }


    }



    private static Tuple<Integer, Integer> getSlotPair(Set<Integer> slots ){
        int lowest = Integer.MAX_VALUE;
        int highest = -1;

  boolean found = false;

        for (int slot: slots) {

            if (slot < lowest) {
                found = true;
                lowest = slot;
            }

            if (slot > highest) {
                found = true;
                highest = slot;
            }


        }

        if (!found) {
            return null;
        }

        return new Tuple<>(lowest, highest);
    }





    public static void sellTeamBuy(InventoryClickEvent event, Arena arena) {

        BattlePlayer clicked = arena.getPlayers().getOrDefault(event.getWhoClicked().getUniqueId(),null);
        if (clicked == null)
            return;

        BattleTeam team = clicked.getTeam();
        IGameInventory teamInventory = team.getTeamInventory();

        if (event.getClickedInventory().equals(teamInventory) || triedToPlaceIn(event,(Inventory)teamInventory)) {
            event.setCancelled(true);
        }

        int slot = event.getRawSlot();
        TeamInventoryConfig configItem = null;

        for (TeamInventoryConfig current: config)
        {
            for (int index: current.getSlots())
                if (slot == index)
                {
                    configItem = current;
                    break;
                }
        }

        if (configItem == null)
            return;


        event.setCancelled(true);

        TeamItem teamItem = configItem.getItems();
        if (teamItem.name().contains(Values.SLOT.name()))
            return;

        int cost;
        if (teamItem.isRenewable()) {
            cost = team.countTraps()+1;

            if (team.countTraps() >= team.getMaxTrapNumber()) {
                clicked.sendMessage(ChatColor.RED+"Your trap slots are full!");
                return;
            }
        }
        else
        {
            int index = team.getUpgradeIndex(teamItem);

            if (index == teamItem.getCost().length + 1)
            {
              clicked.sendMessage(ChatColor.RED+"You have the max upgrades for this category!");
              return;
            }
            cost = (index == -1) ? teamItem.getCost()[0] : teamItem.getCost()[index-1];
        }

              //check for current traps and upgrade limits here.


        boolean paid = ItemHelper.didPay(clicked,teamItem.getCostMat(),cost);

        if (paid)
        {

            team.sendTeamMessage(ChatColor.GREEN+clicked.getRawPlayer().getName()+" bought "+teamItem.format());

            if (teamItem.isRenewable())
            {

                ITrap trap = null;
                switch (teamItem) {
                    case TRAP_ALARM:
                        trap = new AlarmTrap(team,team.getTrapArea());
                        break;

                    case TRAP_MINER_SLOW:
                        trap = new MiningTrap(team, team.getTrapArea());
                        break;

                    case TRAP_OFFENSIVE:
                        trap = new OffensiveTrap(team, team.getTrapArea());
                        break;

                    case TRAP_SIMPLE:
                        trap = new SimpleTrap(team,team.getTrapArea());
                        break;
                }

               if (trap == null) {
                   team.sendTeamMessage(ChatColor.RED + "[ERROR]Trap result was null! (This should not be!) Given team item: " + teamItem);
                   return;
               }

               team.addTrap(trap);
               team.updateTrapDisplay();

                return;
            }


            // Only updates the hashmap for level keeping
           boolean upgraded = team.updateUpgradeTeamModifier(teamItem);

           if (!upgraded)
           {
               clicked.sendMessage(ChatColor.RED+"[ERROR] Could not upgrade team modifier. (This should not be!)");
               return;
           }

           //if it's not a trap, then we should make do to update modifiers. (saves resources)
            team.applyPlayerTeamModifiers();
           //updates everything in the inv.
           team.updateModifierDisplay(configItem);

           //do the modifiers here. Only these should be here, since an update to the applications will
            //refresh everything including this. (We don't want 9999 dragons spawning, etc)
            //These upgrades only have a limited amount they can be upgraded, so no if statement
            //needed here.
           switch (teamItem) {

               case BUFF_DRAGONS: //
                   team.setDragonSpawnNumber(team.getDragonSpawnNumber()+1);
                   break;

               case UPGRADE_FORGE: //
                  Forge forge = team.getForge();
                  forge.setTier(forge.getTier()+1);
                   break;

               case BUFF_BASE_REGEN:
                   team.loadAura();
                   break;
           }
        }
    }


    //Operations for the hb manager
    public static void operateHotBarClick(InventoryClickEvent event, Arena arena)
    {

        /*
        This is an exception to the cancelling above. All other cases should be cancelled, yes, but this one, no
        in some cases.
         */



        BattlePlayer clicked = arena.getPlayers().getOrDefault(event.getWhoClicked().getUniqueId(),null);
         if (clicked == null)
             return;


         Inventory playerInv = clicked.getRawPlayer().getInventory();

        int slot = event.getSlot();
        boolean hotbar = event.getAction().name().toLowerCase().contains("hotbar");


         if (InventoryOperationHelper.triedToPlaceIn(event, clicked.getRawPlayer().getInventory())) {



             ItemStack question = event.getClickedInventory().getItem(event.getSlot());


             if (ItemHelper.getNavigator(question) != null) {

                 event.setCancelled(true);
                 return;
             }
         }




        HotbarManager manager = clicked.getBarManager();
        Inventory clickedInv = event.getClickedInventory();


        ItemStack cursor = event.getCursor();

         if (cursor == null || cursor.getType()==Material.AIR)
         {

             if (hotbar) {
                 ItemStack hotbarItem = playerInv.getItem(event.getHotbarButton());

                 int significand = event.getView().getTopInventory().getSize();
                 int rawSlot= event.getRawSlot();

                 if (rawSlot <= significand) {
                     event.setCancelled(true);
                     return;
                 }


             }





             if (HotbarManager.slotInRangeTake(slot))
             {

                 operateBarItemTake(clickedInv, slot, arena.getPlugin());
             }
             else if (HotbarManager.slotInRangePlace(slot))
             {

                 clickedInv.setItem(slot, null);
                boolean result = manager.updateLayout(slot,null);

                 if (!result)
                     clicked.sendMessage(ChatColor.RED+"[ERROR] Unable to update your layout!");
             }
             else {

                 checkReturnReset(clicked, slot);
                 event.setCancelled(true);
             }

         }
         else
         {


                 if (HotbarManager.slotInRangePlace(slot))
                 {

                     if (ItemHelper.getNavigator(cursor) == null) {
                         event.setCancelled(true);
                         return;
                     }


                    boolean result = manager.updateLayout(slot, cursor);

                    if (!result)
                        clicked.sendMessage(ChatColor.RED+"[ERROR] Unable to update your layout!");

                 }
                 else {
                     checkReturnReset(clicked, slot);
                     event.setCancelled(true);
                 }


         }

            /*
            If the cursor is null, then check if pickup slots valid. If not, check if place slots valid.
            If place valid, then remove item from layout.

            If cursor not null, then check if place valid. If place invalid, cancel. Else, apply layout.
             */


        //if the residing item is not invalid (the clicked item)
        //if the item is valid for editing, put it onto the cursor and set the item back in there.
        //if it is a nav item, then do the actions instead then.



    }

    private static void checkReturnReset(BattlePlayer player, int slot){
        if (slot == HotBarConfig.RETURN.getSlots()[0]) {
            player.getRawPlayer().openInventory(player.getShopManager().getQuickBuy());
        }

        if (slot == HotBarConfig.RESET.getSlots()[0]) {
            player.getBarManager().reset();
        }
    }

    //for the hb manager
    private static void operateBarItemTake(Inventory clicked, int slot, Plugin plugin){


            //not sure if this will work. Needs to be invoked after the event, so maybe use bukkitrunnable instead?

        if (clicked == null)
            return;

        ItemStack residing = clicked.getItem(slot);
        if (residing == null)
            return;

        final ItemStack replace = residing.clone();

            new BukkitRunnable() {
                @Override
                public void run() {
                    clicked.setItem(slot, replace);
                    cancel();
                }
            }.runTaskLater(plugin, 1);


    }


    /*
    replace: the itemstack which will replace an existing item in the inventory
    We can assume that the player is the player that invoked the event
     */
    public static void operateInventoryEdit(InventoryClickEvent event, Arena arena){


        BattlePlayer player = arena.getPlayers().getOrDefault(event.getWhoClicked().getUniqueId(),null);
        if (player == null)
            return;

        QuickBuyEditorInventory display = player.getQuickEditor();
        Inventory clicked = event.getClickedInventory();


        //also need to account for dragging
        if (triedToPlaceIn(event, display)) {
            event.setCancelled(true);
        }

        int slot = event.getSlot();

        if (isInQuickBuyPanel(slot)) {

            if (!clicked.equals(display))
                return;

            event.setCancelled(true);
            display.applyConfigChange(slot);
            QuickBuyInventory section = player.getShopManager().getQuickBuy();
            player.getRawPlayer().openInventory(section);

        }
        else
            event.setCancelled(true);
    }


    public static boolean isInQuickBuyPanel(int slot){

        return ( slot >= InventoryProperty.QUICK_INV_BORDER_START.getValue() &&
                slot <= InventoryProperty.QUICK_INV_ROW1_END.getValue() ) ||

                ( slot >= InventoryProperty.QUICK_INV_BORDER_ROW2_START.getValue() &&
                        slot <= InventoryProperty.QUICK_INV_BORDER_ROW2_END.getValue() ) ||

                ( slot >= InventoryProperty.QUICK_INV_ROW3_START.getValue() &&
                        slot <= InventoryProperty.QUICK_INV_BORDER_END.getValue() );

    }





    /*
    @Author CAMM
    Takes an inventory click event and determines whether action on the quick buy inventory should be
    executed to sell an item, etc.

    At this point, the inventory referenced should be guaranteed a shop inventory.
     */
    public static void sellQuickbuy(InventoryClickEvent event, Arena arena, boolean isInflated)
    {


//



        Map<UUID,BattlePlayer> registeredPlayers = arena.getPlayers();
        Inventory clickedInv = event.getClickedInventory();
        HumanEntity player = event.getWhoClicked();


        if (!registeredPlayers.containsKey(player.getUniqueId()))
            return;

        BattlePlayer currentPlayer = registeredPlayers.get(player.getUniqueId());

        if (clickedInv==null||clickedInv.getTitle()==null)
            return;

        ItemStack item = event.getCurrentItem();

        if (item==null||item.getItemMeta()==null)
            return;

        PlayerInventoryManager manager = currentPlayer.getShopManager();

        if (manager==null)
            return;

        event.setCancelled(true);

        try
        {
            ShopItem clickedItem = null;
            String name = item.getItemMeta().getDisplayName();
            if (name == null)
                return;


            for (ShopItem current: gameItems)
            {
                if ((current.name).equalsIgnoreCase(name) || name.equalsIgnoreCase(current.name))
                {
                    clickedItem = current;
                    break;
                }
            }

            if (clickedItem==null)
                return;


            ItemCategory category = clickedItem.category;

            if (ItemHelper.isPlaceHolder(category))
            {
                navigate(clickedItem, currentPlayer);
                return;
            }



            //accounts for team enchants.
            //This section is unfinished. We need to account for if the player shift-clicks the item.
            //(We remove it from quick buy in this case)
            QuickBuyInventory quickBuy = manager.getQuickBuy();

            if (quickBuy.equals(clickedInv))
            {
                //if it is the quickbuy inventory, we have the option of replacing items.
                if (event.isShiftClick()) {
                    int slot = event.getRawSlot();
                    quickBuy.setItem(slot, EMPTY);
                    currentPlayer.getQuickEditor().setItem(slot, EMPTY);
                }
                else
                    ItemHelper.sellItem(clickedItem, currentPlayer, isInflated,event);
            }
            else if (event.isShiftClick())
            {
                currentPlayer.getQuickEditor().setCurrentAdding(item);
                currentPlayer.getQuickEditor().display();
            }
            else
                ItemHelper.sellItem(clickedItem, currentPlayer, isInflated,event);



        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            e.printStackTrace();
        }

    }

    /*

   Takes an Inventory item, and a battle player.
   If the item is a navigation item, brings the player to a different inventory interface.
    */
    private static void navigate(ShopItem item, BattlePlayer player)
    {
        Player rawPlayer = player.getRawPlayer();
        PlayerInventoryManager manager = player.getShopManager();
        switch (item)
        {
            case ARMOR_NAV:
                rawPlayer.openInventory(manager.getArmorSection());
                break;

            case BLOCKS_NAV:
                rawPlayer.openInventory(manager.getBlockSection());
                break;

            case HOME_NAV:
                rawPlayer.openInventory(manager.getQuickBuy());
                break;

            case MELEE_NAV:
                rawPlayer.openInventory(manager.getMeleeSection());
                break;

            case TOOLS_NAV:
                rawPlayer.openInventory(manager.getToolsSection());
                break;

            case HOTBAR_NAV:
                player.getBarManager().display(rawPlayer);
                break;

            case RANGED_NAV:
                rawPlayer.openInventory(manager.getRangedSection());
                break;

            case POTIONS_NAV:
                rawPlayer.openInventory(manager.getPotionSection());
                break;

            case UTILITY_NAV:
                rawPlayer.openInventory(manager.getUtilitySection());
        }
    }
}
