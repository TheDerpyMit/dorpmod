package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;

public class BananaItem extends Item {
   public BananaItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A fake banana found in the canopy.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("Has a 20% chance to drop from Jungle").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("and Oak leaves when broken.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Restores 4 ♥♥ hunger").withStyle(ChatFormatting.YELLOW));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
