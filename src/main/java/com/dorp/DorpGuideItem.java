package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

public class DorpGuideItem extends Item {
   public DorpGuideItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
      if (level.isClientSide()) {
         DorpModClient.openGuideBookScreen();
      }

      return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide());
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("Official guide for crafting recipes of ALL dorpmod items.").withStyle(ChatFormatting.GRAY));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
