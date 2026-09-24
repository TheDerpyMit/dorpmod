package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;

public class TrackerViewerItem extends Item {
   public TrackerViewerItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (data != null) {
         CompoundTag tag = data.copyTag();
         if (tag.getBoolean("bricked")) {
            tooltipComponents.add(Component.literal("Bricked").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}));
            tooltipComponents.add(Component.literal("The signal has been lost...").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            return;
         }

         if (tag.contains("TrackedPlayerName")) {
            String name = tag.getString("TrackedPlayerName");
            tooltipComponents.add(
               Component.literal("Tracking: ").withStyle(ChatFormatting.GRAY).append(Component.literal(name).withStyle(ChatFormatting.GREEN))
            );
            if (tag.contains("TrackerID")) {
               int id = tag.getInt("TrackerID");
               tooltipComponents.add(Component.literal("Linked ID: " + id).withStyle(ChatFormatting.YELLOW));
            }

            if (tag.contains("LastX")) {
               int x = tag.getInt("LastX");
               int y = tag.getInt("LastY");
               int z = tag.getInt("LastZ");
               String arrow = tag.getString("Arrow");
               tooltipComponents.add(
                  Component.literal("Location: ")
                     .withStyle(ChatFormatting.GRAY)
                     .append(Component.literal("X: " + x + ", Y: " + y + ", Z: " + z).withStyle(ChatFormatting.YELLOW))
               );
               if (!arrow.isEmpty()) {
                  tooltipComponents.add(
                     Component.literal("Direction: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(arrow).withStyle(new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.BOLD}))
                  );
               }
            } else {
               tooltipComponents.add(Component.literal("Awaiting signal...").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
            }
         } else {
            tooltipComponents.add(Component.literal("Unlinked").withStyle(ChatFormatting.DARK_GRAY));
         }
      }

      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public Component getName(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return (Component)(data != null && data.copyTag().getBoolean("bricked")
         ? Component.literal("Bricked Tracker Viewer").withStyle(ChatFormatting.RED)
         : super.getName(stack));
   }
}
