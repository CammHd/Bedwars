package me.camm.productions.bedwars.Util.Helpers;

import me.camm.productions.bedwars.Game.BattlePlayer;
import me.camm.productions.bedwars.Game.PlayerManagers.HotbarManager;
import me.camm.productions.bedwars.Items.ItemProperties.ItemCategory;
import me.camm.productions.bedwars.Items.ItemProperties.*;
import me.camm.productions.bedwars.Items.SectionInventories.InventoryConfigurations.HotBarConfig;
import me.camm.productions.bedwars.Items.ItemProperties.ShopItem;
import me.camm.productions.bedwars.Items.ShopItemSet;
import me.camm.productions.bedwars.Util.PacketSound;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.camm.productions.bedwars.Items.ItemProperties.BattleEnchantment.*;
import static me.camm.productions.bedwars.Items.ItemProperties.ShopItem.SHEARS;
import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_END;
import static me.camm.productions.bedwars.Items.SectionInventories.Templates.InventoryProperty.QUICK_INV_BORDER_START;
import static me.camm.productions.bedwars.Items.ItemProperties.ItemCategory.*;
import static me.camm.productions.bedwars.Items.ItemProperties.LorePhrases.COST;
import static me.camm.productions.bedwars.Items.ItemProperties.LorePhrases.SELL;
import static me.camm.productions.bedwars.Items.ItemProperties.PotionData.*;
import static org.bukkit.Material.*;


public class ItemHelper
{
  private static final ArrayList<ShopItem> restrictedFileItems;
  private static final ArrayList<TieredItem> tieredItemList;
  private static final ArrayList<TieredItem> lowestTiers;
  private static final HashSet<ShopItem> navigators;

  private static final HashMap<String, ShopItem> itemAssociator;



 static {
        itemAssociator = new HashMap<>();
         TieredItem[] tieredItems = TieredItem.values();
         ShopItem[] inventory = ShopItem.values();
         navigators = new HashSet<>();

         restrictedFileItems = new ArrayList<>();

         tieredItemList = new ArrayList<>();
         lowestTiers = new ArrayList<>();

         for (TieredItem item : tieredItems) {
             tieredItemList.add(item);

             if (item.getIndex() == -1)
                 lowestTiers.add(item);
         }

         for (ShopItem item : inventory) {

             itemAssociator.put(item.name,item);

             if (isNavigator(item)) {
                 navigators.add(item);
                 restrictedFileItems.add(item);
             }
         }

         for (TieredItem item : tieredItems) {
             if (item.getIsFileValid())
                 continue;

             restrictedFileItems.add(item.getItem());
         }
 }


    public static boolean isToolBetterThanPresent(TieredItem item, BattlePlayer player)
    {
        TieredCategory category = item.getCategory();
        TieredItem currentItem = null;
        switch (category) {
            case AXE:
                currentItem = player.getAxe();
                break;

            case PICK:
                currentItem = player.getPick();
                break;
        }
        if (currentItem == null)
        return true;
        else
            return item.getIndex() > currentItem.getIndex();

    }



