package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.block.Block;

public class BananaBurgerItem extends BlockItem {
   public BananaBurgerItem(Block block, Properties properties) {
      super(block, properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A mysterious burger made from fake bananas.").withStyle(ChatFormatting.GOLD));
      tooltipComponents.add(Component.literal("Highly filling, always edible even when full.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Restores 8 ♥♥♥♥ hunger").withStyle(ChatFormatting.YELLOW));
      tooltipComponents.add(Component.literal("Don't eat it too often...").withStyle(ChatFormatting.RED));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Can be placed as a decorative block").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
