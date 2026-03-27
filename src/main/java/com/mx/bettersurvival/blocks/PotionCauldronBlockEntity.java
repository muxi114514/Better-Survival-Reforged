package com.mx.bettersurvival.blocks;

import com.mx.bettersurvival.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public class PotionCauldronBlockEntity extends BlockEntity {

    private Potion storedPotion = Potions.EMPTY;
    private boolean isMilk = false;

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POTION_CAULDRON.get(), pos, state);
    }

    public Potion getStoredPotion() {
        return storedPotion;
    }

    public void setStoredPotion(Potion potion) {
        this.storedPotion = potion;
        this.isMilk = false;
        setChanged();
    }

    public boolean isMilk() {
        return isMilk;
    }

    public void setMilk(boolean milk) {
        this.isMilk = milk;
        if (milk)
            this.storedPotion = Potions.EMPTY;
        setChanged();
    }

    public List<MobEffectInstance> getEffects() {
        if (isMilk || storedPotion == Potions.EMPTY)
            return Collections.emptyList();
        return storedPotion.getEffects();
    }

    public ItemStack createPotionStack() {
        if (storedPotion == Potions.EMPTY)
            return ItemStack.EMPTY;
        return PotionUtils.setPotion(new ItemStack(Items.POTION), storedPotion);
    }

    public ItemStack createTippedArrow() {
        if (storedPotion == Potions.EMPTY)
            return ItemStack.EMPTY;
        return PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), storedPotion);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Potion", net.minecraft.core.registries.BuiltInRegistries.POTION.getKey(storedPotion).toString());
        tag.putBoolean("IsMilk", isMilk);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("IsMilk")) {
            isMilk = tag.getBoolean("IsMilk");
        }
        if (tag.contains("Potion")) {
            storedPotion = net.minecraft.core.registries.BuiltInRegistries.POTION
                    .get(new net.minecraft.resources.ResourceLocation(tag.getString("Potion")));
            if (storedPotion == null)
                storedPotion = Potions.EMPTY;
        }
    }
}
