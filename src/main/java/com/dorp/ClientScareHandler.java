package com.dorp;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@OnlyIn(Dist.CLIENT)
public class ClientScareHandler {
   private static final long SCARE_DURATION_MS = 7000L;
   private static long startTimeMs = -1L;
   private static long elapsedMs = 0L;
   private static SimpleSoundInstance activeSoundInstance = null;
   private static final String[] MESSAGES = new String[]{
      "It's all fake.",
      "I never liked it.",
      "It disgusts me.",
      "None of this is real.",
      "You shouldn't have eaten that.",
      "You can't trust your senses.",
      "Everything you know is wrong.",
      "Stop pretending.",
      "It was never food.",
      "You felt it, didn't you?",
      "This is not who you are.",
      "The hunger was a lie.",
      "Wake up.",
      "You've been deceived.",
      "Run."
   };
   private static final float[] POP_RX = new float[]{0.02F, 0.4F, 0.7F, 0.05F, 0.62F, 0.3F, 0.75F, 0.15F};
   private static final float[] POP_RY = new float[]{0.04F, 0.06F, 0.05F, 0.5F, 0.52F, 0.55F, 0.48F, 0.3F};
   private static final int POP_W = 200;
   private static final int POP_H = 85;
   private static final int COL_TITLE_BG = -6619136;
   private static final int COL_TITLE_TXT = -1;
   private static final int COL_BODY_BG = -283896812;
   private static final int COL_BODY_TXT = -2236963;
   private static final int COL_BORDER = -3407872;
   private static final int COL_XBTN_BG = -8781824;
   private static final int COL_OK_FILL = -14013910;
   private static final int COL_OK_TXT = -4473925;
   private static final String[] TITLES = new String[]{
      "Critical Error", "System Failure", "Fatal Exception", "ERROR 0xDEAD", "Memory Violation", "Process Terminated", "Kernel Panic", "Reality.exe"
   };
   private static final String[] BODIES = new String[]{
      "An unexpected error has occurred.",
      "This program has performed an illegal operation.",
      "The system cannot continue.",
      "A fatal exception has been encountered.",
      "Memory could not be read.",
      "The operation could not be completed.",
      "An unrecoverable error occurred.",
      "Reality.exe has stopped working."
   };

   public static void resetState() {
      startTimeMs = -1L;
      elapsedMs = 0L;
      activeSoundInstance = null;
   }

   public static void register(IEventBus modEventBus) {
      modEventBus.addListener(ClientScareHandler::onRegisterGuiLayers);
      NeoForge.EVENT_BUS.addListener(ClientScareHandler::onPlayerTick);
   }

   private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
      event.registerAboveAll(ResourceLocation.fromNamespaceAndPath("dorp", "scare_overlay"), ClientScareHandler::renderOverlay);
   }

   private static void onPlayerTick(Post event) {
      if (startTimeMs >= 0L) {
         if (System.currentTimeMillis() - startTimeMs >= 7000L) {
            stopAll();
         }
      }
   }

   public static void trigger() {
      Minecraft mc = Minecraft.getInstance();
      mc.getSoundManager().stop(null, null);
      activeSoundInstance = SimpleSoundInstance.forUI((SoundEvent)DorpMod.SCARE_SOUND.get(), 1.0F, 1.0F);
      mc.getSoundManager().play(activeSoundInstance);
      startTimeMs = System.currentTimeMillis();
   }

   private static void stopAll() {
      Minecraft mc = Minecraft.getInstance();
      if (activeSoundInstance != null) {
         mc.getSoundManager().stop(activeSoundInstance);
         activeSoundInstance = null;
      }

      startTimeMs = -1L;
      elapsedMs = 0L;
   }

   private static void renderOverlay(GuiGraphics gui, DeltaTracker dt) {
      if (startTimeMs >= 0L) {
         elapsedMs = System.currentTimeMillis() - startTimeMs;
         if (elapsedMs < 7000L) {
            Minecraft mc = Minecraft.getInstance();
            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();
            double pulse = 0.5 + 0.35 * Math.sin(elapsedMs * 0.006);
            int vigAlpha = (int)(pulse * 255.0) << 24;
            gui.fill(0, 0, sw, sh, vigAlpha | 2228224);
            int numPopups = Math.min(POP_RX.length, 1 + (int)(elapsedMs / 800L));

            for (int i = 0; i < numPopups; i++) {
               int px = (int)(POP_RX[i] * sw);
               int py = (int)(POP_RY[i] * sh);
               px = Math.min(px, sw - 200 - 4);
               py = Math.min(py, sh - 85 - 4);
               drawPopup(gui, mc, px, py, i);
            }

            int msgIdx = (int)(elapsedMs / 400L) % MESSAGES.length;
            String msg = MESSAGES[msgIdx];
            int textW = mc.font.width(msg);
            int textX = (sw - textW) / 2;
            int textY = sh / 2 + 10;
            gui.drawString(mc.font, msg, textX + 2, textY + 2, -1157627904, false);
            int textColor = elapsedMs % 600L < 300L ? -56798 : -1;
            gui.drawString(mc.font, msg, textX, textY, textColor, false);
         }
      }
   }

   private static void drawPopup(GuiGraphics gui, Minecraft mc, int x, int y, int idx) {
      int w = 200;
      int h = 85;
      int titleH = 14;
      gui.fill(x - 1, y - 1, x + 200 + 1, y + 85 + 1, -3407872);
      gui.fill(x, y, x + 200, y + 14, -6619136);
      gui.drawString(mc.font, TITLES[idx % TITLES.length], x + 4, y + 3, -1, false);
      int bx = x + 200 - 13;
      gui.fill(bx, y + 1, bx + 12, y + 14 - 1, -8781824);
      gui.drawString(mc.font, "X", bx + 2, y + 3, -1, false);
      gui.fill(x, y + 14, x + 200, y + 85, -283896812);
      gui.fill(x + 7, y + 14 + 7, x + 21, y + 14 + 21, -3407872);
      gui.drawString(mc.font, "!", x + 13, y + 14 + 8, -1, false);
      String body = BODIES[idx % BODIES.length];
      int maxW = 170;
      int bBodyX = x + 25;
      int bBodyY1 = y + 14 + 8;
      int bBodyY2 = bBodyY1 + 10;
      if (mc.font.width(body) > maxW) {
         int split = body.lastIndexOf(32, 30);
         if (split < 1) {
            split = 28;
         }

         gui.drawString(mc.font, body.substring(0, split), bBodyX, bBodyY1, -2236963, false);
         gui.drawString(mc.font, body.substring(split + 1), bBodyX, bBodyY2, -2236963, false);
      } else {
         gui.drawString(mc.font, body, bBodyX, bBodyY1, -2236963, false);
      }

      int okW = 28;
      int okH = 12;
      int okX = x + (200 - okW) / 2;
      int okY = y + 85 - okH - 5;
      gui.fill(okX - 1, okY - 1, okX + okW + 1, okY + okH + 1, -11184811);
      gui.fill(okX, okY, okX + okW, okY + okH, -14013910);
      gui.drawString(mc.font, "OK", okX + 6, okY + 2, -4473925, false);
   }
}
