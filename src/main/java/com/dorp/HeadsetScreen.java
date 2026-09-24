package com.dorp;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

@OnlyIn(Dist.CLIENT)
public class HeadsetScreen extends Screen {
   private static final int PANEL_W = 160;
   private static final int PANEL_H = 150;
   private final ItemStack headset;
   private EditBox freqField;
   private Button saveButton;
   private Button toggleButton;
   private int currentFreq;
   private boolean currentActive;

   public HeadsetScreen() {
      super(Component.translatable("screen.dorp.headset.title"));
      Minecraft mc = Minecraft.getInstance();
      Optional<SlotResult> slotResultOpt = CuriosApi.getCuriosInventory(mc.player).flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEADSET.get()));
      if (slotResultOpt.isPresent()) {
         this.headset = slotResultOpt.get().stack();
         this.currentFreq = HeadsetItem.getFrequency(this.headset);
         this.currentActive = HeadsetItem.isActive(this.headset);
      } else {
         this.headset = ItemStack.EMPTY;
         this.currentFreq = 1;
         this.currentActive = false;
      }
   }

   protected void init() {
      if (this.headset.isEmpty()) {
         this.onClose();
      } else {
         int x = (this.width - 160) / 2;
         int y = (this.height - 150) / 2;
         this.freqField = new EditBox(this.font, x + 10, y + 60, 140, 20, Component.translatable("screen.dorp.headset.frequency"));
         this.freqField.setValue(String.valueOf(this.currentFreq));
         this.freqField.setMaxLength(5);
         this.freqField.setFilter(text -> text.matches("^[0-9]*$"));
         this.addRenderableWidget(this.freqField);
         this.saveButton = Button.builder(Component.translatable("screen.dorp.headset.save"), btn -> {
            String val = this.freqField.getValue().trim();
            if (!val.isEmpty()) {
               try {
                  int f = Integer.parseInt(val);
                  if (f >= 1 && f <= 5000) {
                     this.currentFreq = f;
                     PacketDistributor.sendToServer(new UpdateHeadsetPayload(this.currentFreq, false), new CustomPacketPayload[0]);
                     this.onClose();
                  }
               } catch (NumberFormatException var4) {
               }
            }
         }).bounds(x + 10, y + 90, 140, 20).build();
         this.toggleButton = Button.builder(Component.translatable("screen.dorp.headset.toggle"), btn -> {
            this.currentActive = !this.currentActive;
            PacketDistributor.sendToServer(new UpdateHeadsetPayload(this.currentFreq, true), new CustomPacketPayload[0]);
            this.onClose();
         }).bounds(x + 10, y + 120, 140, 20).build();
         this.addRenderableWidget(this.saveButton);
         this.addRenderableWidget(this.toggleButton);
         this.setInitialFocus(this.freqField);
      }
   }

   public void tick() {
      if (this.freqField != null) {
         String val = this.freqField.getValue().trim();
         boolean valid = false;
         if (!val.isEmpty()) {
            try {
               int f = Integer.parseInt(val);
               valid = f >= 1 && f <= 5000;
            } catch (NumberFormatException var4) {
            }
         }

         this.saveButton.active = valid;
      }
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      g.fill(0, 0, this.width, this.height, 1426063360);
      int x = (this.width - 160) / 2;
      int y = (this.height - 150) / 2;
      g.fill(x, y, x + 160, y + 150, -3750202);
      g.fill(x, y, x + 160, y + 1, -1);
      g.fill(x, y, x + 1, y + 150, -1);
      g.fill(x + 160 - 1, y, x + 160, y + 150, -11184811);
      g.fill(x, y + 150 - 1, x + 160, y + 150, -11184811);
      g.drawCenteredString(this.font, this.title, this.width / 2, y + 8, -1);
      String statusStr = this.currentActive ? "ON" : "OFF";
      int statusColor = this.currentActive ? -11141291 : -43691;
      g.drawString(this.font, "Status: ", x + 10, y + 26, -11184811, false);
      g.drawString(this.font, statusStr, x + 50, y + 26, statusColor, false);
      g.drawString(this.font, "Freq: " + this.currentFreq, x + 85, y + 26, -11184811, false);
      g.drawString(this.font, "Frequency (1-5000):", x + 10, y + 46, -11184811, false);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.render(g, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.freqField != null) {
         this.freqField.mouseClicked(mouseX, mouseY, button);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.onClose();
         return true;
      } else {
         return this.freqField != null && this.freqField.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      return this.freqField != null && this.freqField.charTyped(codePoint, modifiers) ? true : super.charTyped(codePoint, modifiers);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
