package com.dorp;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import rbasamoyai.ritchiesprojectilelib.RitchiesProjectileLib;
import rbasamoyai.ritchiesprojectilelib.effects.screen_shake.ScreenShakeEffect;

public class BombVestItem extends ArmorItem {
   public BombVestItem(Properties properties) {
      super(ArmorMaterials.LEATHER, Type.CHESTPLATE, properties);
   }

   public static boolean isActive(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return customData != null ? customData.copyTag().getBoolean("active") : false;
   }

   public static void setActive(ItemStack stack, boolean active) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      tag.putBoolean("active", active);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static int getFuse(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         CompoundTag tag = customData.copyTag();
         if (tag.contains("fuse")) {
            return tag.getInt("fuse");
         }
      }

      return 240;
   }

   public static void setFuse(ItemStack stack, int fuse) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      tag.putInt("fuse", fuse);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static String getPrimedBy(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return customData != null ? customData.copyTag().getString("primedBy") : "";
   }

   public static void setPrimedBy(ItemStack stack, String uuidStr) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      tag.putString("primedBy", uuidStr);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      boolean active = isActive(stack);
      int fuse = getFuse(stack);
      if (active) {
         double timeLeft = fuse / 20.0;
         tooltipComponents.add(
            Component.literal("⚠ ARMED ")
               .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})
               .append(
                  Component.literal(String.format("%.1fs remaining!", timeLeft)).withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD})
               )
         );
      } else {
         tooltipComponents.add(
            Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("UNARMED").withStyle(ChatFormatting.GREEN))
         );
      }

      tooltipComponents.add(
         Component.literal("An improvised explosive device integrated into a standard issue military harness.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("Deployed for high-impact sabotage and tactical sacrifice in active warzones.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("A wearable explosive vest.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("Once primed, it detonates after 12 seconds with high explosive power.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Right-Click to equip as chestplate").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("Crouch+Right-Click to ARM the fuse").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         if (!level.isClientSide() && !isActive(stack)) {
            setActive(stack, true);
            setFuse(stack, 240);
            setPrimedBy(stack, player.getUUID().toString());
            level.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)DorpMod.VEST_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
      } else {
         return super.use(level, player, hand);
      }
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      if (!level.isClientSide()) {
         if (entity instanceof Player player) {
            if (isActive(stack)) {
               int fuse = getFuse(stack);
               if (fuse <= 0) {
                  setActive(stack, false);
                  stack.shrink(1);
                  level.explode(player, player.getX(), player.getY(), player.getZ(), 4.0F, ExplosionInteraction.BLOCK);
                  if (level instanceof ServerLevel serverLevel) {
                     ScreenShakeEffect shake = new ScreenShakeEffect(40, 5.0F, 5.0F, player.getX(), player.getY(), player.getZ());

                     for (ServerPlayer sp : serverLevel.players()) {
                        if (sp.distanceToSqr(player.getX(), player.getY(), player.getZ()) < 2500.0) {
                           RitchiesProjectileLib.shakePlayerScreen(sp, shake);
                        }
                     }

                     serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY(), player.getZ(), 200, 3.0, 3.0, 3.0, 0.05);
                     serverLevel.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, player.getX(), player.getY(), player.getZ(), 150, 3.0, 3.0, 3.0, 0.05);
                     serverLevel.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY(), player.getZ(), 150, 3.0, 3.0, 3.0, 0.1);
                  }

                  player.hurt(level.damageSources().explosion(player, player), Float.MAX_VALUE);
               } else {
                  setFuse(stack, fuse - 1);
                  double timeLeft = fuse / 20.0;
                  Component msg = Component.literal("\ud83d\udca3 EXPLOSION IN: ")
                     .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})
                     .append(Component.literal(String.format("%.1fs", timeLeft)).withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}));
                  player.displayClientMessage(msg, true);
               }
            }
         }
      }
   }

   public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
      Level level = entity.level();
      if (level.isClientSide()) {
         return false;
      } else {
         if (isActive(stack)) {
            int fuse = getFuse(stack);
            if (fuse <= 0) {
               setActive(stack, false);
               entity.discard();
               level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 4.0F, ExplosionInteraction.BLOCK);
               if (level instanceof ServerLevel serverLevel) {
                  ScreenShakeEffect shake = new ScreenShakeEffect(40, 5.0F, 5.0F, entity.getX(), entity.getY(), entity.getZ());

                  for (ServerPlayer sp : serverLevel.players()) {
                     if (sp.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 2500.0) {
                        RitchiesProjectileLib.shakePlayerScreen(sp, shake);
                     }
                  }

                  serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 200, 3.0, 3.0, 3.0, 0.05);
                  serverLevel.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 150, 3.0, 3.0, 3.0, 0.05);
                  serverLevel.sendParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY(), entity.getZ(), 150, 3.0, 3.0, 3.0, 0.1);
               }

               String uuidStr = getPrimedBy(stack);
               if (!uuidStr.isEmpty()) {
                  try {
                     UUID uuid = UUID.fromString(uuidStr);
                     Player primedPlayer = level.getPlayerByUUID(uuid);
                     if (primedPlayer != null) {
                        level.explode(primedPlayer, primedPlayer.getX(), primedPlayer.getY(), primedPlayer.getZ(), 4.0F, ExplosionInteraction.BLOCK);
                        primedPlayer.hurt(level.damageSources().explosion(primedPlayer, primedPlayer), Float.MAX_VALUE);
                     }
                  } catch (Exception var9) {
                  }
               }
            } else {
               setFuse(stack, fuse - 1);
            }
         }

         return false;
      }
   }

   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(BombVestItemExtensions.INSTANCE);
   }
}
