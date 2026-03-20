package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.effect.MobEffects;
import com.chen1335.ultimateEnchantment.effect.UnActiveVanquisher;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    public abstract Font getFont();

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderEffects(GuiGraphics pGuiGraphics, CallbackInfo ci, Collection collection, Screen $$4, int j1, int k1, MobEffectTextureManager mobeffecttexturemanager, List list, Iterator var8, MobEffectInstance mobeffectinstance, MobEffect mobeffect, IClientMobEffectExtensions renderer, int i, int j, float f, TextureAtlasSprite textureatlassprite, int i1, float f1, int i_f) {
        if (mobeffect == MobEffects.UN_ACTIVE_VANQUISHER.get()) {
            UnActiveVanquisher.renderLevel(pGuiGraphics, this.getFont(), i_f + 3, i1 + 3, mobeffectinstance);
        }
    }

}