    // sells an item. accounts for team enchants and inflation. also accounts for if the item is a placeholder.
    //also accounts for dream defenders.
    public static void sellItem(ShopItem item, BattlePlayer player, boolean isInflated, InventoryClickEvent event)
    {

        if (item==null)
            return;

        ItemCategory category = item.category;

        if (isPlaceHolder(category))
            return;

        if (category==ARMOR)
        {
            if (!isArmorBetter(getArmor(player.getRawPlayer()),item))
                return;
        }

        TieredItem possible = isTieredItem(item);

        HotbarManager manager = player.getBarManager();
        Inventory playerInv = player.getRawPlayer().getInventory();

        if (manager==null)
            return;

        if (possible != null)
        {
            if (!isToolBetterThanPresent(possible,player))
                return;
        }

        if (item==SHEARS && player.hasShears())
            return;


        ItemStack bought;


        switch (category)
        {
            case ARMOR:
            {
                ItemStack[] newArmor = inventoryItemToArmor(item, player);
                BattleEnchantment enchantment = player.getTeam().getArmorEnchant();

                if (newArmor==null)
                    return;

                if (didNotPay(player, item, isInflated))
                    return;

                if (enchantment!=null)
                {
                    for (int slot=0;slot< newArmor.length;slot++)
                        newArmor[slot] = enchant(newArmor[slot],enchantment);
                }

                player.setPurchasedArmor(possible == null ? player.getArmor() : possible);
                setArmor(newArmor, player.getRawPlayer());

            }
            break;

            //set(ItemStack item, ItemCategory category, Player player)
            case MELEE:
            {
                bought = toSoldItem(item,player);
                if (bought == null)
                    return;

                if (!roomAvailable(bought, playerInv)) {
                    player.sendMessage(ChatColor.RED+"Can't do an operation with a full inventory!");
                    return;
                }

                if (didNotPay(player, item, isInflated))
                    return;

                if (item== ShopItem.STICK)
                {
                    enchant(bought, Enchantment.KNOCKBACK, 1);
                    manager.set(bought,item,player.getRawPlayer(),event);
                   return;
                }
                BattleEnchantment enchantment = player.getTeam().getMeleeEnchant();



                if (enchantment==null)
                    manager.set(bought,item,player.getRawPlayer(),event);
                else
                    manager.set(enchant(bought,enchantment),item,player.getRawPlayer(),event);
            }
            break;

            default:
            {

                ///the item could be a tiered item, and a tool
                ItemStack boughtItem = toSoldItem(item, player);
                TieredItem associate = isTieredItem(item);
                TieredItem previousTier = getPreviousTier(associate);


                if (previousTier == null && associate == null) {
                    if (!roomAvailable(boughtItem, playerInv))
                    {
                        player.sendMessage(ChatColor.RED+"Inventory is full!");
                        return;
                    }
                }
                else {

                    if (previousTier == null) {
                        if (playerInv.firstEmpty() == -1) {
                            player.sendMessage(ChatColor.RED+"Inventory is full!");
                            return;
                        }
                    } else {


                        ItemStack previousTool = toSoldItem(previousTier.getItem(), player);
                        if (!playerInv.contains(previousTool)) {
                            player.sendMessage(ChatColor.RED + "You do not have the previous tier of the tool in your inventory!");
                            return;
                        }
                    }
                }




                if (didNotPay(player, item, isInflated))
                    return;


                //If it's a tool, then it's a tiered item
                if (possible != null)
                {
                    TieredCategory toolCategory = possible.getCategory();

                    switch (toolCategory) {
                        case PICK:
                            player.setPickUpwards(possible);
                            break;

                        case AXE:
                            player.setAxeUpwards(possible);
                    }
                }

                if (item==SHEARS) {
                    player.setShears();
                }

                manager.set(boughtItem, item, player.getRawPlayer(),event);
               }
            }

            player.sendMessage(ChatColor.GREEN+"You bought "+item.format());
        }


        /*
        this method checks to see if there is room in an inventory

        If there is an empty space: true
        Else,
        we check all items in the inventory to see if there are stacks which are similar
        if there are similar stacks, we check if there is a stack which can hold at least the stack provided.

         */
        public static boolean roomAvailable(ItemStack stack, Inventory inv){
            if (inv.firstEmpty() != -1)
                return true;

            for (ItemStack residing: inv.getContents()) {
                if (residing.isSimilar(stack)) {
                    int amountResiding = residing.getAmount();
                    int amountStack = stack.getAmount();

                    int max = stack.getMaxStackSize();

                    if (amountResiding + amountStack <= max)
                        return true;
                }
            }

            return false;
        }


   //the index is the string in the list that is targeted.
    //for all of the strings before it, they are highlighted.

    /*
    E.g

    Forge with 1 upgrade: index = 1
    A <-- 0
    B <-- 1
    C
    D


    Creates an item to be displayed in the team upgrades inventory shop.
    Adds lore and descriptions depending on the amount of upgrades that a player has purchased,
    if the item is tierable.
     */
        public static ItemStack toTeamDisplayItem(TeamItem item, int index)
        {
            Material mat = item.getMat();
            try {
                ItemStack sell;

                //if it is colored glass, then create it separately.
                if (item == TeamItem.SLOT_BARRIER)
                    sell = createColoredGlass(mat, DyeColor.GRAY);
                else
                    sell = new ItemStack(mat,1);

                if (sell == null)
                    return null;

                ItemMeta meta = sell.getItemMeta();

                //array of name descriptions that some items may have
                String[] names = item.getNames();

                //if the index is the max or over, then set it to the max tiered name, else
                //set it to the name of the current tier
                if (index >= names.length)
                    meta.setDisplayName(names[names.length-1]);
                else
                    meta.setDisplayName(names[index]);


                List<String> lore = new ArrayList<>();

                //descriptions for the tiers if applicable
                String[] descriptions = item.getLore();

                VALID_DESC:
                {

                    //checking for the description length. If there is no description, then break
                    if (descriptions.length < 1)
                        break VALID_DESC;

                    //if the description index is less than the tier, then
                    //the max tier is achieved.
                    //
                    if (descriptions.length - 1 < index)  //3, 2
                    {
                        for (String phrase : descriptions)
                            lore.add(ChatColor.GOLD + phrase);

                    } else {

                        //otherwise, we need to change the descriptions up to the index.
                        for (int slot = 0; slot < index; slot++) {
                            String phrase = descriptions[slot];
                            lore.add(ChatColor.GOLD + phrase);
                        }
                    }

                    //now, for the rest of the tiers not yet purchased, we set them to aqua instead of gold,
                    //so show that they are not purchased yet
                    for (int slot = index; slot < descriptions.length; slot++)
                        lore.add(ChatColor.AQUA + descriptions[slot]);
                }

                //set lore, meta, and return.
               meta.setLore(lore);
               sell.setItemMeta(meta);

               return sell;
            }
            catch (IndexOutOfBoundsException e)
            {
                return null;
            }
        }



