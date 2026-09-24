package com.dorp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

@OnlyIn(Dist.CLIENT)
public class RadioBlockEntityRenderer implements BlockEntityRenderer<RadioBlockEntity> {
   private static final float SCR_CX = 0.40625F;
   private static final float SCR_CY = 0.3046875F;
   private static final float SCR_Z = 0.24643749F;
   private static final float TEXT_SCALE = 0.0042F;
   private static final int MAX_LINE_WIDTH = 80;
   private static final int Y_HEADER = -22;
   private static final int Y_FIRST_MSG = -5;
   private static final int LINE_STEP = 9;
   private static final int COLOR_FREQ = 65348;
   private static final int COLOR_MSG = 8978346;
   private static final int COLOR_IDLE = 2254387;
   private static final int COLOR_SEP = 1727534;

   public RadioBlockEntityRenderer(Context ctx) {
   }

   public static void register(IEventBus modEventBus) {
      modEventBus.addListener(RadioBlockEntityRenderer::onRegisterRenderers);
   }

   private static void onRegisterRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)DorpMod.RADIO_BLOCK_ENTITY.get(), RadioBlockEntityRenderer::new);
   }

   public void render(RadioBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      boolean active = be.isActive();
      long lastDeactive = be.getLastDeactivatedTime();
      long deactiveElapsed = lastDeactive > 0L ? System.currentTimeMillis() - lastDeactive : -1L;
      long shutdownDuration = 800L;
      if (active || deactiveElapsed >= 0L && deactiveElapsed < 800L) {
         BlockState state = be.getBlockState();
         Direction facing = (Direction)state.getValue(RadioBlock.FACING);
         poseStack.pushPose();

         int blockstateY = switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
         };
         if (blockstateY != 0) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(-blockstateY));
            poseStack.translate(-0.5, -0.5, -0.5);
         }

         poseStack.translate(0.40625F, 0.3046875F, 0.24643749F);
         poseStack.scale(-1.0F, -1.0F, 1.0F);
         poseStack.scale(0.0042F, 0.0042F, 0.0042F);
         Font font = Minecraft.getInstance().font;
         int light = 15728880;
         long lastActive = be.getLastActivatedTime();
         long elapsed = lastActive > 0L ? System.currentTimeMillis() - lastActive : -1L;
         long bootDuration = 2000L;
         if (!active && deactiveElapsed >= 0L && deactiveElapsed < 800L) {
            String status;
            String bar;
            if (deactiveElapsed < 400L) {
               status = "SHUTTING DOWN...";
               bar = "[████░] 80%";
            } else {
               status = "HALTED";
               bar = "[░░░░░] 0%";
            }

            drawCentered(font, poseStack, bufferSource, status, -12, 65348, light);
            drawCentered(font, poseStack, bufferSource, bar, -2, 8978346, light);
            drawCentered(font, poseStack, bufferSource, "dorpOS v1.2", 10, 1727534, light);
         } else if (elapsed >= 0L && elapsed < 2000L) {
            String status;
            String bar;
            if (elapsed < 500L) {
               status = "BOOTING SYSTEM...";
               bar = "[█░░░░] 20%";
            } else if (elapsed < 1000L) {
               status = "SEARCHING LINK...";
               bar = "[██░░░] 40%";
            } else if (elapsed < 1500L) {
               status = "CONNECTING FREQ...";
               bar = "[████░] 80%";
            } else {
               status = "LINK ESTABLISHED";
               bar = "[█████] 100%";
            }

            drawCentered(font, poseStack, bufferSource, status, -12, 65348, light);
            drawCentered(font, poseStack, bufferSource, bar, -2, 8978346, light);
            drawCentered(font, poseStack, bufferSource, "dorpOS v1.2", 10, 1727534, light);
         } else {
            int freq = be.getFrequency();
            List<String> messages = RadioClientMessageLog.getMessages(freq);
            boolean blink = System.currentTimeMillis() / 600L % 2L == 0L;
            String header = "FREQ: " + freq + (blink ? " ●" : "   ");
            drawCentered(font, poseStack, bufferSource, header, -22, 65348, light);
            drawCentered(font, poseStack, bufferSource, "─────────", -14, 1727534, light);
            if (messages.isEmpty()) {
               drawCentered(font, poseStack, bufferSource, "-- IDLE --", 4, 2254387, light);
            } else {
               int y = -5;

               for (String msg : messages) {
                  drawCentered(font, poseStack, bufferSource, truncate(font, msg, 80), y, 8978346, light);
                  y += 9;
               }
            }
         }

         poseStack.popPose();
      }
   }

   private static void drawCentered(Font font, PoseStack poseStack, MultiBufferSource buffer, String text, int y, int color, int light) {
      float x = -(font.width(text) / 2.0F);
      font.drawInBatch(text, x, y, color, false, poseStack.last().pose(), buffer, DisplayMode.SEE_THROUGH, 0, light);
   }

   private static String truncate(Font font, String text, int maxWidth) {
      if (font.width(text) <= maxWidth) {
         return text;
      } else {
         String ellipsis = "..";

         while (!text.isEmpty() && font.width(text + "..") > maxWidth) {
            text = text.substring(0, text.length() - 1);
         }

         return text + "..";
      }
   }
}
