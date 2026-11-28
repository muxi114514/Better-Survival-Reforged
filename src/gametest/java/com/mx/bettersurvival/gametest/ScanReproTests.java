package com.mx.bettersurvival.gametest;

import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.enchantments.VitalityEnchantment;
import com.mx.bettersurvival.init.ModBlocks;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.init.ModItems;
import com.mx.bettersurvival.init.ModMobEffects;
import com.mx.bettersurvival.blocks.PotionCauldronBlockEntity;
import com.mx.bettersurvival.network.SpinningPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Reproduces the findings of the Nov 28 2026 bug scan (bug-report.md). Each test asserts the CORRECT behaviour, so a
 * failing test means that finding reproduced. #4 (hammer slam hits count as melee hits) is kept as a feature and not
 * tested; #7 (Heavy) didn't reproduce on this branch. Not tested here: #8/#20 (config design, needs a client), #13 (design choice),
 * #14 (dead code, no effect), #18 (client side), #21/#22 (performance).
 */
@GameTestHolder("bettersurvival")
@PrefixGameTestTemplate(false)
public class ScanReproTests {
    private static final String T = "empty";

    // ---------- #1: hammer slam in a protected area ----------

    /** A claim mod cancels every break in the test area: the slam must not crush stone or break glass there. */
    @GameTest(template = T)
    public static void hammerRespectsProtection(GameTestHelper h) {
        stoneFloor(h);
        TestKit.Mock m = TestKit.player(h, 5, 1, 3, GameType.SURVIVAL, 0);
        BlockPos glass = h.absolutePos(new BlockPos(5, 1, 6));
        h.getLevel().setBlockAndUpdate(glass, Blocks.GLASS.defaultBlockState());
        AABB area = new AABB(h.absolutePos(BlockPos.ZERO), h.absolutePos(new BlockPos(11, 5, 11)));
        Consumer<BlockEvent.BreakEvent> claim = e -> { if (area.contains(Vec3.atCenterOf(e.getPos()))) e.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, claim);
        try {
            ItemStack hammer = new ItemStack(item("itemironhammer"));
            m.player().setItemInHand(InteractionHand.MAIN_HAND, hammer);
            BlockPos floor = h.absolutePos(new BlockPos(5, 0, 5));
            hammer.getItem().useOn(new UseOnContext(m.player(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false)));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(claim);
        }
        int crushed = crushedCount(h);
        boolean glassLeft = h.getLevel().getBlockState(glass).is(Blocks.GLASS);
        TestKit.remove(h, m);
        TestKit.result(h, "#1 hammer protection", crushed == 0 && glassLeft,
                crushed + " floor blocks crushed, glass " + (glassLeft ? "kept" : "broken") + " inside a claim");
    }

    /** Still works without protection: the slam crushes stone. */
    @GameTest(template = T)
    public static void hammerStillCrushes(GameTestHelper h) {
        stoneFloor(h);
        TestKit.Mock m = TestKit.player(h, 5, 1, 3, GameType.SURVIVAL, 0);
        ItemStack hammer = new ItemStack(item("itemironhammer"));
        m.player().setItemInHand(InteractionHand.MAIN_HAND, hammer);
        BlockPos floor = h.absolutePos(new BlockPos(5, 0, 5));
        hammer.getItem().useOn(new UseOnContext(m.player(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false)));
        int crushed = crushedCount(h);
        TestKit.remove(h, m);
        TestKit.result(h, "#1 hammer still works", crushed > 0, crushed + " floor blocks crushed");
    }

    // ---------- #2: Diamonds Everywhere ----------

