package me.camm.productions.bedwars.Arena.Players.Managers;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory;
import me.camm.productions.bedwars.Items.ItemDatabases.*;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.HotbarEditorInventory;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.HotBarConfig;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.IGameInventory;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.HOT_BAR_END;
import static me.camm.productions.bedwars.Items.ItemDatabases.ItemCategory.*;


/*
 * @author CAMM
 * The hotbar manager manages the placement of bought items into the player's inventory, EXCLUDING armor.
 */
public class HotbarManager
{
    private final ItemCategory[] layout;
    private final IGameInventory editor;


    //creating a manager with no layout from a saved file
    public HotbarManager(Arena arena)
    {
        this.layout = new ItemCategory[HOT_BAR_END.getValue()];
         editor = new HotbarEditorInventory(arena);



        initInventory();
        defaultConfig();
        updateDisplay();
    }



    //creating a manager with a layout from a saved file
    public HotbarManager(ItemCategory[] layout, Arena arena)
    {

        editor = new HotbarEditorInventory(arena);
        if ((layout==null)||(layout.length!= HOT_BAR_END.getValue())) {
            this.layout = new ItemCategory[HOT_BAR_END.getValue()];
            defaultConfig();
        }
        else {
            this.layout = layout;

            boolean empty = true;
            for (ItemCategory category: layout) {
                if (category!=null)
                {
                    empty = false;
                    break;
                }
            }

            if (empty)
                defaultConfig();
        }


        initInventory();
        updateDisplay();
    }

    private void defaultConfig(){
        addCategory(ItemCategory.MELEE,0);
        addCategory(TRACKER, 8);
    }


    private void initInventory(){
        for (HotBarConfig config: HotBarConfig.values()) {
            ItemStack item = ItemHelper.toSimpleItem(config.getMat(),config.getName());
            for (int slot: config.getSlots()) {
                editor.setItem(slot, item);
            }
        }
    }

    //we are only updating the layout here.
    public boolean updateLayout(int clickedSlot, ItemStack placed){
        clickedSlot -= InventoryProperty.LARGE_ROW_FIVE_START.getValue();
        if (clickedSlot < 0 || clickedSlot > layout.length)
            return false;

        if (placed==null) {
            layout[clickedSlot] = null;
            return true;
        }

        ItemCategory category = ItemHelper.getHotBarAssociate(placed);
        if (category == null)
            return false;

        if (category == OPERATOR)
            return false;

        layout[clickedSlot] = category;
        return true;
    }


    //updates the display for the inventory
    public void updateDisplay(){
        for (int slot=0;slot<layout.length;slot++) {
            ItemStack stack = ItemHelper.toBarItem(layout[slot]);
            editor.setItem(slot+InventoryProperty.LARGE_ROW_FIVE_START.getValue(),stack);
        }
    }


    //resets the configuration. Does not update the inventory
    public void reset(){

        for (int slot=0;slot<layout.length;slot++) {
            layout[slot] = null;
            addCategory(null,slot);
        }

        defaultConfig();
    }


    //slot category is in terms of the hotbar slots
    public void addCategory(ItemCategory category, int slot)
    {
        if (slot>layout.length||slot<0)
            return;

        layout[slot] = category;
        editor.setItem(slot+ InventoryProperty.LARGE_ROW_FIVE_START.getValue(),ItemHelper.toBarItem(category));
    }

    //shouldn't matter if raw or not since top inv is 0
    public static boolean slotInRangeTake(int slot){
        return slot >= InventoryProperty.LARGE_ROW_THREE_START.getValue() &&
                slot <= InventoryProperty.LARGE_ROW_THREE_END.getValue();
    }

    public static boolean slotInRangePlace(int slot) {
        return slot >= InventoryProperty.LARGE_ROW_FIVE_START.getValue() &&
                slot <= InventoryProperty.LARGE_ROW_FIVE_END.getValue();
    }




    public ItemCategory[] getLayout()
    {
        return layout;
    }

    public List<String> getFileEntries(){
        List<String> entries = new ArrayList<>();
        for (ItemCategory category: getLayout()) {
            if (category == null) {
                entries.add(null);
            }
            else entries.add(category.toString());
        }
        return entries;

    }

    public void display(Player player) {
        player.openInventory((Inventory)editor);
    }

    public boolean invEquals(Inventory other){
        return other.equals(editor);
    }

