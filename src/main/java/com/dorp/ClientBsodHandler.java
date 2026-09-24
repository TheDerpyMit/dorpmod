package com.dorp;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@OnlyIn(Dist.CLIENT)
public class ClientBsodHandler {
   private static final long BSOD_DURATION_MS = 3000L;
   private static long startTimeMs = -1L;

   public static void resetState() {
      startTimeMs = -1L;
   }

   public static void register(IEventBus modEventBus) {
      modEventBus.addListener(ClientBsodHandler::onRegisterGuiLayers);
      NeoForge.EVENT_BUS.addListener(ClientBsodHandler::onPlayerTick);
   }

   private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
      event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("dorp", "bsod_overlay"), ClientBsodHandler::renderOverlay);
   }

   public static void trigger() {
      Minecraft mc = Minecraft.getInstance();
      mc.getSoundManager().stop(null, null);
      mc.getSoundManager().play(SimpleSoundInstance.forUI((SoundEvent)DorpMod.SCARE_SOUND.get(), 1.0F, 1.0F));
      startTimeMs = System.currentTimeMillis();
   }

   private static void onPlayerTick(Post event) {
      if (startTimeMs >= 0L) {
         long elapsed = System.currentTimeMillis() - startTimeMs;
         if (elapsed >= 3000L) {
            startTimeMs = -1L;
            executeDisconnectAndError();
         }
      }
   }

   private static void executeDisconnectAndError() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getConnection() != null) {
         mc.getConnection().getConnection().disconnect(Component.literal("A fatal system exception has occurred."));
      }

      DorpMod.showNativeWarning(
         "A fatal system error has occurred. Memory could not be read.\nError code: 0xC0000005 (Access Violation)\nFailed module: Reality.exe",
         "Fatal System Error"
      );
      DorpMod.showNativeWarning("\"WARNED YOU //HUMAN\\\\\"", "System Warning");
   }

   private static void renderOverlay(GuiGraphics gui, DeltaTracker dt) {
      if (startTimeMs >= 0L) {
         long elapsed = System.currentTimeMillis() - startTimeMs;
         if (elapsed < 3000L) {
            Minecraft mc = Minecraft.getInstance();
            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();
            gui.fill(0, 0, sw, sh, -16746281);
            gui.pose().pushPose();
            gui.pose().scale(4.0F, 4.0F, 1.0F);
            gui.drawString(mc.font, ":(", 15, 15, -1, false);
            gui.pose().popPose();
            int startY = 95;
            gui.drawString(mc.font, "Your PC ran into a problem and needs to restart. We're just", 60, startY, -1, false);
            gui.drawString(mc.font, "collecting some error info, and then we'll restart for you.", 60, startY + 12, -1, false);
            int percent = (int)Math.min(100L, elapsed * 100L / 2500L);
            gui.drawString(mc.font, percent + "% complete", 60, startY + 36, -1, false);
            int qrX = 60;
            int qrY = startY + 60;
            gui.fill(qrX, qrY, qrX + 32, qrY + 32, -1);
            gui.fill(qrX + 3, qrY + 3, qrX + 10, qrY + 10, -16746281);
            gui.fill(qrX + 22, qrY + 3, qrX + 29, qrY + 10, -16746281);
            gui.fill(qrX + 3, qrY + 22, qrX + 10, qrY + 29, -16746281);
            gui.fill(qrX + 13, qrY + 11, qrX + 19, qrY + 17, -16746281);
            gui.drawString(mc.font, "For more information about this issue and possible fixes, visit", qrX + 42, qrY, -1, false);
            gui.drawString(mc.font, "https://www.windows.com/stopcode", qrX + 42, qrY + 10, -1, false);
            gui.drawString(mc.font, "If you call a support person, give them this info:", qrX + 42, qrY + 25, -2236963, false);
            gui.drawString(mc.font, "Stop code: CRITICAL_PROCESS_DIED (Reality.exe)", qrX + 42, qrY + 35, -2236963, false);
         }
      }
   }
}
