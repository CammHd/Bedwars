package me.camm.productions.bedwars.Items.ItemProperties;


/**
 * @author CAMM
 * template for the shops (excluding the buyable items) in the quickbuy shops
 */
public enum DefaultTemplateNavigation
{
    HOME(new int[] {0}, ShopItem.HOME_NAV),
    BLOCKS(new int[] {1}, ShopItem.BLOCKS_NAV),
    MELEE(new int[] {2}, ShopItem.MELEE_NAV),
    ARMOR(new int[] {3}, ShopItem.ARMOR_NAV),
    TOOLS(new int[] {4}, ShopItem.TOOLS_NAV),
    RANGED(new int[] {5}, ShopItem.RANGED_NAV),
    POTIONS(new int[] {6}, ShopItem.POTIONS_NAV),
    UTILITY(new int[] {7}, ShopItem.UTILITY_NAV),
    HOTBAR(new int[] {53}, ShopItem.HOTBAR_NAV),
    SEPARATOR(new int[]{9,10,11,12,13,14,15,16,17}, ShopItem.SEPARATOR),
    EMPTY(new int[] {19,22,37,38,39,40,41,42,43}, ShopItem.EMPTY_SLOT);

    private final int[] range;
    private final ShopItem item;

    DefaultTemplateNavigation(int[] range, ShopItem item)
    {
        this.range = range;
        this.item = item;
    }

    public int[] getRange()
    {
        return range;
    }

    public ShopItem getItem()
    {
        return item;
    }

}


