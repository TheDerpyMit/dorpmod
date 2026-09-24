package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AnomalousSledgehammerItem extends Item {
   public AnomalousSledgehammerItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (player.getPersistentData().getInt("TimeStopTicks") > 0) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         player.startUsingItem(hand);
         return InteractionResultHolder.consume(itemstack);
      }
   }

   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
      int holdTicks = this.getUseDuration(stack, entity) - count;
      if (holdTicks == 100 && entity instanceof Player player) {
         long gameTime = level.getGameTime();
         long cooldownEnd = player.getPersistentData().getLong("FireChargeCooldown");
         if (gameTime < cooldownEnd) {
            if (!level.isClientSide()) {
               long secondsLeft = (cooldownEnd - gameTime) / 20L;
               player.displayClientMessage(Component.literal("Fire Charge is on cooldown for " + secondsLeft + "s").withStyle(ChatFormatting.RED), true);
            }

            player.stopUsingItem();
            return;
         }

         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0F, 1.0F);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 1.2F);
         player.getCooldowns().addCooldown(this, 300);
         if (!level.isClientSide()) {
            player.getPersistentData().putLong("FireChargeCooldown", gameTime + 18000L);
            InteractionHand hand = player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == this ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            stack.hurtAndBreak(50, player, LivingEntity.getSlotForHand(hand));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1));
            player.getPersistentData().putInt("SledgehammerSlownessDelay", 600);
            player.hurt(level.damageSources().magic(), 4.0F);
            double radius = 6.0;
            AABB area = player.getBoundingBox().inflate(radius);

            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
               target.setRemainingFireTicks(100);
               target.hurt(level.damageSources().onFire(), 4.0F);
            }
         } else {
            AnomalousSledgehammerItem.ClientHelper.playFlame(player);
         }

         player.stopUsingItem();
      }
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      int holdTicks = this.getUseDuration(stack, entity) - timeLeft;
      if (holdTicks < 100 && entity instanceof Player player && !player.getCooldowns().isOnCooldown(this)) {
         player.getPersistentData().putInt("SledgehammerChargeTicks", 30);
         player.getCooldowns().addCooldown(this, 300);
         level.playSound(null, player.blockPosition(), SoundEvents.BAT_LOOP, SoundSource.PLAYERS, 1.0F, 0.5F);
         level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 0.8F);
         InteractionHand hand = player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == this ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
         stack.hurtAndBreak(12, player, LivingEntity.getSlotForHand(hand));
         if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1, player.getZ(), 20, 0.5, 0.1, 0.5, 0.15);
            serverLevel.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
         }

         if (level.isClientSide()) {
            AnomalousSledgehammerItem.ClientHelper.playSonicBoom(player);
         }
      }
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (attacker instanceof Player player) {
         player.getCooldowns().addCooldown(this, 20);
      }

      return true;
   }

   public ItemAttributeModifiers getDefaultAttributeModifiers() {
      return ItemAttributeModifiers.builder()
         .add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "sledgehammer_damage"), 12.0, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "sledgehammer_speed"), -3.2, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .add(
            Attributes.MOVEMENT_SPEED,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "sledgehammer_movement"), -0.03, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .build();
   }

   public int getEnchantmentValue() {
      return 14;
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(
         Component.literal("A heavy hammer vibrating with anomalous energy.").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal("Right Click Tap: Charge forward (Takes 5% durability).").withStyle(ChatFormatting.RED));
      tooltipComponents.add(
         Component.literal("Hold Right Click (5s): Fire Charge - Burns nearby entities, grants Speed III & Extra Hearts (Cooldown: 15m, Takes 20% durability).")
            .withStyle(ChatFormatting.RED)
      );
      tooltipComponents.add(Component.literal("Provides a 50% chance to parry incoming projectiles when held.").withStyle(ChatFormatting.GOLD));
      tooltipComponents.add(Component.literal("Slower movement speed when held in the main hand.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   @OnlyIn(Dist.CLIENT)
   static final class ClientHelper {
      static void playFlame(Player player) {
         Level level = player.level();
         for (int i = 0; i < 24; i++) {
            double dx = (player.getRandom().nextDouble() - 0.5) * 2.0;
            double dz = (player.getRandom().nextDouble() - 0.5) * 2.0;
            level.addParticle(
               ParticleTypes.FLAME, player.getX() + dx, player.getY() + player.getRandom().nextDouble() * 2.0, player.getZ() + dz, 0.0, 0.1, 0.0
            );
         }

         for (int i = 0; i < 12; i++) {
            double dx = (player.getRandom().nextDouble() - 0.5) * 2.5;
            double dz = (player.getRandom().nextDouble() - 0.5) * 2.5;
            level.addParticle(ParticleTypes.LARGE_SMOKE, player.getX() + dx, player.getY() + 0.2, player.getZ() + dz, 0.0, 0.15, 0.0);
         }
      }

      static void playSonicBoom(Player player) {
         Level level = player.level();
         Vec3 look = player.getLookAngle();
         double x = player.getX() + look.x * 2.0;
         double y = player.getEyeY() + look.y * 2.0;
         double z = player.getZ() + look.z * 2.0;
         level.addParticle(ParticleTypes.SONIC_BOOM, x, y, z, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.SWEEP_ATTACK, x, y, z, look.x, 0.0, look.z);
         level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 0.0, 0.2, 0.0);
      }
   }
}
