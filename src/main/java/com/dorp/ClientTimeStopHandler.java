package com.dorp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@EventBusSubscriber(modid = "dorp", value = Dist.CLIENT)
public class ClientTimeStopHandler {
   private static int ticksRemaining = 0;
   private static int maxTicks = 0;
   private static final int TRANSITION_TICKS = 10;
   private static boolean shaderActive = false;

   public static void start(int durationTicks) {
      ticksRemaining = durationTicks;
      maxTicks = durationTicks;
   }

   @SubscribeEvent
   public static void onClientTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (event.getEntity() == mc.player) {
         if (ticksRemaining > 0) {
            ticksRemaining--;
            if (ticksRemaining == maxTicks - 5 && !shaderActive && mc.gameRenderer.currentEffect() == null) {
               mc.gameRenderer.loadEffect(ResourceLocation.withDefaultNamespace("shaders/post/invert.json"));
               shaderActive = true;
            }

            if (ticksRemaining == 5 && shaderActive) {
               mc.gameRenderer.shutdownEffect();
               shaderActive = false;
            } else if (shaderActive && mc.gameRenderer.currentEffect() == null) {
               mc.gameRenderer.loadEffect(ResourceLocation.withDefaultNamespace("shaders/post/invert.json"));
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRenderGui(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
      if (ticksRemaining > 0) {
         float alpha = 0.0F;
         if (ticksRemaining > maxTicks - 10) {
            int elapsed = maxTicks - ticksRemaining;
            alpha = elapsed < 5 ? elapsed / 5.0F : 1.0F - (elapsed - 5) / 5.0F;
         } else if (ticksRemaining <= 10) {
            int elapsed = 10 - ticksRemaining;
            alpha = elapsed < 5 ? elapsed / 5.0F : 1.0F - (elapsed - 5) / 5.0F;
         }

         if (alpha > 0.0F) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int color = (int)(alpha * 255.0F) << 24 | 16777215;
            guiGraphics.fill(0, 0, screenWidth, screenHeight, color);
            RenderSystem.disableBlend();
         }
      }
   }
}
