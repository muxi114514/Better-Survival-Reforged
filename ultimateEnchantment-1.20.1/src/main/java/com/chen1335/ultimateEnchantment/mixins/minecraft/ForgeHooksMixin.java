package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.data.LootProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public class ForgeHooksMixin {
    @Inject(method = "modifyLoot(Lnet/minecraft/resources/ResourceLocation;Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private static void modifyLoot(ResourceLocation lootTableId, ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        if (!UltimateEnchantment.canBoosDropEnchantmentBook || lootTableId == null) {
            return;
        }

        if (lootTableId.equals(new ResourceLocation("entities/ender_dragon"))) {
            context.getLevel().getServer().getLootData().getLootTable(LootProvider.ENDER_DRAGON_LOOT_ADDITION).getRandomItems(context, cir.getReturnValue()::add);
        }
        if (lootTableId.equals(new ResourceLocation("entities/wither"))) {
            context.getLevel().getServer().getLootData().getLootTable(LootProvider.WITHER_LOOT_ADDITION).getRandomItems(context, cir.getReturnValue()::add);
        }
        if (lootTableId.equals(new ResourceLocation("entities/warden"))) {
            context.getLevel().getServer().getLootData().getLootTable(LootProvider.WARDEN_LOOT_ADDITION).getRandomItems(context, cir.getReturnValue()::add);
        }
    }
}
