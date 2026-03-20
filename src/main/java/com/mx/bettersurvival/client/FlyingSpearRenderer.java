package com.mx.bettersurvival.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mx.bettersurvival.entities.FlyingSpearEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Renderer for the flying spear entity.
 * Renders the spear's item model rotated to look like a projectile in flight.
 * Ported from 1.12's RenderFlyingSpear using PoseStack instead of
 * GlStateManager.
 */
public class FlyingSpearRenderer extends EntityRenderer<FlyingSpearEntity> {

    public FlyingSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull FlyingSpearEntity entity, float entityYaw, float partialTicks,
            @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight) {
        ItemStack spear = entity.getSpear();
        if (spear.isEmpty())
            return;

        poseStack.pushPose();
        {
            // Rotate to match entity's yaw and pitch (projectile flight direction)
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
            poseStack.mulPose(Axis.XN.rotationDegrees(entity.getXRot()));
            // Rotate -90° Y so the item faces the flight direction, then -45° Z for tilt
            poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZN.rotationDegrees(45.0F));

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    spear, ItemDisplayContext.NONE, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        }
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull FlyingSpearEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
