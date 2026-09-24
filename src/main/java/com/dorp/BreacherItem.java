package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;

public class BreacherItem extends Item {
   public BreacherItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A highly advanced hacking device.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("Right Click a locked chest to attempt to hack it.").withStyle(ChatFormatting.GREEN));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
