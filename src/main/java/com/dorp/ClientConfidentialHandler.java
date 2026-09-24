package com.dorp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;

@EventBusSubscriber(modid = "dorp", value = Dist.CLIENT)
public class ClientConfidentialHandler {
   private static boolean hasShownWarningThisSession = false;

   @SubscribeEvent
   public static void onScreenInit(Post event) {
   }

   @SubscribeEvent
   public static void onScreenRender(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event) {
   }

   @SubscribeEvent
   public static void onRenderGui(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
   }

   private static void drawWatermark(GuiGraphics guiGraphics, Minecraft mc, int screenWidth) {
      String line1 = "CONFIDENTIAL BUILD";
      String line2 = "DO NOT DISTRIBUTE";
      String line3 = "dorpmod (unreleased) v1.3";
      int width1 = mc.font.width(line1);
      int width2 = mc.font.width(line2);
      int width3 = mc.font.width(line3);
      guiGraphics.drawString(mc.font, line1, screenWidth - width1 - 5, 5, -65536, true);
      guiGraphics.drawString(mc.font, line2, screenWidth - width2 - 5, 15, -65536, true);
      guiGraphics.drawString(mc.font, line3, screenWidth - width3 - 5, 25, -5592406, true);
   }
}