        public static void clearAll(ItemStack toReplace, Inventory inv){

            if (isItemInvalid(toReplace))
                return;

            for (int slot=0;slot<inv.getSize();slot++)
            {
                ItemStack stack = inv.getItem(slot);

                if (isItemInvalid(stack))  {
                  continue;
                }

                if (stack.isSimilar(toReplace)) {
                    inv.setItem(slot, null);
                }
            }
        }

        public static void clearAll(Material mat, Inventory inv) {
            for (int slot=0;slot<inv.getSize();slot++)
            {
                ItemStack stack = inv.getItem(slot);

                if (isItemInvalid(stack))  {
                    continue;
                }

                if (stack.getType()==mat) {
                    inv.setItem(slot, null);
                }
            }
        }

        public static boolean isInventoryPlaceRestrict(ItemStack stack){
            if (isItemInvalid(stack)) {
                return false;
            }

            if (getNavigator(stack) != null)
                return true;

            if (isArmor(stack.getType()))
                return true;

            if ( stack.getType() == Material.SHEARS)
                return true;

            return isPick(stack) || isAxe(stack) || (isSword(stack.getType()) && isWoodenSimple(stack.getType()));
        }

        public static boolean isWoodenSimple(Material mat){
            return mat.name().toLowerCase().contains("wood");

        }

    //takes an inventory item and returns the item that is actually sold
    //Does not account for armor.
    //can return null
    @SuppressWarnings("deprecation")
    public static ItemStack toSoldItem(ShopItem item, BattlePlayer player)
    {
        if (item == null)
            return null;

        ItemStack stack = null;

        ItemCategory category = item.category;

        switch (category)
        {
            case ARMOR:
            case NONE:
            case NAV:
            case SEPARATOR: {

                //These aren't placed into the player inventory, but instead do stuff important,
                //so they're not "sold" here.
            }
                break;

            case POTION: {
                stack = getPotion(item);
            }
                break;

            case TOOLS: {
                stack = toDegradableTool(item);
            }
                break;

            case MELEE:
            {
                stack = hideFlags(setUnbreakable(new ItemStack(item.sellMaterial,item.sellAmount)));
                BattleEnchantment teamEnchant = player.getTeam().getMeleeEnchant();
                if (isSword(item.sellMaterial) && teamEnchant != null)
                    stack = enchant(stack,teamEnchant);
            }
                break;

            case RANGED: {
                stack = toRangedItem(item);
            }
                break;

            case BLOCK: {
                byte color = (byte) player.getTeam().getTeamColor().getValue();
                stack = addBlockColor(color, item);
            }
                break;

            default: {

                if (item== ShopItem.DREAM_DEFENDER)
                    stack = new ItemStack(item.sellMaterial,1,EntityType.IRON_GOLEM.getTypeId());
                else
                    stack = new ItemStack(item.sellMaterial, item.sellAmount);
            }
        }

        if (stack == null)
            return null;


       if (item.keepName)
          return addName(stack,item);
       return stack;
    }

    public static ItemStack toDegradableTool(ShopItem item)
    {
        if (item == null)
            return null;

        ItemCategory category = item.category;
        if (category != TOOLS)
            return null;

        ItemStack stack = new ItemStack(item.sellMaterial,item.sellAmount);
        stack = setUnbreakable(stack);

        switch (item)
        {

            //eff 1
            case WOOD_AXE:
            case WOODEN_PICKAXE:
            case STONE_AXE:
                stack = enchant(stack,EFFICIENCY_ONE);
                break;


            //eff 2
            case IRON_PICKAXE:
            case IRON_AXE:
                stack = enchant(stack,EFFICIENCY_TWO);
                break;

            //eff 3
            case DIAMOND_PICKAXE:
            case DIAMOND_AXE:
                stack = enchant(stack,EFFICIENCY_THREE);
                break;

            //eff + sharp
            case GOLD_PICKAXE:
                stack = enchant(stack,EFFICIENCY_THREE);
                stack = enchant(stack, SHARPNESS_TWO);
                break;

        }


        return hideFlags(stack);
    }




