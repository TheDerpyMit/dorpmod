package com.dorp.client;

import com.dorp.BreachChestPayload;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

public class BreachHackScreen extends Screen {
   private final BlockPos chestPos;
   private float targetAmp;
   private float targetFreq;
   private float targetPhase;
   private float playerAmp = 0.5F;
   private float playerFreq = 0.5F;
   private float playerPhase = 0.5F;
   private int ticksOpen = 0;
   private static final int MAX_TICKS = 6000;
   private Button breachButton;
   private float[] columnDrops;
   private final Random random = new Random();
   private int draggingSlider = -1;

   public BreachHackScreen(BlockPos chestPos) {
      super(Component.literal("System Breach Interface"));
      this.chestPos = chestPos;
      this.targetAmp = 0.2F + this.random.nextFloat() * 0.6F;
      this.targetFreq = 0.2F + this.random.nextFloat() * 0.6F;
      this.targetPhase = this.random.nextFloat();
      this.columnDrops = new float[60];

      for (int i = 0; i < this.columnDrops.length; i++) {
         this.columnDrops[i] = this.random.nextFloat() * 400.0F;
      }
   }

   protected void init() {
      super.init();
      int cx = this.width / 2;
      int cy = this.height / 2;
      this.breachButton = Button.builder(Component.literal("EXECUTE BREACH"), b -> {
         PacketDistributor.sendToServer(new BreachChestPayload(this.chestPos), new CustomPacketPayload[0]);
         this.onClose();
      }).bounds(cx + 10, cy + 80, 140, 20).build();
      this.breachButton.active = false;
      this.addRenderableWidget(this.breachButton);
   }

   public void tick() {
      super.tick();
      this.ticksOpen++;

      for (int i = 0; i < this.columnDrops.length; i++) {
         this.columnDrops[i] = this.columnDrops[i] + (2.0F + this.random.nextFloat() * 2.0F);
         if (this.columnDrops[i] > this.height) {
            this.columnDrops[i] = -20.0F;
         }
      }

      this.checkWinCondition();
   }

   private void checkWinCondition() {
      if (this.ticksOpen >= 6000) {
         this.breachButton.active = true;
         this.breachButton.setMessage(Component.literal("AUTO-SOLVE READY"));
      } else {
         float diffA = Math.abs(this.playerAmp - this.targetAmp);
         float diffF = Math.abs(this.playerFreq - this.targetFreq);
         float diffP = Math.abs(this.playerPhase - this.targetPhase);
         if (diffA < 0.05F && diffF < 0.05F && diffP < 0.05F) {
            this.breachButton.active = true;
            this.breachButton.setMessage(Component.literal("EXECUTE BREACH"));
         } else {
            this.breachButton.active = false;
            this.breachButton.setMessage(Component.literal("ALIGNMENT FAILED"));
         }
      }
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(g, mouseX, mouseY, partialTick);
      g.fill(0, 0, this.width, this.height, -16446971);
      g.pose().pushPose();
      int cols = this.width / 10;

      for (int i = 0; i < Math.min(cols, this.columnDrops.length); i++) {
         int x = i * 10;
         float y = this.columnDrops[i];
         g.drawString(this.font, String.valueOf((char)(33 + this.random.nextInt(90))), x, (int)y, 1140915968, false);
         g.drawString(this.font, String.valueOf((char)(33 + this.random.nextInt(90))), x, (int)y - 10, 570490624, false);
      }

      g.pose().popPose();
      int cx = this.width / 2;
      int cy = this.height / 2;
      this.drawHackerGraphics(g, cx - 120, cy, partialTick);
      g.fill(cx - 10, cy - 100, cx + 160, cy + 110, -586542070);
      g.fill(cx - 10, cy - 100, cx + 160, cy - 99, -16711936);
      g.fill(cx - 10, cy + 109, cx + 160, cy + 110, -16711936);
      g.fill(cx - 10, cy - 100, cx - 9, cy + 110, -16711936);
      g.fill(cx + 159, cy - 100, cx + 160, cy + 110, -16711936);
      g.drawCenteredString(this.font, "OVERRIDE PROTOCOL", cx + 75, cy - 90, -16711936);
      int timeLeft = (6000 - this.ticksOpen) / 20;
      String timeStr = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
      g.drawCenteredString(this.font, "AUTO-SOLVE: " + timeStr, cx + 75, cy - 75, -5592406);
      int boxX = cx;
      int boxY = cy - 50;
      int boxW = 150;
      int boxH = 60;
      g.fill(cx, boxY, cx + boxW, boxY + boxH, -16777216);

      for (int i = 1; i < 5; i++) {
         g.fill(boxX, boxY + i * (boxH / 5), boxX + boxW, boxY + i * (boxH / 5) + 1, 855703296);
         g.fill(boxX + i * (boxW / 5), boxY, boxX + i * (boxW / 5) + 1, boxY + boxH, 855703296);
      }

      this.renderWave(g, boxX, boxY, boxW, boxH, this.targetAmp, this.targetFreq, this.targetPhase, 1442775040);
      this.renderWave(g, boxX, boxY, boxW, boxH, this.playerAmp, this.playerFreq, this.playerPhase, -16711936);
      this.drawSlider(g, cx, cy + 20, 150, "AMPLITUDE", this.playerAmp, 0, mouseX, mouseY);
      this.drawSlider(g, cx, cy + 40, 150, "FREQUENCY", this.playerFreq, 1, mouseX, mouseY);
      this.drawSlider(g, cx, cy + 60, 150, "PHASE", this.playerPhase, 2, mouseX, mouseY);

      for (Renderable renderable : this.renderables) {
         renderable.render(g, mouseX, mouseY, partialTick);
      }
   }

