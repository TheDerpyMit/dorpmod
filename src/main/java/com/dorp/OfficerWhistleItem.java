package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.AABB;

public class OfficerWhistleItem extends Item {
   public OfficerWhistleItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (player.getCooldowns().isOnCooldown(this)) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         player.getCooldowns().addCooldown(this, 900);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)DorpMod.WW1_WHISTLE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         if (!level.isClientSide()) {
            AABB area = new AABB(player.blockPosition()).inflate(16.0);

            for (Player p : level.getEntitiesOfClass(Player.class, area)) {
               p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1));
            }

            itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
         }

         return InteractionResultHolder.success(itemstack);
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(
         Component.literal("A reproduction of the standard issue trench whistles used by officers in the First World War.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("Used to signal soldiers to charge over the top of the trenches into No Man's Land.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("On Use: Plays a whistle and rallies all players within 16 blocks, granting Speed II for 8 seconds.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