    /** A claim mod cancels the break at LOWEST priority: no diamond may drop (the stone stays, so it can be mined again). */
    @GameTest(template = T)
    public static void diamondsNotOnCancelledBreak(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 2, 3, GameType.SURVIVAL, 0);
        BlockPos stone = h.absolutePos(new BlockPos(5, 1, 5));
        h.getLevel().setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
        m.player().setItemInHand(InteractionHand.MAIN_HAND, enchanted(Items.DIAMOND_PICKAXE, ModEnchantments.DIAMONDS, 100));
        Consumer<BlockEvent.BreakEvent> claim = e -> e.setCanceled(true);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, claim);
        try {
            m.player().gameMode.destroyBlock(stone);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(claim);
        }
        h.runAfterDelay(3, () -> {
            int diamonds = diamonds(h);
            boolean stoneLeft = h.getLevel().getBlockState(stone).is(Blocks.STONE);
            TestKit.remove(h, m);
            TestKit.result(h, "#2 diamonds on cancelled break", diamonds == 0,
                    diamonds + " diamond(s) dropped, stone " + (stoneLeft ? "still there" : "gone"));
        });
    }

    /** Creative breaks drop nothing, so no diamond either. */
    @GameTest(template = T)
    public static void diamondsNotInCreative(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 2, 3, GameType.CREATIVE, 0);
        BlockPos stone = h.absolutePos(new BlockPos(5, 1, 5));
        h.getLevel().setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
        m.player().setItemInHand(InteractionHand.MAIN_HAND, enchanted(Items.DIAMOND_PICKAXE, ModEnchantments.DIAMONDS, 100));
        m.player().gameMode.destroyBlock(stone);
        h.runAfterDelay(3, () -> {
            int diamonds = diamonds(h);
            TestKit.remove(h, m);
            TestKit.result(h, "#2 diamonds in creative", diamonds == 0, diamonds + " diamond(s) dropped in creative");
        });
    }

    /** Still works: a real survival break with level 100 (100%) drops a diamond. */
    @GameTest(template = T)
    public static void diamondsStillDrop(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 2, 3, GameType.SURVIVAL, 0);
        BlockPos stone = h.absolutePos(new BlockPos(5, 1, 5));
        h.getLevel().setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
        m.player().setItemInHand(InteractionHand.MAIN_HAND, enchanted(Items.DIAMOND_PICKAXE, ModEnchantments.DIAMONDS, 100));
        m.player().gameMode.destroyBlock(stone);
        h.runAfterDelay(3, () -> {
            int diamonds = diamonds(h);
            TestKit.remove(h, m);
            TestKit.result(h, "#2 diamonds still work", diamonds == 1, diamonds + " diamond(s) dropped");
        });
    }

    // ---------- #3: water bottle into an empty cauldron ----------

    @GameTest(template = T)
    public static void waterBottleFillsCauldron(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 1, 3, GameType.SURVIVAL, 0);
        BlockPos pos = h.absolutePos(new BlockPos(5, 1, 5));
        h.getLevel().setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        ItemStack bottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        m.player().setItemInHand(InteractionHand.MAIN_HAND, bottle);
        CauldronInteraction.EMPTY.get(Items.POTION).interact(h.getLevel().getBlockState(pos), h.getLevel(), pos, m.player(),
                InteractionHand.MAIN_HAND, bottle);
        BlockState after = h.getLevel().getBlockState(pos);
        TestKit.remove(h, m);
        TestKit.result(h, "#3 water bottle", after.is(Blocks.WATER_CAULDRON), "cauldron is now " + after.getBlock());
    }

    // ---------- #5: Vitality 41+ ----------

    @GameTest(template = T)
    public static void vitalityHighLevelNoCrash(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 1, 5, GameType.SURVIVAL, 0);
        m.player().getFoodData().setFoodLevel(20);
        m.player().setHealth(5);
        String what;
        boolean ok;
        try {
            VitalityEnchantment.healPlayer(m.player(), 41);
            ok = true;
            what = "no error";
        } catch (ArithmeticException e) {
            ok = false;
            what = "ArithmeticException: " + e.getMessage() + " (thrown every tick from LivingTickEvent)";
        }
        TestKit.remove(h, m);
        TestKit.result(h, "#5 vitality 41", ok, what);
    }

    // ---------- #6: Reflection ping-pong ----------

    /** Two players blocking with Reflection X shields: one hit must not bounce back and forth. */
    @GameTest(template = T)
    public static void reflectionNoPingPong(GameTestHelper h) throws Exception {
        TestKit.Mock a = TestKit.player(h, 3, 1, 5, GameType.SURVIVAL, 0);
        TestKit.Mock b = TestKit.player(h, 6, 1, 5, GameType.SURVIVAL, 0);
        hurtable(a, b);
        boolean pvp = h.getLevel().getServer().isPvpAllowed();
        h.getLevel().getServer().setPvpAllowed(true); // the test server has PvP off
        face(a.player(), b.player());
        face(b.player(), a.player());
        block(a.player(), enchanted(ModItems.SMALL_SHIELD.get(), ModEnchantments.REFLECTION, 10));
        block(b.player(), enchanted(ModItems.SMALL_SHIELD.get(), ModEnchantments.REFLECTION, 10));
        boolean blocking = a.player().isBlocking() && b.player().isBlocking();
        AtomicInteger blocks = new AtomicInteger();
        Consumer<ShieldBlockEvent> counter = e -> blocks.incrementAndGet();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, counter);
        String what;
        try {
            b.player().hurt(a.player().damageSources().playerAttack(a.player()), 5);
            what = blocks.get() + " blocks from one hit" + (blocking ? "" : " (mocks NOT blocking: test setup broken)");
        } catch (StackOverflowError e) {
            what = "stack overflow after " + blocks.get() + " blocks";
            blocks.set(Integer.MAX_VALUE);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(counter);
            h.getLevel().getServer().setPvpAllowed(pvp);
        }
        TestKit.remove(h, a, b);
        // the hit (B blocks) and the reflection (A may block it) = at most 2
        TestKit.result(h, "#6 reflection ping-pong", blocking && blocks.get() <= 2, what);
    }

    // ---------- #9: Tunneling and hardness ----------

    /** Mining stone with Tunneling must not also mine the obsidian next to it in one go. */
    @GameTest(template = T)
    public static void tunnelingRespectsHardness(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 4, 5, GameType.SURVIVAL, 0);
        m.player().setXRot(90); // looking down
        BlockPos stone = h.absolutePos(new BlockPos(5, 1, 5));
        BlockPos obsidian = stone.east();
        BlockPos stone2 = stone.west();
        h.getLevel().setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(obsidian, Blocks.OBSIDIAN.defaultBlockState());
        h.getLevel().setBlockAndUpdate(stone2, Blocks.STONE.defaultBlockState());
        m.player().setItemInHand(InteractionHand.MAIN_HAND, enchanted(Items.DIAMOND_PICKAXE, ModEnchantments.TUNNELING, 1));
        m.player().gameMode.destroyBlock(stone);
        h.runAfterDelay(3, () -> {
            boolean obsidianLeft = h.getLevel().getBlockState(obsidian).is(Blocks.OBSIDIAN);
            boolean stone2Mined = h.getLevel().getBlockState(stone2).isAir();
            TestKit.remove(h, m);
            TestKit.result(h, "#9 tunneling hardness", obsidianLeft && stone2Mined,
                    "obsidian " + (obsidianLeft ? "kept" : "mined with the stone") + ", neighbouring stone " + (stone2Mined ? "mined" : "NOT mined"));
        });
    }

    // ---------- #10: coating spent on a hit that doesn't land ----------

    @GameTest(template = T)
    public static void coatingNotSpentOnBlockedHit(GameTestHelper h) throws Exception {
        TestKit.Mock m = TestKit.player(h, 4, 1, 5, GameType.SURVIVAL, 0);
        Zombie target = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
        ItemStack sword = PotionUtils.setPotion(new ItemStack(Items.IRON_SWORD), Potions.SLOWNESS);
        sword.getOrCreateTag().putInt("remainingPotionHits", 5);
        m.player().setItemInHand(InteractionHand.MAIN_HAND, sword);
        // the target is still in its hurt cooldown from a bigger hit, so this one does nothing
        target.invulnerableTime = 20;
        setField(LivingEntity.class, "lastHurt", target, 100.0F);
        m.player().attack(target);
        int left = sword.getTag() != null ? sword.getTag().getInt("remainingPotionHits") : 0;
        boolean poisoned = target.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        TestKit.remove(h, m);
        TestKit.result(h, "#10 coating on a blocked hit", left == 5 && !poisoned,
                "charges left " + left + "/5, target " + (poisoned ? "slowed" : "not slowed") + " by a hit that did no damage");
    }

    /** Still works: a hit that lands uses one charge and applies the potion. */
    @GameTest(template = T)
    public static void coatingStillWorks(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 4, 1, 5, GameType.SURVIVAL, 0);
        Zombie target = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
        ItemStack sword = PotionUtils.setPotion(new ItemStack(Items.IRON_SWORD), Potions.SLOWNESS);
        sword.getOrCreateTag().putInt("remainingPotionHits", 5);
        m.player().setItemInHand(InteractionHand.MAIN_HAND, sword);
        m.player().attack(target);
        int left = sword.getTag() != null ? sword.getTag().getInt("remainingPotionHits") : 0;
        boolean poisoned = target.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        TestKit.remove(h, m);
        TestKit.result(h, "#10 coating still works", left == 4 && poisoned, "charges left " + left + "/5, slowed " + poisoned);
    }

    // ---------- #11: the spin packet ----------

    /** A player without nunchaku must not be able to switch spinning on. */
    @GameTest(template = T)
    public static void spinNeedsNunchaku(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 1, 5, GameType.SURVIVAL, 0);
        m.player().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        TestKit.fromClient(m.player(), new SpinningPacket(true));
        boolean spinning = m.player().getCapability(ModCapabilities.NUNCHAKU_COMBO).map(c -> c.isSpinning()).orElse(false);
        TestKit.remove(h, m);
        TestKit.result(h, "#11 spin without nunchaku", !spinning, spinning ? "spinning switched on holding a sword" : "refused");
    }

    // ---------- #12: lightning dragon bone/dragonsteel lightning without JMixin ----------

    /**
     * Without JMixin, every lightning dragon bone/dragonsteel hit spawned a real lightning bolt: fire, villagers
     * turned into witches, pigs into zombified piglins, charged creepers. It must only look like lightning.
     */
    @GameTest(template = T)
    public static void lightningFallbackIsHarmless(GameTestHelper h) throws Exception {
        TestKit.Mock m = TestKit.player(h, 3, 1, 5, GameType.SURVIVAL, 0);
        net.minecraft.world.entity.npc.Villager villager = h.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(6, 1, 5));
        Method fallback = null;
        for (Method x : com.mx.bettersurvival.integration.IaFCompat.class.getDeclaredMethods()) {
            if (x.getName().equals("triggerChainLightning") || x.getName().equals("triggerFallbackLightning")) fallback = x;
        }
        fallback.setAccessible(true);
        Object[] args = fallback.getParameterCount() == 3
                ? new Object[] {villager, m.player(), 6.0F}
                : new Object[] {villager, m.player()};
        fallback.invoke(null, args);
        AABB box = new AABB(h.absolutePos(BlockPos.ZERO), h.absolutePos(new BlockPos(12, 6, 12)));
        Field visualOnly = net.minecraft.world.entity.LightningBolt.class.getDeclaredField("visualOnly"); // no getter
        visualOnly.setAccessible(true);
        long real = 0;
        for (var bolt : h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class, box)) {
            if (!visualOnly.getBoolean(bolt)) real++;
        }
        TestKit.remove(h, m);
        TestKit.result(h, "#12 lightning fallback", real == 0, real + " real (fire-starting, mob-converting) lightning bolt(s) from one lightning hit");
    }

    // ---------- #16: thrown spear's material bonus ----------

    /**
     * A thrown spear added Ice and Fire's material bonus (silver: +2 against undead) to its base damage before each
     * hit, permanently. A spear that bounces off (target in its hurt cooldown) and hits again had it twice.
     */
    @GameTest(template = T)
    public static void spearBonusDoesNotStack(GameTestHelper h) throws Exception {
        Item silverSpear = item("itemsilverspear");
        if (silverSpear == null || silverSpear == Items.AIR) {
            TestKit.result(h, "#16 spear bonus", true, "Ice and Fire isn't loaded (can't tell)");
            return;
        }
        TestKit.Mock m = TestKit.player(h, 3, 1, 5, GameType.SURVIVAL, 0);
        Zombie target = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
        target.invulnerableTime = 20;
        setField(LivingEntity.class, "lastHurt", target, 1000.0F); // every hit bounces off
        var spear = new com.mx.bettersurvival.entities.FlyingSpearEntity(h.getLevel(), m.player());
        spear.setSpear(new ItemStack(silverSpear));
        spear.setBaseDamage(3.0);
        Method onHit = net.minecraft.world.entity.projectile.AbstractArrow.class.getDeclaredMethod("onHitEntity",
                net.minecraft.world.phys.EntityHitResult.class);
        onHit.setAccessible(true);
        for (int i = 0; i < 3; i++) {
            spear.setDeltaMovement(new Vec3(1, 0, 0));
            onHit.invoke(spear, new net.minecraft.world.phys.EntityHitResult(target));
        }
        double base = spear.getBaseDamage();
        TestKit.remove(h, m);
        TestKit.result(h, "#16 spear bonus", Math.abs(base - 3.0) < 0.001, "base damage after 3 bounced hits: " + base + " (started at 3.0)");
    }

    // ---------- #15: nunchaku knockback cancel ----------

    /** A nunchaku swing that does nothing (target in hurt cooldown) must not cancel other knockback that tick. */
    @GameTest(template = T)
    public static void nunchakuFailedHitKeepsOtherKnockback(GameTestHelper h) throws Exception {
        TestKit.Mock m = TestKit.player(h, 4, 1, 5, GameType.SURVIVAL, 0);
        Zombie target = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
        m.player().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("itemironnunchaku")));
        target.invulnerableTime = 20;
        setField(LivingEntity.class, "lastHurt", target, 100.0F);
        m.player().attack(target);
        target.setDeltaMovement(Vec3.ZERO);
        target.knockback(1.0, -1.0, 0.0); // e.g. a hammer slam or another player's hit in the same tick
        double moved = target.getDeltaMovement().horizontalDistance();
        TestKit.remove(h, m);
        TestKit.result(h, "#15 nunchaku knockback", moved > 0.1, "other knockback " + (moved > 0.1 ? "applied" : "cancelled") + " (" + moved + ")");
    }

    // ---------- #17: Warp and Antiwarp ----------

    @GameTest(template = T)
    public static void warpRespectsAntiwarp(GameTestHelper h) {
        Zombie target = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
        target.addEffect(new MobEffectInstance(ModMobEffects.ANTIWARP.get(), 600));
        Vec3 before = target.position();
        for (int i = 0; i < 5; i++) ModMobEffects.WARP.get().applyInstantenousEffect(null, null, target, 1, 1.0);
        double moved = target.position().distanceTo(before);
        TestKit.result(h, "#17 warp vs antiwarp", moved < 0.01, "an Antiwarp mob moved " + String.format("%.1f", moved) + " blocks");
    }

    // ---------- #19, #23: potion cauldron ----------

    /** The cauldron's potion must be in its update tag, or clients never learn what it holds. */
    @GameTest(template = T)
    public static void potionCauldronSyncsPotion(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(5, 1, 5));
        h.getLevel().setBlockAndUpdate(pos, ModBlocks.POTION_CAULDRON.get().defaultBlockState());
        PotionCauldronBlockEntity be = (PotionCauldronBlockEntity) h.getLevel().getBlockEntity(pos);
        be.setStoredPotion(Potions.SWIFTNESS);
        boolean synced = be.getUpdateTag().contains("Potion");
        TestKit.result(h, "#19 cauldron sync", synced, synced ? "update tag has the potion" : "update tag has no potion: clients show the default");
    }

    /** Filling a glass bottle from a potion cauldron in creative keeps the bottle (like vanilla cauldrons). */
    @GameTest(template = T)
    public static void creativeKeepsBottle(GameTestHelper h) {
        TestKit.Mock m = TestKit.player(h, 5, 1, 3, GameType.CREATIVE, 0);
        BlockPos pos = h.absolutePos(new BlockPos(5, 1, 5));
        BlockState state = ModBlocks.POTION_CAULDRON.get().defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3);
        h.getLevel().setBlockAndUpdate(pos, state);
        ((PotionCauldronBlockEntity) h.getLevel().getBlockEntity(pos)).setStoredPotion(Potions.SWIFTNESS);
        ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, 3);
        m.player().setItemInHand(InteractionHand.MAIN_HAND, bottles);
        com.mx.bettersurvival.blocks.PotionCauldronBlock.INTERACTIONS.get(Items.GLASS_BOTTLE)
                .interact(state, h.getLevel(), pos, m.player(), InteractionHand.MAIN_HAND, bottles);
        int left = m.player().getMainHandItem().is(Items.GLASS_BOTTLE) ? m.player().getMainHandItem().getCount() : 0;
        TestKit.remove(h, m);
        TestKit.result(h, "#23 creative bottle", left == 3, left + "/3 glass bottles left in creative");
    }

    // ---------- helpers ----------

    private static Item item(String path) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("bettersurvival", path));
    }

    /** The stack with the enchantment at any level (bypasses the max level, like /give with NBT does). */
    private static ItemStack enchanted(Item item, RegistryObject<Enchantment> ench, int level) {
        ItemStack stack = new ItemStack(item);
        EnchantmentHelper.setEnchantments(Map.of(ench.get(), level), stack);
        return stack;
    }

    /** Raises the shield in the offhand, already past vanilla's 5-tick warm-up so isBlocking() is true. */
    private static void block(LivingEntity e, ItemStack shield) throws Exception {
        e.setItemInHand(InteractionHand.OFF_HAND, shield);
        e.startUsingItem(InteractionHand.OFF_HAND);
        setField(LivingEntity.class, "useItemRemaining", e, shield.getUseDuration() - 20);
    }

    /** New players can't be hurt for their first 60 ticks; mocks never tick, so clear it. */
    private static void hurtable(TestKit.Mock... mocks) throws Exception {
        for (TestKit.Mock m : mocks) setField(net.minecraft.server.level.ServerPlayer.class, "spawnInvulnerableTime", m.player(), 0);
    }

    private static void face(LivingEntity e, LivingEntity other) {
        e.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, other.getEyePosition());
        e.setYHeadRot(e.getYRot());
        e.setYBodyRot(e.getYRot());
    }

    private static void setField(Class<?> owner, String name, Object target, Object value) throws Exception {
        Field f = owner.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    /** Layer y=0 of the test area as stone (the empty template has no floor). */
    private static void stoneFloor(GameTestHelper h) {
        for (int x = 0; x < 12; x++) for (int z = 0; z < 12; z++) {
            h.getLevel().setBlockAndUpdate(h.absolutePos(new BlockPos(x, 0, z)), Blocks.STONE.defaultBlockState());
        }
    }

    /** Cobblestone + gravel (stone -> cobble -> gravel) on the bottom two layers. */
    private static int crushedCount(GameTestHelper h) {
        return count(h, Blocks.COBBLESTONE, 0) + count(h, Blocks.GRAVEL, 0) + count(h, Blocks.COBBLESTONE, 1) + count(h, Blocks.GRAVEL, 1);
    }

    /** How many blocks of that type are on layer y of the test area. */
    private static int count(GameTestHelper h, net.minecraft.world.level.block.Block block, int y) {
        int n = 0;
        for (int x = 0; x < 12; x++) for (int z = 0; z < 12; z++) {
            if (h.getLevel().getBlockState(h.absolutePos(new BlockPos(x, y, z))).is(block)) n++;
        }
        return n;
    }

    private static int diamonds(GameTestHelper h) {
        AABB box = new AABB(h.absolutePos(BlockPos.ZERO), h.absolutePos(new BlockPos(12, 6, 12)));
        return h.getLevel().getEntitiesOfClass(ItemEntity.class, box, e -> e.getItem().is(Items.DIAMOND)).stream()
                .mapToInt(e -> e.getItem().getCount()).sum();
    }
}
