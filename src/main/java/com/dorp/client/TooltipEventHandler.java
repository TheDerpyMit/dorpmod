package com.dorp.client;

import com.dorp.DorpMod;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = "dorp", value = Dist.CLIENT)
public class TooltipEventHandler {
   @SubscribeEvent
   public static void onTooltip(ItemTooltipEvent event) {
      if (!event.getToolTip().isEmpty()) {
         Item item = event.getItemStack().getItem();
         if (isHat(item)) {
            boolean isWDown = false;

            try {
               long window = Minecraft.getInstance().getWindow().getWindow();
               isWDown = InputConstants.isKeyDown(window, 87);
            } catch (Exception var8) {
            }

            int insertIndex = Math.min(1, event.getToolTip().size());
            if (isWDown) {
               List<String> loreLines = new ArrayList<>();
               getLore(item, loreLines);
               int idx = insertIndex;

               for (String line : loreLines) {
                  event.getToolTip().add(idx++, Component.literal(line).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
               }

               event.getToolTip().add(idx, Component.literal(""));
            } else {
               event.getToolTip()
                  .add(insertIndex, Component.literal("Hold [W] for History").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
               event.getToolTip().add(insertIndex + 1, Component.literal(""));
            }
         }
      }
   }

   private static void getLore(Item item, List<String> lines) {
      if (item == DorpMod.BLUE_CAP.get()) {
         lines.add("A traditional French infantry cap worn by soldiers during the First World War.");
         lines.add("Symbolic of trench warfare resilience and patriotism.");
      } else if (item == DorpMod.POOP_CAP.get()) {
         lines.add("A standard issue peak cap worn by officers and infantry of the Russian Imperial Army.");
         lines.add("An iconic silhouette of early 20th century military hierarchy.");
      } else if (item == DorpMod.NAZI_CAP.get()) {
         lines.add("A command peak cap worn by officers of the German Reich military forces.");
         lines.add("Signifies authority and strict tactical doctrine during the Second World War.");
      } else if (item == DorpMod.CAPTAIN_CAP.get()) {
         lines.add("A white peak cap symbolizing naval authority and command over the high seas.");
         lines.add("Issued to captains overseeing fleet operations.");
      } else if (item == DorpMod.SUOMI_FIELD_CAP.get()) {
         lines.add("A Finnish field cap worn by defenders during the Winter and Continuation Wars.");
         lines.add("Engineered to withstand harsh northern environments and sub-zero temperatures.");
      } else if (item == DorpMod.COMMONWEALTH_FIELD_CAP.get()) {
         lines.add("A standard service cap worn by British and Commonwealth forces across various global theaters.");
         lines.add("A symbol of the Allied presence throughout the First and Second World Wars.");
      } else if (item == DorpMod.REICH_FIELD_CAP.get()) {
         lines.add("A lightweight field cap issued to German infantry during tactical operations.");
         lines.add("Designed for mobility and utility in the active combat theater.");
      } else if (item == DorpMod.LIBERTE_CAP.get()) {
         lines.add("A service cap symbolizing liberty, worn by French soldiers in combat zones.");
         lines.add("Adopted during historical campaigns to boost troop morale and solidarity.");
      } else if (item == DorpMod.SUNLIGHT_HELMET.get()) {
         lines.add("This helm with a red feather is said to have belonged to a Knight of Sunlight in a previous age.");
      } else if (item == DorpMod.WEHRMACHT_OFFICER_CAP.get()) {
         lines.add("A command peak cap worn by officers of the German Wehrmacht.");
         lines.add("Symbolic of leadership and tactical oversight during WWII.");
      }
   }

   private static boolean isHat(Item item) {
      return item == DorpMod.BLUE_CAP.get()
         || item == DorpMod.POOP_CAP.get()
         || item == DorpMod.NAZI_CAP.get()
         || item == DorpMod.CAPTAIN_CAP.get()
         || item == DorpMod.SUOMI_FIELD_CAP.get()
         || item == DorpMod.COMMONWEALTH_FIELD_CAP.get()
         || item == DorpMod.REICH_FIELD_CAP.get()
         || item == DorpMod.LIBERTE_CAP.get()
         || item == DorpMod.SUNLIGHT_HELMET.get()
         || item == DorpMod.WEHRMACHT_OFFICER_CAP.get();
   }
}
