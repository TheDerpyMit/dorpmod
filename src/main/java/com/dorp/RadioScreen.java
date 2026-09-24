package com.dorp;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class RadioScreen extends Screen {
   private final RadioBlockEntity radio;
   private EditBox frequencyBox;
   private Button toggleButton;

   public RadioScreen(RadioBlockEntity radio) {
      super(Component.literal("Radio"));
      this.radio = radio;
   }

   protected void init() {
      super.init();
      int startX = (this.width - 150) / 2;
      int startY = (this.height - 100) / 2;
      this.frequencyBox = new EditBox(this.font, startX, startY + 20, 150, 20, Component.literal("Frequency"));
      this.frequencyBox.setMaxLength(4);
      this.frequencyBox.setValue(String.valueOf(this.radio.getFrequency()));
      this.frequencyBox.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
      this.addRenderableWidget(this.frequencyBox);
      Component toggleText = Component.literal(this.radio.isActive() ? "Turn OFF" : "Turn ON");
      this.toggleButton = Button.builder(toggleText, btn -> {
         int freq = this.parseFrequency(this.frequencyBox.getValue());
         PacketDistributor.sendToServer(new UpdateRadioPayload(this.radio.getBlockPos(), freq, true), new CustomPacketPayload[0]);
         this.onClose();
      }).bounds(startX, startY + 50, 150, 20).build();
      this.addRenderableWidget(this.toggleButton);
      this.addRenderableWidget(Button.builder(Component.literal("Save Frequency"), btn -> {
         int freq = this.parseFrequency(this.frequencyBox.getValue());
         PacketDistributor.sendToServer(new UpdateRadioPayload(this.radio.getBlockPos(), freq, false), new CustomPacketPayload[0]);
         this.onClose();
      }).bounds(startX, startY + 75, 150, 20).build());
   }

   private int parseFrequency(String val) {
      try {
         int f = Integer.parseInt(val);
         return Math.max(1, Math.min(5000, f));
      } catch (NumberFormatException var3) {
         return 1;
      }
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      guiGraphics.fill(0, 0, this.width, this.height, 1426063360);
      int startX = (this.width - 150) / 2;
      int startY = (this.height - 100) / 2;
      guiGraphics.fill(startX - 10, startY - 10, startX + 160, startY + 110, -3750202);
      guiGraphics.fill(startX - 10, startY - 10, startX + 160, startY - 9, -1);
      guiGraphics.fill(startX - 10, startY - 10, startX - 9, startY + 110, -1);
      guiGraphics.fill(startX + 159, startY - 10, startX + 160, startY + 110, -11184811);
      guiGraphics.fill(startX - 10, startY + 109, startX + 160, startY + 110, -11184811);
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      int startX = (this.width - 150) / 2;
      int startY = (this.height - 100) / 2;
      guiGraphics.drawCenteredString(this.font, "Frequency (1-5000)", this.width / 2, startY, 16777215);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
