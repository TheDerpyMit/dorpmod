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

public class RadioBlockItem extends BlockItem {
   public RadioBlockItem(Block block, Properties properties) {
      super(block, properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A placeable radio transmitter.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("Broadcasts chat messages to all players tuned").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("to the same frequency via their Walkie Talkie").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("or Headset.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Transmit range: ").withStyle(ChatFormatting.GRAY).append(Component.literal("7 blocks").withStyle(ChatFormatting.AQUA))
      );
      tooltipComponents.add(
         Component.literal("Receive range: ").withStyle(ChatFormatting.GRAY).append(Component.literal("40 blocks").withStyle(ChatFormatting.AQUA))
      );
      tooltipComponents.add(Component.literal("Output range: ").withStyle(ChatFormatting.GRAY).append(Component.literal("∞").withStyle(ChatFormatting.GOLD)));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Right-Click placed block to set frequency").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Niko").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
