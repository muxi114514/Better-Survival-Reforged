package com.mx.bettersurvival.combat;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 持盾攻击的武器黑/白名单匹配器（单一职责：把配置表编译成可查询的匹配器）。
 *
 * <p>由 {@link ModConfig.ClientConfig} 的 {@code shieldAttack*} 项驱动，规则三种：
 * <ul>
 *   <li>{@code #命名空间:标签} —— 整类（如 {@code #spartanweaponry:sabers}）；</li>
 *   <li>{@code 命名空间:含*?通配} —— 按注册 ID 通配（如 {@code bettersurvival:*nunchaku}）；</li>
 *   <li>{@code 命名空间:物品} —— 精确单个（如 {@code minecraft:trident}）。</li>
 * </ul>
 *
 * <p>空表 + 黑名单 = 全部放行（保持默认行为）。解析一次并缓存，配置（重）加载时失效重建；
 * 结果字段不可变、经 {@code volatile} 发布，读多写少线程安全。
 */
@Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ShieldAttackFilter {

    private static volatile ShieldAttackFilter cache;

    private final boolean whitelist;
    private final boolean allowEmptyHand;
    private final Set<ResourceLocation> exact;
    private final List<Pattern> globs;
    private final List<TagKey<Item>> tags;

    private ShieldAttackFilter(boolean whitelist, boolean allowEmptyHand,
            Set<ResourceLocation> exact, List<Pattern> globs, List<TagKey<Item>> tags) {
        this.whitelist = whitelist;
        this.allowEmptyHand = allowEmptyHand;
        this.exact = exact;
        this.globs = globs;
        this.tags = tags;
    }

    /** 获取当前匹配器（懒构建 + 缓存）。 */
    public static ShieldAttackFilter get() {
        ShieldAttackFilter f = cache;
        if (f == null) {
            f = build();
            cache = f;
        }
        return f;
    }

    /** 配置（重）加载时使缓存失效，下次 {@link #get()} 重建。 */
    @SubscribeEvent
    static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() == ModConfig.CLIENT_SPEC) {
            cache = null;
        }
    }

    /** 主手武器是否允许在持盾防御时攻击。 */
    public boolean allows(ItemStack mainHand) {
        if (mainHand.isEmpty()) {
            return allowEmptyHand;
        }
        boolean matched = false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(mainHand.getItem());
        if (id != null) {
            if (exact.contains(id)) {
                matched = true;
            } else {
                String s = id.toString();
                for (Pattern p : globs) {
                    if (p.matcher(s).matches()) {
                        matched = true;
                        break;
                    }
                }
            }
        }
        if (!matched) {
            for (TagKey<Item> tag : tags) {
                if (mainHand.is(tag)) {
                    matched = true;
                    break;
                }
            }
        }
        // 白名单：命中才放行；黑名单：命中才拦。
        return whitelist == matched;
    }

    private static ShieldAttackFilter build() {
        boolean wl = ModConfig.CLIENT.shieldAttackWhitelist.get();
        boolean eh = ModConfig.CLIENT.shieldAttackAllowEmptyHand.get();
        Set<ResourceLocation> exact = new HashSet<>();
        List<Pattern> globs = new ArrayList<>();
        List<TagKey<Item>> tags = new ArrayList<>();
        for (String raw : ModConfig.CLIENT.shieldAttackList.get()) {
            String s = raw == null ? "" : raw.trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                if (s.startsWith("#")) {
                    tags.add(TagKey.create(Registries.ITEM, new ResourceLocation(s.substring(1))));
                } else if (s.indexOf('*') >= 0 || s.indexOf('?') >= 0) {
                    globs.add(Pattern.compile(globToRegex(s)));
                } else {
                    exact.add(new ResourceLocation(s));
                }
            } catch (RuntimeException e) {
                BetterSurvival.LOGGER.warn("[BetterSurvival] 忽略无效的持盾攻击名单项 \"{}\": {}", s, e.getMessage());
            }
        }
        return new ShieldAttackFilter(wl, eh, exact, globs, tags);
    }

    /** glob（{@code *} 任意串 / {@code ?} 单字符）转正则，转义其余元字符。 */
    private static String globToRegex(String glob) {
        StringBuilder sb = new StringBuilder(glob.length() + 8);
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> sb.append(".*");
                case '?' -> sb.append('.');
                default -> {
                    if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
                        sb.append('\\');
                    }
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