    //creates the item that is to be displayed in th e shop, with price depending on player number
    @SuppressWarnings("deprecation")
    public static ItemStack toDisplayItem(ShopItem item, boolean isInflated)
    {
        ItemCategory category = item.category;


        ItemStack stack;

        switch (category)
        {
            case POTION:
                stack = addLore(addName(getPotion(item),item),item,isInflated);
                break;

            case NAV:
                stack = addName(new ItemStack(item.sellMaterial,item.sellAmount),item);
                break;

            case TOOLS:
                stack = addLore(addName(toDegradableTool(item),item),item,isInflated);
                break;

            case NONE:
            case SEPARATOR:
                stack =  addName(createGlassPane(category),item);
                break;

            default: {

                stack = (item == ShopItem.DREAM_DEFENDER) ? addLore(addName(new ItemStack(item.sellMaterial, 1, EntityType.IRON_GOLEM.getTypeId()), item), item, isInflated)
                      : addLore(addName(new ItemStack(item.sellMaterial, item.sellAmount), item), item, isInflated);
            }
        }
      return stack;
    }


    //creates an item based on the inv.item provided. returns null otherwise.
    //accounts for enchants.
    public static ItemStack toRangedItem(ShopItem item)
    {
        ItemStack stack;
        if (item.category!=RANGED)
            return null;

        switch (item)
        {
            case BOW:
                stack = setUnbreakable(new ItemStack(item.sellMaterial,item.sellAmount));
                break;

            case PUNCH_BOW:
                stack = enchant(enchant(setUnbreakable(new ItemStack(item.sellMaterial, item.sellAmount)),PUNCH_ONE),POWER_ONE);
                break;

            case POW_BOW:
                stack = enchant(setUnbreakable(new ItemStack(item.sellMaterial,item.sellAmount)),POWER_ONE);
                break;

            default:
                stack = new ItemStack(item.sellMaterial,item.sellAmount);
        }
        return stack;
    }

    //adds color to blocks if they can be colored.
    public static ItemStack addBlockColor(byte color, ShopItem item)
    {
        ItemStack stack;

        switch (item)
        {
            case WOOL:
            case STAINED_GLASS:
            case HARDENED_CLAY:
                stack = new ItemStack(item.sellMaterial,item.sellAmount,color);
                break;

            default:
                stack = new ItemStack(item.sellMaterial, item.sellAmount);

        }
        return stack;

    }

    public static ItemStack createWool(byte color) {
            return new ItemStack(Material.WOOL, 1, color);
    }



    //creates a glass pane depending on category. returns null otherwise.
    @SuppressWarnings("deprecation")
    public static ItemStack createGlassPane(ItemCategory category)
    {

        if (category==NONE)
        {
            return new ItemStack(Material.STAINED_GLASS_PANE,1, DyeColor.RED.getData());
        }
        else if (category==SEPARATOR)
        {
            return new ItemStack(Material.STAINED_GLASS_PANE,1,DyeColor.BLACK.getData());
        }
        else
             return null;
    }

    @SuppressWarnings("deprecation")
    public static @Nullable ItemStack createColoredGlass(Material mat, DyeColor color){
            if (mat == STAINED_GLASS || mat == STAINED_GLASS_PANE)
                return new ItemStack(mat, 1, color.getData());
            return null;
    }

