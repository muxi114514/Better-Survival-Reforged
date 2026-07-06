package com.mx.bettersurvival.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 自定义盾牌（移植自 1.12 BetterSurvival 的 ItemCustomShield）。
 *
 * <p>两把：小盾（blockPower 0.5 / weight 1）、大盾（blockPower 0.8 / weight 3）。
 *
 * <p><b>直接继承原版 {@link ShieldItem}</b>（而非 {@code extends Item} 再手动照抄举盾方法）：
 * 这样在类型系统里"真的是盾"，凡是按 {@code instanceof ShieldItem} 判定的第三方模组
 * （更好战斗等战斗类 mod 接管格挡逻辑时正是这样判断）都会正确识别本模组盾牌，
 * 从而正常挡伤害、播放盾牌音效。格挡姿势 / 使用时长 / 右键举盾 / {@code SHIELD_BLOCK}
 * 工具动作全部复用原版实现；具体减伤 / 被动 / 重量逻辑仍在事件处理器里。
 */
public class CustomShieldItem extends ShieldItem {

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

    // 盾牌可附魔（原版 ShieldItem 附魔值为 0，这里放开并按重量递减）
    @Override
    public int getEnchantmentValue() {
        return Math.max(1, 30 - 5 * weight);
    }

    // 铁锭修复（覆盖原版的木板修复）
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.IRON_INGOT);
    }
}
