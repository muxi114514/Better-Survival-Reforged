package com.mx.bettersurvival.integration;

import com.kettle.pml.core.DamageControl;
import com.kettle.pml.util.AdvancedDamageSource;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Progressive Mechanics Library hooks. Only touched when PML is loaded (BetterSurvival.isPmlLoaded).
 *
 * PML gives every damage source a DamageControl and applies it at LOW priority:
 * (damage + base flat bonuses) * base percent, then flat and percent bonuses. Spartan Weaponry traits and
 * JLME enchantments add their bonuses there, so Better Survival does too instead of editing the event's
 * amount directly; that way all weapon bonuses stack by the same rules.
 */
public final class PmlCompat {

    private PmlCompat() {
    }

    /** Adds a flat weapon bonus (e.g. +8 vs dragons). Returns false if PML isn't tracking this source. */
    public static boolean addBaseFlatBonus(DamageSource source, float amount) {
        DamageControl control = control(source);
        if (control == null)
            return false;
        control.addBaseFlatBonus(amount);
        return true;
    }

    /** Applies a weapon damage multiplier (e.g. backstab x2) as a base percent bonus. */
    public static boolean addBaseMultiplier(DamageSource source, float multiplier) {
        DamageControl control = control(source);
        if (control == null)
            return false;
        control.addBasePercentBonus(multiplier - 1.0F);
        return true;
    }

    private static DamageControl control(DamageSource source) {
        return source instanceof AdvancedDamageSource ads ? ads.getDamageControl() : null;
    }
}
