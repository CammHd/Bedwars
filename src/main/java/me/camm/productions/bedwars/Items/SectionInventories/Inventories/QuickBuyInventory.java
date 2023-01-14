package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.DefaultQuickItemConfig;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import me.camm.productions.bedwars.Util.DataSets.ItemStackSet;
import me.camm.productions.bedwars.Util.DataSets.ShopItemSet;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.inventory.Inventory;


import java.util.ArrayList;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.*;
import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName.QUICK_BUY;

/**
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
public class QuickBuyInventory extends ShopInventory {
   private ArrayList<ShopItemSet> values;

    public QuickBuyInventory(boolean isInflated, ArrayList<ShopItemSet> values, Arena arena)
    {
        super(null,SHOP_SIZE.getValue(),QUICK_BUY.getTitle(),isInflated, arena);

        if (values!=null)
            this.values = ItemHelper.filter(values);
        setTemplate(isInflated,true);
        setInventoryItems();
    }


    //Setting the default configurations.
    @Override
    public void setInventoryItems()
    {
        if (values==null || values.size() == 0)
        {

            //Setting the default items.
            for (DefaultQuickItemConfig config : DefaultQuickItemConfig.values()) {
                int[] slots = config.getSlots();
                for (int slot : slots)
                    setItem(slot, config.getItem(),isInflated);
            }
        }
        else {
            //Filling the remaining slots with the configured items.
            for (ShopItemSet set : values)
                setItem(set.getSlot(), set.getItem(),isInflated);
        }

        fillEmpties();
    }

    private void fillEmpties()
    {
        for (int slot=QUICK_INV_BORDER_START.getValue();slot<=QUICK_INV_BORDER_END.getValue();slot++)
            if (getItem(slot)==null && !(slot%9 == 0 || (slot+1)%9 == 0))
                setItem(slot, ShopItem.EMPTY_SLOT,false);
    }

    public ArrayList<ItemStackSet> getItems(){
        ArrayList<ItemStackSet> items = new ArrayList<>();
        for (int start = QUICK_INV_BORDER_START.getValue();start <= QUICK_INV_BORDER_END.getValue();start++) {
           items.add(new ItemStackSet(getItem(start),start));
        }
        return items;
    }

    public ArrayList<ShopItemSet> packageInventory(){
        ArrayList<ItemStackSet> items = getItems();
        ArrayList<ShopItemSet> set = new ArrayList<>();
        for (ItemStackSet current: items)
            set.add(new ShopItemSet(ItemHelper.getPackingAssociate(current.getStack()),current.getSlot()));

        return set;
    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }

}
