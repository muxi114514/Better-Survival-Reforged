package com.mx.bettersurvival.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

/**
 * 自定义盾牌（移植自 1.12 BetterSurvival 的 ItemCustomShield）。
 *
 * <p>两把：小盾（blockPower 0.5 / weight 1）、大盾（blockPower 0.8 / weight 3）。
 * 手动实现举盾（{@link UseAnim#BLOCK} + {@link ToolActions#SHIELD_BLOCK}），
 * 从而复用原版方向格挡与 {@code ShieldBlockEvent}；具体减伤/被动/重量逻辑在事件处理器里。
 */
public class CustomShieldItem extends Item {

    /** 盾牌专属附魔分类：仅匹配本模组盾牌。 */
    public static final EnchantmentCategory SHIELD_CATEGORY =
            EnchantmentCategory.create("bettersurvival_shield", item -> item instanceof CustomShieldItem);

    private final float blockPower; // 主动格挡时挡下的伤害比例
    private final int weight;       // 重量档，越大移速惩罚越强、耐久越高、附魔值越低

    public CustomShieldItem(float blockPower, int weight, Properties properties) {
        super(properties.durability(250 * weight));
        this.blockPower = blockPower;
        this.weight = weight;
    }

    public float getBlockPower() {
        return blockPower;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.SHIELD_BLOCK.equals(toolAction);
    }

    @Override
    public int getEnchantmentValue() {
        return Math.max(1, 30 - 5 * weight);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.IRON_INGOT);
    }
}
