package com.mx.bettersurvival.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;

/**
 * Tells a real melee swing apart from other damage (projectiles, magic, effect ticks, chain lightning).
 *
 * Mods like RLCombat give each weapon its own melee damage type (rlcombat:slashing etc.) instead of
 * player_attack, so player_attack alone misses those hits. Progressive Mechanics Library collects every
 * melee damage type in the pml:is_melee_attack tag; Spartan Weaponry and JLME use the same tag, so
 * packs only have to list their melee types in one place.
 */
public final class MeleeHits {

    /** Progressive Mechanics Library's melee damage type tag (just data, so PML doesn't have to be installed). */
    public static final TagKey<DamageType> PML_MELEE_ATTACK =
            TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("pml", "is_melee_attack"));

    private MeleeHits() {
    }

    public static boolean isMeleeHit(DamageSource source) {
        if (source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK))
            return true;
        // Same rule PML uses: the attacker itself landed the hit (not a projectile it fired).
        Entity attacker = source.getEntity();
        return attacker != null && source.getDirectEntity() == attacker && source.is(PML_MELEE_ATTACK);
    }
}