    public IGameInventory getEditor() {
        return editor;
    }

    /*



       DEFINITION OF HAS ROOM
        get an array of all empty slots. Make sure the array length is at least 2.

         y
        [x] [] [] []

        Parameters:

        For TIERED ITEMS
        if the inventory is full, return

        if x and y are a tiered item, and index x is less than index y, then x replaces y.
        If index x  == index y, then we search for the next spot in the hotbar to place y.
        First we check for reserved slots. If the all of the reserved slots are full, then we then
        put the item in a non reserved slot if it exists.

        If no spot exists [since the hotbar is full], then we go through all of the slots which are dedicated for the category of item
        We should make an array containing all of the possible slots when we do this so we don't have to loop over again
        if no spot is vacant, then we go through all of the slots which have items in them.
        If the category of the item is not the same as the category of the reserved slot,
        then we first check the fullness of the inventory. If the inventory is not full, then
        replace the item in the slot with the item, and put the replaced item in the next vacant or holdable slot
        in the inventory.


        for NORMAL ITEMS
        if the inventory is full, return

        if x and y are not the same category, and the slot of x is reserved for y, then
        we first check if the inv is full. If it is not full, then y replaces x.
        Else, if it is full, do not allow the player to purchase.

        if x and y are the same category, then we loop through all of the other slots, looking for a vacant spot, with
        the same reservation.


        If there is a slot with a different reservation, and all reserved slots are full, then place the item in the slot.

        If all hotbar slots are full, then check for items that are in slots with different reservations.
        If such item exists on a slot with the same reservation as y, then replace that item with y and
        put the replaced item deeper in the inventory.
         */

    public void set(ItemStack item, ShopItem enumItem, Player player, @Nullable InventoryClickEvent event) {


        ItemCategory category = enumItem.category;
        Inventory playerInv = player.getInventory();



        //armor is not set in this method. In fact, armor should not be set here at all.
        if (ItemHelper.isItemInvalid(item)||ItemHelper.isPlaceHolder(category)||
                category==ARMOR||!player.isOnline())
            return;

        //check if the item sold is an item with tiers.
        TieredItem enumTiered = ItemHelper.isTieredItem(enumItem);


        if (enumTiered != null) {

            //if the tier is the lowest tier, then we can do a set as if it was a non-tiered item, since we
            //won't be replacing any items in the player inventory.
            if (enumTiered.getIndex() == ItemHelper.getLowestTier(enumTiered).getIndex()) {
                doNormalSet(playerInv, enumItem, item,player,event);
                return;
            }


            //First we try to find items that it is trying to replace.
            ItemStack[] items = playerInv.getContents();
            for (int slot = 0; slot < items.length; slot++) {
                ItemStack residing = items[slot];

                //if the item is invalid, continue
                if (ItemHelper.isItemInvalid(residing))
                    continue;

                // if we cannot resolve the associate
                ShopItem associate = ItemHelper.getAssociate(residing);
                if (associate == null)
                    continue;

                //the item must be a tiered item to be replaced
                TieredItem associateTier = ItemHelper.isTieredItem(associate);
                if (associateTier == null)
                    continue;

                if (associateTier.getCategory() != enumTiered.getCategory())
                    continue;

                //It replaces everything
                if (enumTiered.isTotalReplacing()) {
                    if (associateTier.getIndex() < enumTiered.getIndex()) {
                        tieredSetTryWithEvent(event, playerInv, slot, item);
                        return;
                    }
                } else {
                    //If it is the smallest index
                    if (associateTier.getIndex() == ItemHelper.getLowestTier(associateTier).getIndex()) {
                        tieredSetTryWithEvent(event, playerInv, slot, item);
                        return;
                    }

                }
            }

            //if there is nothing to replace, do a normal set.

        }
        doNormalSet(playerInv,enumItem, item, player,event);
    }

    private void tieredSetTryWithEvent(@Nullable InventoryClickEvent event, Inventory inv, int slot, ItemStack item){

        if (event == null) {
            inv.setItem(slot, item);
            return;
        }

        int button = event.getHotbarButton();
        if (button == -1) {
            inv.setItem(slot, item);
            return;
        }

        inv.setItem(slot, null);

        ItemStack residing = inv.getItem(button);

        if (residing != null)
            inv.addItem(residing);

        inv.setItem(button, item);
    }



