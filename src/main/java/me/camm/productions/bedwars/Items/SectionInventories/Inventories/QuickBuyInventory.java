package me.camm.productions.bedwars.Items.SectionInventories.Inventories;

import me.camm.productions.bedwars.Game.Arena;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.DefaultQuickItemConfig;
import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import me.camm.productions.bedwars.Items.ShopItemSet;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import me.camm.productions.bedwars.Util.Tuple2;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


import java.util.ArrayList;
import java.util.List;

import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.*;
import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryName.QUICK_BUY;

/**
 * @author CAMM
 * This inventory models a section inventory in the quick buy
 */
public class QuickBuyInventory extends ShopInventory {
   private List<ShopItemSet> values;

    public QuickBuyInventory(boolean isInflated, List<ShopItemSet> values, Arena arena)
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

    public List<Tuple2<ItemStack,Integer>> getItems(){
        List<Tuple2<ItemStack,Integer>> items = new ArrayList<>();
        for (int start = QUICK_INV_BORDER_START.getValue();start <= QUICK_INV_BORDER_END.getValue();start++) {
           items.add(new Tuple2<>(getItem(start),start));
        }
        return items;
    }

    public List<ShopItemSet> packageInventory(){
        List<Tuple2<ItemStack,Integer>> items = getItems();
        ArrayList<ShopItemSet> set = new ArrayList<>();
        for (Tuple2<ItemStack,Integer> current: items)
            set.add(new ShopItemSet(ItemHelper.getPackingAssociate(current.getFirstElem()),current.getSecondElem()));

        return set;
    }


    public List<String> getFileEntries() {
        List<ShopItemSet> packaged = packageInventory();
        List<String> values = new ArrayList<>();

        String empty = ShopItem.EMPTY_SLOT.name();
        for (ShopItemSet set: packaged) {
            if (set == null)
                values.add(empty);
            else values.add(set.toString());
        }
        return values;
    }

    @Override
    public boolean equals(Inventory other)
    {
        return super.equals((Object)other);
    }

}
