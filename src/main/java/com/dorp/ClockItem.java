package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClockItem extends Item {
   public ClockItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(
         Component.literal("A finely crafted timepiece of classic French design.").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("On Use: Slipping into the shadows, grants temporary invisibility and silence at the cost of 50% of your maximum health.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (!player.getCooldowns().isOnCooldown(this)) {
         player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0, false, false, false));
         player.setSilent(true);
         float maxHealth = player.getMaxHealth();
         player.hurt(player.damageSources().magic(), maxHealth / 2.0F);
         player.getPersistentData().putInt("TimeStopTicks", 300);
         player.getCooldowns().addCooldown(this, 4800);
         itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
         if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.blockPosition(), (SoundEvent)DorpMod.TIMESTOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.playSound((SoundEvent)DorpMod.TIMETICK.get(), 1.0F, 1.0F);
            if (player instanceof ServerPlayer serverPlayer) {
               PacketDistributor.sendToPlayer(serverPlayer, new TimeStopPayload(300), new CustomPacketPayload[0]);
               PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                  serverPlayer, new MonsterRoarPayload(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()), new CustomPacketPayload[0]
               );
            }
         }

         return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }
}
