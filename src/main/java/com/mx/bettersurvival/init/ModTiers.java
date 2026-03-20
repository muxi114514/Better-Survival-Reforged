package com.mx.bettersurvival.init;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

/**
 * Custom tool tiers for BetterSurvival weapons that don't come from IaF.
 */
public class ModTiers {

    /**
     * Emerald — Diamond-grade attack, higher enchantability, slightly lower
     * durability.
     * 
     * <pre>
     * harvestLevel: 3 (same as diamond)
     * durability:   1200
     * speed:        9.0
     * damage:       3.0 (same as diamond)
     * enchantability: 20 (higher than diamond's 10)
     * repair: emerald
     * </pre>
     */
    public static final ForgeTier EMERALD = new ForgeTier(
            3, // harvest level
            1200, // durability
            9.0F, // speed
            3.0F, // attackDamageBonus
            20, // enchantability
            BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.of(Items.EMERALD));

    /**
     * Crying Obsidian — Between diamond and netherite, heavy and durable.
     * 
     * <pre>
     * harvestLevel: 3
     * durability:   1800
     * speed:        7.0 (slow, heavy feel)
     * damage:       3.5 (diamond 3.0 < this < netherite 4.0)
     * enchantability: 12
     * repair: crying obsidian
     * </pre>
     */
    public static final ForgeTier CRYING_OBSIDIAN = new ForgeTier(
            3, // harvest level
            1800, // durability
            7.0F, // speed
            3.5F, // attackDamageBonus
            12, // enchantability
            BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.of(Items.CRYING_OBSIDIAN));
}
