package com.dorp;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class CigaretteItem extends Item {
   public CigaretteItem(Properties properties) {
      super(properties);
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 32;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (player.getCooldowns().isOnCooldown(this)) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         CustomData customData = (CustomData)itemstack.get(DataComponents.CUSTOM_DATA);
         if (customData != null && customData.copyTag().getBoolean("used")) {
            return InteractionResultHolder.fail(itemstack);
         } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
         }
      }
   }

   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
      if (level.isClientSide() && entity instanceof Player player && count % 3 == 0) {
         double px = player.getX() + player.getLookAngle().x * 0.4;
         double py = player.getEyeY() - 0.15;
         double pz = player.getZ() + player.getLookAngle().z * 0.4;
         level.addParticle(ParticleTypes.SMOKE, px, py, pz, player.getLookAngle().x * 0.05, 0.05, player.getLookAngle().z * 0.05);
      }
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      int useDuration = this.getUseDuration(stack, entity);
      int inhaleTicks = useDuration - timeLeft;
      if (inhaleTicks >= 5) {
         this.spawnSmokePuff(level, entity, inhaleTicks);
      }
   }

   private void spawnSmokePuff(Level level, LivingEntity entity, int inhaleTicks) {
      if (level.isClientSide() && entity instanceof Player player) {
         int count = Math.min(15, inhaleTicks / 2 + 3);
         double lookX = player.getLookAngle().x;
         double lookY = player.getLookAngle().y;
         double lookZ = player.getLookAngle().z;
         double px = player.getX() + lookX * 0.5;
         double py = player.getEyeY() - 0.15 + lookY * 0.1;
         double pz = player.getZ() + lookZ * 0.5;
         RandomSource random = player.getRandom();

         for (int i = 0; i < count; i++) {
            double vx = lookX * 0.15 + (random.nextDouble() - 0.5) * 0.08;
            double vy = lookY * 0.15 + (random.nextDouble() - 0.5) * 0.08 + 0.02;
            double vz = lookZ * 0.15 + (random.nextDouble() - 0.5) * 0.08;
            ParticleType<?> type = random.nextFloat() < 0.3F ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE;
            level.addParticle((SimpleParticleType)type, px, py, pz, vx, vy, vz);
         }
      }
   }

   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      if (level.isClientSide()) {
         this.spawnSmokePuff(level, entity, 32);
      }

      if (entity instanceof ServerPlayer player) {
         player.getCooldowns().addCooldown(this, 400);
         long currentTime = level.getGameTime();
         long currentDay = level.getDayTime() / 24000L;
         CompoundTag persist = player.getPersistentData();
         long[] smokeTimes = persist.getLongArray("CigaretteSmokeTimes");
         List<Long> recentSmokes = new ArrayList<>();

         for (long t : smokeTimes) {
            if (currentTime - t < 48000L) {
               recentSmokes.add(t);
            }
         }

         recentSmokes.add(currentTime);
         long[] newSmokeTimes = new long[recentSmokes.size()];

         for (int i = 0; i < recentSmokes.size(); i++) {
            newSmokeTimes[i] = recentSmokes.get(i);
         }

         persist.putLongArray("CigaretteSmokeTimes", newSmokeTimes);
         if (newSmokeTimes.length >= 4) {
            player.addEffect(new MobEffectInstance(DorpMod.OVER_SMOKING, 200, 0));
            player.sendSystemMessage(Component.literal("Your chest feels incredibly tight from smoking too much!").withStyle(ChatFormatting.DARK_RED));
         }

         long lastSmokeDay = persist.getLong("CigaretteLastSmokeDay");
         int smokedToday = 1;
         if (lastSmokeDay == currentDay) {
            smokedToday = persist.getInt("CigarettesSmokedToday") + 1;
         }

         persist.putInt("CigarettesSmokedToday", smokedToday);
         persist.putLong("CigaretteLastSmokeDay", currentDay);
         if (persist.getInt("CigaretteWithdrawalTicks") > 0) {
            persist.putInt("CigaretteWithdrawalTicks", 0);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            player.sendSystemMessage(Component.literal("You satisfy your craving. The withdrawal fades.").withStyle(ChatFormatting.GREEN));
         }

         persist.putInt("TicksSinceLastSmoke", 0);
         if (smokedToday >= 2) {
            long[] heavyDays = persist.getLongArray("CigaretteHeavyDays");
            boolean alreadyHeavy = false;

            for (long d : heavyDays) {
               if (d == currentDay) {
                  alreadyHeavy = true;
                  break;
               }
            }

            if (!alreadyHeavy) {
               long[] newHeavyDays = new long[heavyDays.length + 1];
               System.arraycopy(heavyDays, 0, newHeavyDays, 0, heavyDays.length);
               newHeavyDays[heavyDays.length] = currentDay;
               persist.putLongArray("CigaretteHeavyDays", newHeavyDays);
               heavyDays = newHeavyDays;
            }

            boolean day0 = this.hasDay(heavyDays, currentDay);
            boolean day1 = this.hasDay(heavyDays, currentDay - 1L);
            boolean day2 = this.hasDay(heavyDays, currentDay - 2L);
            boolean day3 = this.hasDay(heavyDays, currentDay - 3L);
            if (day0 && day1 && day2 && day3 && !player.hasEffect(DorpMod.SMOKING_ADDICTION)) {
               player.addEffect(new MobEffectInstance(DorpMod.SMOKING_ADDICTION, -1, 0, false, false, true));
               player.sendSystemMessage(
                  Component.literal("You have developed a Smoking Addiction!").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD})
               );
            }
         }

         if (player.level().getRandom().nextInt(50) == 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)DorpMod.COUGH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            player.addEffect(new MobEffectInstance(DorpMod.LUNG_COLLAPSE, 2400, 0));
            persist.putInt("CigaretteProneTicks", 200);
            player.sendSystemMessage(
               Component.literal("Your lung collapsed! You fall to the ground coughing violently!")
                  .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})
            );
         } else {
            player.addEffect(new MobEffectInstance(DorpMod.NICOTINE_RUSH, 400, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0));
         }

         if (!player.getAbilities().instabuild) {
            if (stack.getCount() == 1) {
               CompoundTag tag = new CompoundTag();
               CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
               if (customData != null) {
                  tag = customData.copyTag();
               }

               tag.putBoolean("used", true);
               stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            } else {
               stack.shrink(1);
               ItemStack usedStack = new ItemStack(this);
               CompoundTag tag = new CompoundTag();
               tag.putBoolean("used", true);
               usedStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
               if (!player.getInventory().add(usedStack)) {
                  player.drop(usedStack, false);
               }
            }
         }
      }

      return stack;
   }

   private boolean hasDay(long[] array, long target) {
      for (long val : array) {
         if (val == target) {
            return true;
         }
      }

      return false;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null && customData.copyTag().getBoolean("used")) {
         tooltipComponents.add(Component.literal("Used").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}));
         tooltipComponents.add(Component.literal("Drop on the ground to discard.").withStyle(ChatFormatting.GRAY));
      } else {
         tooltipComponents.add(Component.literal("Right-click to smoke.").withStyle(ChatFormatting.GRAY));
      }

      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Niko").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
   }
}