   private void drawSlider(GuiGraphics g, int x, int y, int width, String label, float value, int id, int mouseX, int mouseY) {
      g.drawString(this.font, label, x, y - 8, -16733696, false);
      g.fill(x, y + 2, x + width, y + 4, -16755456);
      int handleX = x + (int)(value * (width - 6));
      boolean hovered = mouseX >= handleX && mouseX <= handleX + 6 && mouseY >= y && mouseY <= y + 6;
      int color = !hovered && this.draggingSlider != id ? -16733696 : -11141291;
      g.fill(handleX, y, handleX + 6, y + 6, color);
   }

   private void renderWave(GuiGraphics g, int x, int y, int w, int h, float amp, float freq, float phase, int color) {
      float realFreq = 1.0F + freq * 5.0F;
      float realAmp = 5.0F + amp * (h / 2.0F - 5.0F);
      float realPhase = phase * (float) Math.PI * 2.0F;
      int prevY = -1;

      for (int i = 0; i < w; i++) {
         float normalizedX = (float)i / w;
         float sineVal = Mth.sin(normalizedX * realFreq * (float) Math.PI * 2.0F + realPhase);
         int waveY = y + h / 2 + (int)(sineVal * realAmp);
         if (prevY != -1) {
            g.fill(x + i, waveY, x + i + 2, waveY + 2, color);
         }

         prevY = waveY;
      }
   }

   private void drawHackerGraphics(GuiGraphics g, int cx, int cy, float partialTick) {
      float time = (this.ticksOpen + partialTick) * 2.0F;
      g.pose().pushPose();
      g.pose().translate(cx, cy, 0.0F);

      for (int i = 0; i < 6; i++) {
         g.pose().pushPose();
         float rot = time * (1.0F + i * 0.2F) + i * 45.0F;
         g.pose().mulPose(Axis.ZP.rotationDegrees(rot));
         int size = 20 + i * 15;
         g.fill(-size, -size, size, -size + 2, -2013200640);
         g.fill(-size, size - 2, size, size, -2013200640);
         g.fill(-size, -size, -size + 2, size, -2013200640);
         g.fill(size - 2, -size, size, size, -2013200640);
         g.pose().popPose();
      }

      g.pose().popPose();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int cx = this.width / 2;
      int cy = this.height / 2;
      if (this.checkSliderClick(cx, cy + 20, 150, this.playerAmp, 0, mouseX, mouseY)) {
         return true;
      } else if (this.checkSliderClick(cx, cy + 40, 150, this.playerFreq, 1, mouseX, mouseY)) {
         return true;
      } else {
         return this.checkSliderClick(cx, cy + 60, 150, this.playerPhase, 2, mouseX, mouseY) ? true : super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private boolean checkSliderClick(int x, int y, int width, float value, int id, double mouseX, double mouseY) {
      int handleX = x + (int)(value * (width - 6));
      if (mouseX >= handleX - 5 && mouseX <= handleX + 11 && mouseY >= y - 5 && mouseY <= y + 11) {
         this.draggingSlider = id;
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.draggingSlider = -1;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.draggingSlider != -1) {
         int cx = this.width / 2;
         float newValue = (float)(mouseX - cx) / 144.0F;
         newValue = Mth.clamp(newValue, 0.0F, 1.0F);
         if (this.draggingSlider == 0) {
            this.playerAmp = newValue;
         }

         if (this.draggingSlider == 1) {
            this.playerFreq = newValue;
         }

         if (this.draggingSlider == 2) {
            this.playerPhase = newValue;
         }

         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
