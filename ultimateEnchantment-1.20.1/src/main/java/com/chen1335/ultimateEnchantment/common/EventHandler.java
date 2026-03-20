package com.chen1335.ultimateEnchantment.common;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.EnchantmentEffectsHook;
import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import com.chen1335.ultimateEnchantment.enchantment.IAttributeEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.enchantments.LastStand;
import com.chen1335.ultimateEnchantment.enchantment.enchantments.Legend;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IAbstractArrowExtension;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IAttributeExtension;
import com.chen1335.ultimateEnchantment.utils.SimpleSchedule;
import dev.shadowsoffire.placebo.events.GetEnchantmentLevelEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

public class EventHandler {
    @Mod.EventBusSubscriber(modid = UltimateEnchantment.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class GameHandler {

        @SubscribeEvent
        public static void ultimate(GetEnchantmentLevelEvent event) {
            int ultimateLevel = 0;
            ListTag tag = event.getStack().getEnchantmentTags();
            for (int i = 0; i < tag.size(); ++i) {
                CompoundTag compoundtag = tag.getCompound(i);
                if (Objects.equals(EnchantmentHelper.getEnchantmentId(compoundtag), Enchantments.ULTIMATE.getId())) {
                    ultimateLevel = EnchantmentHelper.getEnchantmentLevel(compoundtag);
                }
            }

            int finalUltimateLevel = ultimateLevel;
            event.getEnchantments().forEach((enchantment, integer) -> {
                if (enchantment.getMaxLevel() > 1 && enchantment != Enchantments.ULTIMATE.get() && integer > 0) {
                    int finalLevel = integer + finalUltimateLevel;
                    if (enchantment == net.minecraft.world.item.enchantment.Enchantments.QUICK_CHARGE) {
                        finalLevel = Math.min(finalLevel, 5);
                    }
                    event.getEnchantments().put(enchantment, finalLevel);
                }
            });
        }

        @SubscribeEvent
        public static void legend(LivingEquipmentChangeEvent event) {
            ItemStack from = event.getFrom();
            ItemStack to = event.getTo();
            LivingEntity livingEntity = event.getEntity();

            Equipable equipable = Equipable.get(to);

            EquipmentSlot slot = event.getSlot();

            boolean slotCorrect = false;

            if (equipable == null && (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND)) {
                slotCorrect = true;
            } else if (equipable != null && equipable.getEquipmentSlot() == slot) {
                slotCorrect = true;
            }

            if (!from.isEmpty()) {
                livingEntity.getAttributes().supplier.instances.forEach((attribute, attributeInstance) -> {
                    if (!Legend.BLACK_LIST.contains(attribute)) {
                        livingEntity.getAttributes().getInstance(attribute).getModifiers().forEach(attributeModifier -> {
                            if (attributeModifier.getName().equals("ue:legendModifier_" + slot.getName())) {
                                livingEntity.getAttributes().getInstance(attribute).removeModifier(attributeModifier.getId());
                            }
                        });
                    }
                });
            }

            if (!to.isEmpty() && slotCorrect) {
                int legendLevel = to.getEnchantmentLevel(Enchantments.LEGEND.get());
                livingEntity.getAttributes().supplier.instances.forEach((attribute, attributeInstance) -> {
                    IAttributeExtension attributeExtension = (IAttributeExtension) attribute;
                    if (attributeExtension.ue$getSentiment() == AttributeTypeInfo.Sentiment.POSITIVE) {
                        livingEntity.getAttributes().getInstance(attribute).addTransientModifier(new AttributeModifier("ue:legendModifier_" + slot.getName(), Enchantments.LEGEND.get().getAttributeBonus(legendLevel), AttributeModifier.Operation.MULTIPLY_BASE));
                    } else if (attributeExtension.ue$getSentiment() == AttributeTypeInfo.Sentiment.NEGATIVE) {
                        livingEntity.getAttributes().getInstance(attribute).addTransientModifier(new AttributeModifier("ue:legendModifier_" + slot.getName(), -Enchantments.LEGEND.get().getAttributeBonus(legendLevel), AttributeModifier.Operation.MULTIPLY_TOTAL));
                    }
                });
            }

            event.getEntity().setHealth(event.getEntity().getHealth());
        }

        @SubscribeEvent
        public static void lastStand(LivingEquipmentChangeEvent event) {
            EnchantmentEffectsHook.updateLastStandState(event.getEntity());

        }

        @SubscribeEvent
        public static void lastStand(ItemAttributeModifierEvent event) {
            ItemStack itemStack = event.getItemStack();
            int lastStandLevel = itemStack.getEnchantmentLevel(Enchantments.LAST_STAND.get());
            Equipable equipable = Equipable.get(itemStack);

            if (itemStack.getTag() != null && itemStack.getItem() instanceof ArmorItem armorItem) {
                float userHealth = itemStack.getOrCreateTag().getFloat("ue:userHealth");
                float userMaxHealth = itemStack.getOrCreateTag().getFloat("ue:userMaxHealth");
                if (userHealth <= userMaxHealth * Enchantments.LAST_STAND.get().getEffectiveMaximumHealthPercentage() && equipable != null && equipable.getEquipmentSlot() == event.getSlotType() && lastStandLevel > 0) {
                    event.addModifier(Attributes.ARMOR, new AttributeModifier(LastStand.ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "ue:lastStand_" + event.getSlotType().name(), Enchantments.LAST_STAND.get().getArmorBonus(lastStandLevel), AttributeModifier.Operation.MULTIPLY_TOTAL));
                    event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(LastStand.ARMOR_TOUGHNESS_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "ue:lastStand_" + event.getSlotType().name(), Enchantments.LAST_STAND.get().getArmorToughnessBonus(lastStandLevel), AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        }

        @SubscribeEvent
        public static void attributeEnchantment(ItemAttributeModifierEvent event) {
            ItemStack itemStack = event.getItemStack();
            EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(itemStack);
            event.getItemStack().getAllEnchantments().forEach((enchantment, integer) -> {
                if (enchantment instanceof IAttributeEnchantment attributeEnchantment && slot == event.getSlotType()) {
                    attributeEnchantment.getAttributeModifier(event.getSlotType(), integer).forEach(event::addModifier);
                }
            });
        }


        @SubscribeEvent
        public static void quickLatch(LivingEntityUseItemEvent.Tick event) {
            if (!event.getEntity().level().isClientSide && event.getItem().getItem() instanceof BowItem bowItem && event.getItem().getEnchantmentLevel(Enchantments.QUICK_LATCH.get()) > 0) {
                if (bowItem.getUseDuration(event.getItem()) - event.getDuration() >= BowItem.MAX_DRAW_DURATION) {
                    event.getEntity().releaseUsingItem();
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void CutDown(LivingHurtEvent event) {
            if (event.getSource().getDirectEntity() instanceof LivingEntity livingEntity) {
                int cutDownLevel = livingEntity.getMainHandItem().getEnchantmentLevel(Enchantments.CUT_DOWN.get());
                float exceedHealthPercentage = ((event.getEntity().getHealth() - livingEntity.getMaxHealth()) / livingEntity.getMaxHealth()) * 100;
                event.setAmount(event.getAmount() * (1 + Enchantments.CUT_DOWN.get().getDamageBonus(Math.max(0, exceedHealthPercentage), cutDownLevel)));
            }
        }

        @SubscribeEvent
        public static void Terminator(LivingEntityUseItemEvent.Stop event) {
            if (event.getEntity() instanceof Player player && event.getItem().getEnchantmentLevel(Enchantments.TERMINATOR.get()) > 0) {
                if (event.getItem().getItem() instanceof BowItem || event.getItem().getTags().anyMatch(itemTagKey -> itemTagKey == Tags.Items.TOOLS_BOWS)) {
                    Enchantments.TERMINATOR.get().shoot(event.getItem(), event.getEntity().level(), player, event.getDuration());
                }
            }
        }

        @SubscribeEvent
        public static void LivingHurtByTerminatorArrow(LivingHurtEvent event) {
            if (event.getSource().getDirectEntity() instanceof AbstractArrow abstractArrow && ((IAbstractArrowExtension) abstractArrow).ue$isByPassInvulnerableTime()) {
                event.getEntity().invulnerableTime = 0;
            }
        }

        @SubscribeEvent
        public static void ServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                SimpleSchedule.update(Dist.DEDICATED_SERVER);
            }
        }

        @SubscribeEvent
        public static void ClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                SimpleSchedule.update(Dist.CLIENT);
            }
        }

        @SubscribeEvent
        public static void eternal(ItemExpireEvent event) {
            ItemStack itemStack = event.getEntity().getItem();
            if (itemStack.getEnchantmentLevel(Enchantments.ETERNAL.get()) > 0) {
                event.setCanceled(true);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = UltimateEnchantment.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModHandler {

    }
}