    //returns colored armor along with any enchantments a battle team has. does not set armor for the player
    public static ItemStack @Nullable [] inventoryItemToArmor(@NotNull ShopItem item,@NotNull BattlePlayer player)
    {
        if (item.category!=ARMOR)
            return null;

        Color color = player.getTeam().getTeamColor().getColor();

        ItemStack head;
        ItemStack chest;
        ItemStack legs;
        ItemStack boots;


        head = hideFlags(enchant(setLeatherColor(setUnbreakable(new ItemStack(LEATHER_HELMET)),color),AQUA));
        chest = hideFlags(setLeatherColor(setUnbreakable(new ItemStack(LEATHER_CHESTPLATE)), color));


        //set the armor to unbreakable first before adding enchants or color.

        switch (item)
        {
            case LEATHER_ARMOR:
                legs = hideFlags(setLeatherColor(setUnbreakable(new ItemStack(LEATHER_LEGGINGS)), color));
                boots = hideFlags(setLeatherColor(setUnbreakable(new ItemStack(LEATHER_BOOTS)), color));
                break;

            case CHAIN_MAIL:
                legs = hideFlags(setUnbreakable(new ItemStack(CHAINMAIL_LEGGINGS)));
                boots = hideFlags(setUnbreakable(new ItemStack(CHAINMAIL_BOOTS)));
                break;

            case IRON_ARMOR:
                legs  = hideFlags(setUnbreakable(new ItemStack(IRON_LEGGINGS)));
                boots = hideFlags(setUnbreakable(new ItemStack(IRON_BOOTS)));
                break;

            //diamond armor
            default:
                legs = hideFlags(setUnbreakable(new ItemStack(DIAMOND_LEGGINGS)));
                boots = hideFlags(setUnbreakable(new ItemStack(DIAMOND_BOOTS)));
        }


        return addArmorEnchants(orderArmor(head,chest,legs,boots),player.getTeam().getArmorEnchant());
    }


    public static boolean isArmor(Material mat)
    {
        switch (mat)
        {
            case LEATHER_BOOTS:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case LEATHER_CHESTPLATE:

            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:

            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:

            case DIAMOND_BOOTS:
            case DIAMOND_LEGGINGS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:

            case GOLD_BOOTS:
            case GOLD_CHESTPLATE:
            case GOLD_HELMET:
            case GOLD_LEGGINGS:
                return true;
        }
        return false;
    }

    public static boolean isAxe(ItemStack axe){
            if (isItemInvalid(axe))
                return false;
            Material mat = axe.getType();
            return mat.name().toLowerCase().contains("axe") && !(mat.name().toLowerCase().contains("pick"));
    }

    public static boolean isPick(ItemStack pick){
            if (isItemInvalid(pick))
                return false;
            Material mat = pick.getType();
            return mat.name().toLowerCase().contains("pick");
    }



    public static ItemStack getPotion(ShopItem item)
    {
        if (item.category!=ItemCategory.POTION)
            return null;

        Potion potion;
        ItemStack stack;
        switch (item)
        {
            case INVIS_POT: {
                potion = new Potion(PotionType.INVISIBILITY);
                stack = potion.toItemStack(item.sellAmount);
                PotionMeta meta = (PotionMeta) stack.getItemMeta();
                meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVIS_DURATION.getValue(), INVIS_LEVEL.getValue(), true), true);
                stack.setItemMeta(meta);
            }
            break;

