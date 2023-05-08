package me.camm.productions.bedwars.Items.ItemProperties;

import org.bukkit.enchantments.Enchantment;


/**
 * @author CAMM
 * enum for enchantments that the player can get
 */
public enum BattleEnchantment
{
    PROTECTION_ONE(Enchantment.PROTECTION_ENVIRONMENTAL,1),
    PROTECTION_TWO(Enchantment.PROTECTION_ENVIRONMENTAL,2),
    PROTECTION_THREE(Enchantment.PROTECTION_ENVIRONMENTAL,3),
    PROTECTION_FOUR(Enchantment.PROTECTION_ENVIRONMENTAL,4),

    EFFICIENCY_ONE(Enchantment.DIG_SPEED,1),
    EFFICIENCY_TWO(Enchantment.DIG_SPEED,2),
    EFFICIENCY_THREE(Enchantment.DIG_SPEED,3),

    SHARPNESS_ONE(Enchantment.DAMAGE_ALL,1),
    SHARPNESS_TWO(Enchantment.DAMAGE_ALL,2),
    SHARPNESS_THREE(Enchantment.DAMAGE_ALL,3),

    POWER_ONE(Enchantment.ARROW_DAMAGE,1),
    POWER_TWO(Enchantment.ARROW_DAMAGE,2),

    PUNCH_ONE(Enchantment.ARROW_KNOCKBACK,1),
    PUNCH_TWO(Enchantment.ARROW_KNOCKBACK,2),

    KNOCKBACK(Enchantment.KNOCKBACK,1),
    AQUA(Enchantment.WATER_WORKER,1);

    private final Enchantment enchantment;
    private final int magnitude;

    BattleEnchantment(Enchantment enchantment, int magnitude)
    {
        this.magnitude = magnitude;
        this.enchantment = enchantment;
    }

    public int getMagnitude()
    {
        return magnitude;
    }

    public Enchantment getEnchantmentType()
    {
        return enchantment;
    }
}
