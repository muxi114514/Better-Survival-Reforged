package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, BetterSurvival.MOD_ID);

    public static final RegistryObject<CreativeModeTab> BETTER_SURVIVAL_TAB = CREATIVE_TABS.register(
            "bettersurvival_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + BetterSurvival.MOD_ID))
                    .icon(() -> Items.DIAMOND_SWORD.getDefaultInstance())
                    .displayItems((parameters, output) -> {

                        // gold dragon bone weapons stay out of the tab, like Ice and Fire's hidden gold dragon items
                        ModItems.ITEMS.getEntries().stream()
                                .filter(entry -> !com.mx.bettersurvival.integration.IaFCompat.isHiddenGoldItem(entry.getId().getPath()))
                                .forEach(entry -> output.accept(entry.get()));
                    })
                    .build());
}