    public void set(ItemStack item, ShopItem enumItem, Player player)
    {
       set(item, enumItem,player, null);
    }

    private void doNormalSet(Inventory playerInv, ShopItem enumItem, ItemStack item, Player player,@Nullable InventoryClickEvent event) {



        if (event != null) {

            boolean result = setWithEvent(playerInv,item,event);

            if (result)
                return;
        }


        //if there is already a slot in the inv with the stack
        //merge the stacks together.
        ItemStack stack;
        for (int slot=0;slot<layout.length;slot++) {

            stack = playerInv.getItem(slot);
            if (item.isSimilar(stack) && (stack.getAmount()+item.getAmount() <= item.getMaxStackSize()))
            {
                item.setAmount(item.getAmount()+stack.getAmount());
                playerInv.setItem(slot,item);
                return;
            }
        }

        for (int slot=0;slot<layout.length;slot++)
        {
            ItemCategory reserved = layout[slot];
            ItemStack residing = playerInv.getItem(slot);

            // if the reserved slot is in the same category
            if (reserved != enumItem.category)
                continue;


            // test if the residing item is valid
            ShopItem residingAssociate = ItemHelper.getAssociate(residing);

                /*
                So if the slot is empty and the slot is reserved, place it in.
                 */
            if (residingAssociate == null) {
                //if we cannot resolve the residing item as an enum item

                //check if it's currency
                if (ItemHelper.isCurrencyItem(residing))
                {
                    ItemStack residingPlaceholder = residing.clone();
                    playerInv.setItem(slot, item);
                    playerInv.addItem(residingPlaceholder);
                }
                else
                    playerInv.setItem(slot, item);
                return;
            }


            //so if the two categories between residing and placing in are not the same,
            // and the reserved slot matches the one to place in,
            //then replace the item.
            if ((residingAssociate.category != enumItem.category))
            {
                //replace the item in the slot.
                //Add the replaced item back.
                ItemStack residingPlaceholder = residing.clone();
                playerInv.setItem(slot,item);
                playerInv.addItem(residingPlaceholder);
                return;
            }

            //in this case, residing and item are the same category and residing is in the reserved slot, so we
            //try to place it in.
            if (!item.isSimilar(residing))
                continue;

            if ( (item.getAmount() + residing.getAmount()) > residing.getMaxStackSize() )
                continue;

            item.setAmount(item.getAmount() + residing.getAmount());
            playerInv.setItem(slot, item);
            return;

        }


        //Now we look at the other slots. (ones with other categories)


        for (int slot=0;slot<layout.length;slot++)
        {
            ItemStack residing = playerInv.getItem(slot);

            //If the category is not specified there...
            if (layout[slot] == null)
            {
                //if the item is undefined or it is currency, place it in.
                if (residing == null || residing.getType() == Material.AIR) {
                    playerInv.setItem(slot,item);
                    return;
                }
                else if (ItemHelper.isCurrencyItem(residing))
                {
                    ItemStack cloned = residing.clone();
                    playerInv.setItem(slot, item);
                    playerInv.addItem(cloned);
                    return;
                }
            }
        }

        //try to place it in their main hand
        ItemStack hand = player.getInventory().getItemInHand();
        if (ItemHelper.isItemInvalid(hand)) {
            player.setItemInHand(item);
            return;
        }

        //If the entire hotbar is unavailable, then just add the item to the inv.
        playerInv.addItem(item);
    }



    //if the player uses a hotbar button for buying, it overrides the hotbar manager.
    //the event should be checked beforehand such that the button is valid.
    private boolean setWithEvent(Inventory playerInv, ItemStack newStack, @NotNull InventoryClickEvent event){

        int button = event.getHotbarButton();
        if (button < 0 || button > 8)
            return false;

        if (playerInv.firstEmpty() == -1) {
            return false;
        }


        ItemStack residing = playerInv.getItem(button);
        if (ItemHelper.isItemInvalid(residing)) {
            playerInv.setItem(button, newStack);
            return true;
        }


            int stackSize = residing.getMaxStackSize() + newStack.getMaxStackSize();
            if (newStack.isSimilar(residing) && stackSize <= residing.getMaxStackSize()) {
                residing.setAmount(stackSize);
               return true;
            }

            residing = residing.clone();
            playerInv.setItem(button, newStack);
            playerInv.addItem(residing);


            return true;
    }
}
