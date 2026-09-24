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

public class InterceptorBlockItem extends BlockItem {
   public InterceptorBlockItem(Block block, Properties properties) {
      super(block, properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A placeable signal interceptor.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("Intercepts and decodes nearby wireless chat messages").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("from walkie-talkies and headsets.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Intercept range: ").withStyle(ChatFormatting.GRAY).append(Component.literal("1200 blocks").withStyle(ChatFormatting.AQUA))
      );
      tooltipComponents.add(
         Component.literal("Broadcast range: ").withStyle(ChatFormatting.GRAY).append(Component.literal("30 blocks").withStyle(ChatFormatting.AQUA))
      );
      tooltipComponents.add(
         Component.literal("Energy cost: ").withStyle(ChatFormatting.GRAY).append(Component.literal("100 FE / message").withStyle(ChatFormatting.GOLD))
      );
      tooltipComponents.add(Component.literal("Capacity: ").withStyle(ChatFormatting.GRAY).append(Component.literal("5000 FE").withStyle(ChatFormatting.GOLD)));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Right-Click placed block to toggle ON/OFF").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Niko").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
