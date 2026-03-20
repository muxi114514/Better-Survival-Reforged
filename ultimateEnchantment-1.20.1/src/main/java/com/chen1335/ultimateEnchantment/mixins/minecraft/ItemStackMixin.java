package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.enchantment.EnchantmentUtils;
import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IForgeItemStack {

    @Shadow
    public abstract boolean isDamageableItem();

    @Shadow
    public abstract Item getItem();


    @Unique
    private static int ue$levelAdditionInToolTip;

    @Inject(method = "appendEnchantmentNames", at = @At(value = "HEAD"))
    private static void appendEnchantmentNames(List<Component> list, ListTag listTag, CallbackInfo ci) {
        ue$levelAdditionInToolTip = 0;
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundtag = listTag.getCompound(i);
            if (Objects.equals(EnchantmentHelper.getEnchantmentId(compoundtag), Enchantments.ULTIMATE.getId())) {
                ue$levelAdditionInToolTip = EnchantmentHelper.getEnchantmentLevel(compoundtag);
            }
        }
    }


    @ModifyArg(method = "lambda$appendEnchantmentNames$5", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/nbt/CompoundTag;)I"))
    private static CompoundTag lambda$appendEnchantmentNames$5(CompoundTag pCompoundTag) {
        CompoundTag compoundTag = pCompoundTag.copy();
        if (Objects.equals(EnchantmentHelper.getEnchantmentId(compoundTag), Enchantments.ULTIMATE.getId())) {
            return pCompoundTag;
        }

        @NotNull Optional<Holder<Enchantment>> holder = ForgeRegistries.ENCHANTMENTS.getHolder(EnchantmentHelper.getEnchantmentId(compoundTag));

        if (holder.isPresent()) {
            if (holder.get().value().getMaxLevel() == 1) {
                return compoundTag;
            }
        }


        EnchantmentHelper.setEnchantmentLevel(compoundTag, EnchantmentHelper.getEnchantmentLevel(compoundTag) + ue$levelAdditionInToolTip);
        return compoundTag;
    }


    @Inject(method = "enchant", at = @At("RETURN"))
    private void enchant(Enchantment p_41664_, int p_41665_, CallbackInfo ci) {
        EnchantmentUtils.sortInItem((ItemStack) (Object) this);
    }

    @Inject(method = "setDamageValue", at = @At("RETURN"))
    private void setDamageValue(int pDamage, CallbackInfo ci) {
        if (!Enchantments.ETERNAL.isPresent()) {
            return;
        }
        if (this.getEnchantmentLevel(Enchantments.ETERNAL.get()) > 0) {
            if (this.isDamageableItem()) {
                this.getItem().setDamage((ItemStack) (Object) this, 0);
            }
        }
    }
}
