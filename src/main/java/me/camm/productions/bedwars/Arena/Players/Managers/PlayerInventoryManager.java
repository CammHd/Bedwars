package me.camm.productions.bedwars.Arena.Players.Managers;

import me.camm.productions.bedwars.Arena.Game.Arena;
import me.camm.productions.bedwars.Arena.Players.BattlePlayer;
import me.camm.productions.bedwars.Items.ItemDatabases.ShopItem;
import me.camm.productions.bedwars.Items.SectionInventories.Inventories.*;
import me.camm.productions.bedwars.Items.SectionInventories.Templates.ShopInventory;
import me.camm.productions.bedwars.Util.DataSets.ShopItemSet;
import me.camm.productions.bedwars.Util.Helpers.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author CAMM
This class manages inventory navigation for players in the game by bringing them to different
inventories.
 */
public class PlayerInventoryManager
{
    private boolean isInflated;
    private BattlePlayer owner;
    private Arena arena;



    //These inventories change for each player.
    private ArmorSectionInventory armorSection;
    private  QuickBuyInventory quickBuy;
    private  ToolsSectionInventory toolsSection;

    //These inventories are universal for every player and thus don't need to be changed.
    //find a way (if possible) to do this.
    private static BlockSectionInventory blockSection;
    private static MeleeSectionInventory meleeSection;
    private static PotionSectionInventory potionSection;
    private static RangedSectionInventory rangedSection;
    private static UtilitySectionInventory utilitySection;


    private ShopInventory[] values;




    public PlayerInventoryManager(boolean isInflated, Arena arena, BattlePlayer owner)
    {
       this(null,isInflated, arena,owner);
    }

    public PlayerInventoryManager(ArrayList<ShopItemSet> quickBuyConfiguration, boolean isInflated, Arena arena, BattlePlayer owner)
    {

        this.arena = arena;
        this.owner = owner;

        armorSection = new ArmorSectionInventory(isInflated, arena);
        quickBuy = new QuickBuyInventory(isInflated,quickBuyConfiguration, arena);
        toolsSection = new ToolsSectionInventory(isInflated, arena);


        if (blockSection == null)
            blockSection = new BlockSectionInventory(isInflated, arena);

        if (meleeSection == null)
            meleeSection = new MeleeSectionInventory(isInflated, arena);

        if (potionSection == null)
            potionSection = new PotionSectionInventory(isInflated, arena);

        if (rangedSection == null)
            rangedSection = new RangedSectionInventory(isInflated, arena);

        if (utilitySection == null)
            utilitySection = new UtilitySectionInventory(isInflated,arena);


        values = new ShopInventory[]{
                armorSection,
                quickBuy,
                toolsSection,
                blockSection,
                meleeSection,
                potionSection,
                rangedSection,
                utilitySection,
      };

        this.isInflated = isInflated;


    }


    @Deprecated
    public Inventory isSectionInventory(Inventory inv)
    {
        if (inv == null)
            return null;

      for (ShopInventory inventory: values) {
          if (inventory.equals(inv))
              return inventory;
      }

      return null;
    }

    //the armor section, quickbuy, and the tools sections are the only sections that
    //can dynamically change
    public void replaceItem(ShopItem toReplace, ShopItem replacement)
    {
        searchAndReplace(armorSection,toReplace, replacement);
        searchAndReplace(quickBuy,toReplace, replacement);
        searchAndReplace(toolsSection,toReplace, replacement);
    }

    public ShopInventory[] getShopInventories() {
        return values;
    }

    /*
        Searches and replaces all items that match the similarity of the toReplace item with
        the replacement item
         */
    private void searchAndReplace(Inventory inv, ShopItem toReplace, ShopItem replacement)
    {



        if (owner == null) {
            return;
        }


        ItemStack toBeSet = ItemHelper.toDisplayItem(replacement, isInflated);
        ItemStack toBeReplaced = ItemHelper.toDisplayItem(toReplace, isInflated);

        for (int i = 0;i< inv.getSize();i++)
        {
            ItemStack residing = inv.getItem(i);
            if (ItemHelper.isItemInvalid(residing))
                continue;


            if (residing.getType() == Material.WOOD_AXE) {
            }

            //Enchantments can change the name, so don't use displayName().equalsIgnoreCase()...
            if (residing.isSimilar(toBeReplaced)) {
                inv.setItem(i, toBeSet);
            }
        }
    }



  /////////////////////////
    //getters
    public ArmorSectionInventory getArmorSection() {
        return armorSection;
    }

    public QuickBuyInventory getQuickBuy() {
        return quickBuy;
    }

    public ToolsSectionInventory getToolsSection() {
        return toolsSection;
    }

    public boolean isInflated() {
        return isInflated;
    }

    public BlockSectionInventory getBlockSection() {
        return blockSection;
    }

    public MeleeSectionInventory getMeleeSection() {
        return meleeSection;
    }

    public PotionSectionInventory getPotionSection() {
        return potionSection;
    }

    public RangedSectionInventory getRangedSection() {
        return rangedSection;
    }

    public UtilitySectionInventory getUtilitySection() {
        return utilitySection;
    }
}