            case JUMP_POT:
            {
                potion = new Potion(PotionType.JUMP);
                stack = potion.toItemStack(item.sellAmount);
                PotionMeta meta = (PotionMeta)stack.getItemMeta();
                meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, JUMP_DURATION.getValue(), JUMP_LEVEL.getValue(),true),true);
                stack.setItemMeta(meta);
            }
            break;

            default:
            {
                potion = new Potion(PotionType.FIRE_RESISTANCE);
                //Speed potions in hypixel have the fire resistance texture.

                stack = potion.toItemStack(item.sellAmount);
                PotionMeta meta = (PotionMeta)stack.getItemMeta();
                meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, SPEED_DURATION.getValue(), SPEED_LEVEL.getValue(),true),true);
                stack.setItemMeta(meta);
            }

        }
        return hideFlags(stack);
    }

    /*
    Removes an item from the list if it shouldn't be contained in the file, or if the slot value is invalid.
    @param original
     */
    public static List<ShopItemSet> filter(List<ShopItemSet> original)
    {
        original.removeIf(Objects::isNull);
        original.removeIf(item -> restrictedFileItems.contains(item.getItem()));
        original.removeIf(item -> !InventoryOperationHelper.isInQuickBuyPanel(item.getSlot()));

        return original;
    }

    public static boolean isSword(ShopItem item)
    {
        if (item == null)
            return false;

        return isSword(item.sellMaterial);
    }

    public static boolean isSword(Material mat)
    {
        if (mat == null)
            return false;

        return mat.name().toLowerCase().contains("sword");
    }


    public static int countSwords(Inventory inv){
        int swords = 0;
        for (ItemStack stack: inv.getContents()) {
            if (isItemInvalid(stack))
                continue;

            if (isSword(stack.getType()))
                swords ++;
        }

        return swords;
    }



     @Deprecated
    public static boolean isInQuickBuyRange(int slot)
    {
        return slot>=QUICK_INV_BORDER_START.getValue()&&slot<= QUICK_INV_BORDER_END.getValue();
    }


    //adds a specific enchantment to all items in the array
    public static ItemStack[] addArmorEnchants(ItemStack[] values, BattleEnchantment enchantment)
    {
        for (int slot=0;slot<values.length;slot++)
        {
            if (isItemInvalid(values[slot]))
                continue;
            values[slot] = enchant(values[slot],enchantment);
        }
        return values;
    }


    public static boolean isPlaceHolder(ItemCategory category)
    {
        return category==NONE||category==NAV||category==SEPARATOR;
    }

    public static boolean isNavigator(ShopItem item){
        return item != null && (item.category == NAV || item.name().toLowerCase().contains("nav"));
    }


    public static boolean isItemInvalid(ItemStack item)
    {
        return item==null||item.getItemMeta()==null||item.getType()==AIR;
    }



    //adds lore for cost and sell amount to an item
    public static ItemStack addLore(ItemStack item, ShopItem id, boolean isInflated)
    {

        if (isItemInvalid(item))
            return item;

        if (isPlaceHolder(id.category))
            return item;

        List<String> lore = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();

        lore.add(ChatColor.GOLD+COST.getPhrase()+" "+ (isInflated?id.inflatedPrice:id.cost)+" "+getPriceName(id));
        lore.add(ChatColor.AQUA+SELL.getPhrase()+" "+id.sellAmount);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }


    public static void addLore(ItemStack item, String lore) {

        if (isItemInvalid(item))
            return;

        if (lore == null)
            return;

        ItemMeta meta = item.getItemMeta();
        List<String> loreList = meta.getLore();

        if (loreList == null)
            loreList = new ArrayList<>();

        loreList.add(lore);
        meta.setLore(loreList);
        item.setItemMeta(meta);

          }


    /*
    Returns true if the item is not allowed to appear in configuration files, otherwise returns false.
     */
    public static boolean isFileRestricted(ShopItem item)
    {
        if (item == null)
            return true;

       TieredItem tier = isTieredItem(item);
       if (tier==null) {

           ItemCategory category = item.category;
           return category == NAV || category == SEPARATOR || category == OPERATOR;
       }
      return !tier.getIsFileValid();
    }

    public static ItemStack addName(ItemStack item, ShopItem id)
    {
        if (isItemInvalid(item))
            return item;

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(id.name);
        item.setItemMeta(meta);
        return item;
    }


    public static ItemStack addName(ItemStack item, String name) {
        if (isItemInvalid(item))
            return item;

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }


    public static TieredItem isTieredItem(ShopItem item)
    {
        for (TieredItem tier: tieredItemList)
        {
            if (tier.getItem()==item)
                return tier;
        }
        return null;
    }


    public static String getPriceName(ShopItem item)
    {
       return getPriceName(item.costMaterial);
    }

    public static String getPriceName(Material mat)
    {
        String name;
        switch (mat)
        {
            case IRON_INGOT:
                name = ChatColor.GRAY+"Iron Ingot";
                break;

            case GOLD_INGOT:
                name = ChatColor.YELLOW+"Gold Ingot";
                break;

            case DIAMOND:
                name = ChatColor.DARK_AQUA+"Diamond";
                break;

            case EMERALD:
                name = ChatColor.GREEN+"Emerald";
                break;

            default:
                name = ChatColor.MAGIC+"If you're reading this, you're awesome. :D";
        }
        return name;
    }


    public static net.minecraft.server.v1_8_R3.ItemStack toNMSItem(ItemStack item)
    {
        return CraftItemStack.asNMSCopy(item);
    }

    public static ItemStack toBukkitItem(net.minecraft.server.v1_8_R3.ItemStack item)
    {
        return CraftItemStack.asCraftMirror(item);
    }

    public static boolean isArmorBetter(ShopItem current, ShopItem isBetter)
    {
       TieredItem currentArmor = isTieredItem(current);
       TieredItem nextArmor = isTieredItem(isBetter);

       if (currentArmor==null||nextArmor==null)
           return false;
       return isArmorBetter(currentArmor,nextArmor);
    }


    public static boolean isArmorBetter(TieredItem current, TieredItem isBetter)
    {
        if (current.getCategory()!=isBetter.getCategory())
            return false;
        return isBetter.getIndex()>current.getIndex();
    }


    public static ShopItem getArmor(Player player)
    {
        StringBuilder complexity = new StringBuilder();
        ItemStack[] items = player.getInventory().getArmorContents();
        for (ItemStack item: items)
        {
            if (isItemInvalid(item))
                continue;

            Material mat = item.getType();

            if (isLeather(mat)) {
                complexity.append("l");
                continue;
            }

            if (isChain(mat)) {
                complexity.append("c");
                continue;
            }

            if (isIron(mat)) {
                complexity.append("i");
                continue;
            }

            if (isDiamond(mat))
                complexity.append("d");
        }
        return analyzeComplexity(complexity.toString());
    }

    private static ShopItem analyzeComplexity(String string)
    {
        if (string.contains("d"))
            return ShopItem.DIAMOND_ARMOR;
        else if (string.contains("i"))
            return ShopItem.IRON_ARMOR;
        else if (string.contains("c"))
            return ShopItem.CHAIN_MAIL;
        else
            return ShopItem.LEATHER_ARMOR;
    }

    private static boolean isLeather(Material mat)
    {
       switch (mat)
       {
           case LEATHER_BOOTS:
           case LEATHER_HELMET:
           case LEATHER_LEGGINGS:
           case LEATHER_CHESTPLATE:
               return true;
       }
       return false;
    }

    private static boolean isIron(Material mat)
    {
        switch (mat)
        {
            case IRON_BOOTS:
            case IRON_LEGGINGS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
                return true;
        }
        return false;
    }

    private static boolean isChain(Material mat)
    {
        switch (mat)
        {
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_CHESTPLATE:
                return true;
        }
        return false;
    }

    private static boolean isDiamond(Material mat)
    {
        switch (mat)
        {
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                return true;
        }
        return false;
    }


    //orders itemstacks determinant on armor format.
    public static ItemStack[] orderArmor(ItemStack head, ItemStack chest, ItemStack legs, ItemStack boots)
    {
        return new ItemStack[] {boots, legs, chest, head};
    }


    //If the stack is used as currency
    public static boolean isCurrencyItem(ItemStack stack)
    {
        if (stack == null)
            return false;

        Material mat = stack.getType();
        return mat == IRON_INGOT || mat == GOLD_INGOT || mat == EMERALD || mat == DIAMOND;
    }

    public static void setArmor(ItemStack[] items, Player player)
    {
       player.getInventory().setArmorContents(items);
    }



    public static TieredItem getNextTier(TieredItem current)
    {
        for (TieredItem item: tieredItemList)
        {
            if ((item.getCategory()==current.getCategory()) && (item.getIndex()==(current.getIndex()+1)))
                return item;
        }
        return null;
    }

    public static TieredItem getPreviousTier(TieredItem current)
    {
        if (current == null)
            return null;

        for (TieredItem item: tieredItemList)
        {
            if ((item.getCategory()==current.getCategory()) && (item.getIndex()==(current.getIndex()-1)) )
                return item;
        }
        return null;
    }



    public static ItemStack hideFlags(ItemStack item)
    {
        if (isItemInvalid(item))
            return item;

       ItemMeta meta = item.getItemMeta();
       meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
       meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
       meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
       item.setItemMeta(meta);
       return item;
    }

    /*
    Enchants an item with a battle enchantment. Accounts for if it is null.
     */
    public static ItemStack enchant(ItemStack stack, BattleEnchantment enchantment)
    {
        return enchantment == null? stack: enchant(stack, enchantment.getEnchantmentType(), enchantment.getMagnitude());
    }

    public static ItemStack enchant(ItemStack stack, Enchantment enchantment, int level)
    {
        if (isItemInvalid(stack))
            return stack;

        stack.addUnsafeEnchantment(enchantment,level);
        return stack;
    }



    //Try to associate an itemstack to a game item.
    //Note that the empty slot game item has a sell item of glass pane , while the rest have sell item of air.
    public static ShopItem getAssociate(ItemStack stack)
    {
        if (isItemInvalid(stack))
            return null;

        ItemMeta meta = stack.getItemMeta();
        if (meta.getDisplayName()!=null) {

            ShopItem navigated = itemAssociator.getOrDefault(meta.getDisplayName(),null);
            if (navigated != null && navigated.sellMaterial == stack.getType()) {
                return navigated;
            }
        }

        Material mat = stack.getType();
        for (ShopItem item: ShopItem.values())
            if (mat == item.sellMaterial)
                return item;

        return null;

    }

    /*
     Get the associated item for packaging the inventory for writing to files
     */
    public static ShopItem getPackingAssociate(ItemStack stack){
        if (isItemInvalid(stack))
            return ShopItem.EMPTY_SLOT;

        String name = stack.getItemMeta().getDisplayName();
        if (name==null)
            return ShopItem.EMPTY_SLOT;

        return itemAssociator.getOrDefault(name, ShopItem.EMPTY_SLOT);
    }

    public static @Nullable ShopItem getNavigator(ItemStack stack){

        if (isItemInvalid(stack))
            return null;

        for (ShopItem item: navigators) {
            if (item.sellMaterial != stack.getType())
                continue;

            String name = stack.getItemMeta().getDisplayName();

            if (name==null)
                continue;

            if (name.equals(item.name))
                return item;

        }
        return null;
    }




    public static ItemStack toBarItem(ItemCategory category){
        if (category == null)
            return null;

        for (HotBarConfig config: HotBarConfig.values()) {
            if (config.getCategory()==category)
                return toSimpleItem(config.getMat(),config.getName());
        }
        return null;
    }

    public static ItemCategory getHotBarAssociate(ItemStack stack){
        if (isItemInvalid(stack))
            return null;

        Material mat = stack.getType();
        for (HotBarConfig config: HotBarConfig.values()) {
            if (config.getMat()==mat)
                return config.getCategory();
        }
        return null;
    }

    public static ItemStack setLeatherColor(ItemStack stack, Color color)
    {
        if (isItemInvalid(stack)) {
            return stack;
        }

        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
            meta.setColor(color);
            stack.setItemMeta(meta);
            return stack;
        }
        catch (ClassCastException e)
        {
            return stack;
        }
    }



    public static ItemStack toSimpleItem(Material mat, String name){
        ItemStack stack = new ItemStack(mat, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }




    public static TieredItem getLowestTier(TieredItem item)
    {
        for (TieredItem current: lowestTiers)
        {
            if (item.getCategory()==current.getCategory())
                return current;
        }
        return item;
    }

    private static ItemStack setUnbreakable(ItemStack item)
    {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = toNMSItem(item);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("Unbreakable",true);
        nmsStack.setTag(compound);
        item = toBukkitItem(nmsStack);
        return item;
    }

    //attempts to do a transaction with a player.
    public static boolean didNotPay(BattlePlayer current, ShopItem item, boolean isInflated)
    {
        return !didPay(current, item.costMaterial, isInflated ? item.inflatedPrice : item.cost);
    }

    public static boolean didPay(BattlePlayer current, Material priceMaterial, int price)
    {
        Player player = current.getRawPlayer();
        ItemStack airHold = new ItemStack(Material.AIR, 1);

        if (!player.getInventory().contains(priceMaterial, price)) //if the inv contains the price
        {
            int below = 0;
            for (int slot = 0; slot < player.getInventory().getSize(); slot++)
            {
                if (isItemInvalid(player.getInventory().getItem(slot)))
                    continue;

                ItemStack currentItem = player.getInventory().getItem(slot);

                if (currentItem.isSimilar(new ItemStack(priceMaterial,1)))
                    below = below + currentItem.getAmount();
            }

            playSound(false,player);
            player.sendMessage(ChatColor.RED+"Don't have enough "+getPriceName(priceMaterial)+"s! Need "+(price-below)+" more!");
            return false;
        }

       final ItemStack PRICE_ITEM = new ItemStack(priceMaterial,1);

            int paid = 0;
            for (int slot = 0; slot < player.getInventory().getSize(); slot++)
            {
                if (isItemInvalid(player.getInventory().getItem(slot)))
                    continue;
                ItemStack currentItem = player.getInventory().getItem(slot);
                if (!currentItem.isSimilar(PRICE_ITEM))
                 continue;

                int amount = currentItem.getAmount();

                while (amount > 0) {
                    paid++;
                    amount--;
                    currentItem.setAmount(amount);

                    if (amount >= 1)
                        player.getInventory().setItem(slot, currentItem);
                    else
                        player.getInventory().setItem(slot, airHold);

                    if (paid >= price) {
                        playSound(true,player);
                        return true;

                    }
                }
            }
            return false;
    }




    private static void playSound(boolean didTrade, Player player)
    {
        PacketSound soundPair = didTrade ? PacketSound.PLING : PacketSound.ENDERMAN;
        Location loc = player.getLocation();
        PacketPlayOutNamedSoundEffect effect = new PacketPlayOutNamedSoundEffect(soundPair.getSoundName(),loc.getX(),loc.getY(),loc.getZ(),1, soundPair.getPitch());
        sendPacket(player,effect);

    }

    private static void sendPacket(Player player, PacketPlayOutNamedSoundEffect packet)
    {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }


    public static int getPresent(Material mat, Inventory inv){

        int slots = 0;
        for (int slot=0;slot<inv.getSize();slot++) {
            ItemStack stack = inv.getItem(slot);
            if (isItemInvalid(stack))
                continue;

            if (stack.getType()==mat)
                slots++;
        }
        return slots;
    }


}
