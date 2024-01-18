package me.camm.productions.bedwars.Items.ItemProperties;


/**
 * @author CAMM
 * Categories for tiered items
 */
public enum TieredCategory
{
    AXE("Axe"),
    SWORD("Sword"),
    PICK("Pickaxe"),
    ARMOR("Armor");

    private final String value;

    TieredCategory(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}

    /*
      WOOD_PICK
      IRON_PICK
      GOLD_PICK
      DIAMOND_PICK

     WOOD_AXE
     STONE_AXE
     IRON_AXE
     DIAMOND_AXE

     WOODEN_SWORD
     STONE_SWORD
     IRON_SWORD
     DIAMOND_SWORD

     CHAIN_MAIL
     IRON_ARMOR
     DIAMOND_ARMOR
     */