package com.dorp;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfidentialWarningScreen extends Screen {
   private final Screen parent;

   public ConfidentialWarningScreen(Screen parent) {
      super(Component.literal("Confidential Build Warning"));
      this.parent = parent;
   }

   protected void init() {
      int btnW = 150;
      int btnH = 20;
      int btnX = (this.width - btnW) / 2;
      int btnY = this.height / 2 + 50;
      this.addRenderableWidget(Button.builder(Component.literal("I Understand"), btn -> {
         if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
         }
      }).bounds(btnX, btnY, btnW, btnH).build());
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      g.fill(0, 0, this.width, this.height, -536541947);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.render(g, mouseX, mouseY, partialTick);
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      g.drawCenteredString(this.font, "WARNING: CONFIDENTIAL BUILD", centerX, centerY - 50, -65536);
      g.drawCenteredString(this.font, "DO NOT DISTRIBUTE", centerX, centerY - 35, -65536);
      g.drawCenteredString(this.font, "This is an unreleased development build of Dorp Mod.", centerX, centerY - 10, -1);
      g.drawCenteredString(this.font, "Sharing, distributing, or publishing this build or any content", centerX, centerY + 5, -1);
      g.drawCenteredString(this.font, "showing it is strictly prohibited without explicit permission. (im looking at u emir)", centerX, centerY + 20, -1);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}
