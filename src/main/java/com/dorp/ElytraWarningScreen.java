package com.dorp;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class ElytraWarningScreen extends Screen {
   private static final int BG_WIDTH = 260;
   private static final int BG_HEIGHT = 120;

   public ElytraWarningScreen() {
      super(Component.empty());
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void init() {
      super.init();
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int btnY = centerY + 28;
      this.addRenderableWidget(Button.builder(Component.literal("sorry ill dispose of it").withStyle(ChatFormatting.GREEN), btn -> {
         PacketDistributor.sendToServer(new ElytraDisposePayload(), new CustomPacketPayload[0]);
         this.onClose();
      }).bounds(centerX - 135, btnY, 130, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("idc pal").withStyle(ChatFormatting.RED), btn -> {
         PacketDistributor.sendToServer(new ElytraAcceptPayload(), new CustomPacketPayload[0]);
         this.onClose();
      }).bounds(centerX + 5, btnY, 130, 20).build());
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
      int cx = this.width / 2;
      int cy = this.height / 2;
      int x = cx - 130;
      int y = cy - 60;
      guiGraphics.fill(x, y, x + 260, y + 120, -587202560);
      guiGraphics.fill(x, y, x + 260, y + 1, -22016);
      guiGraphics.fill(x, y + 120 - 1, x + 260, y + 120, -22016);
      guiGraphics.fill(x, y, x + 1, y + 120, -22016);
      guiGraphics.fill(x + 260 - 1, y, x + 260, y + 120, -22016);
      guiGraphics.drawCenteredString(
         this.font, Component.literal("⚠ Elytra Warning ⚠").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}), cx, y + 10, -22016
      );
      guiGraphics.drawCenteredString(this.font, Component.literal("hey buddy i dont think elytra's are").withStyle(ChatFormatting.YELLOW), cx, y + 30, -1);
      guiGraphics.drawCenteredString(this.font, Component.literal("allowed in the smp?!").withStyle(ChatFormatting.YELLOW), cx, y + 42, -1);
      guiGraphics.drawCenteredString(
         this.font,
         Component.literal("what do you want to do?").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
         cx,
         y + 58,
         -5592406
      );

      for (Renderable renderable : this.renderables) {
         renderable.render(guiGraphics, mouseX, mouseY, partialTick);
      }

      guiGraphics.pose().popPose();
   }
}
