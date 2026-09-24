package com.dorp;

import com.dorp.config.DorpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class WalkieTalkieScreen extends Screen {
   private static final int PANEL_W = 160;
   private static final int PANEL_H = 180;
   private final InteractionHand hand;
   private final ItemStack walkieTalkie;
   private EditBox freqField;
   private EditBox sosMsgField;
   private Button saveButton;
   private Button toggleButton;
   private Button sosButton;
   private Button confirmButton;
   private Button cancelButton;
   private int currentFreq;
   private boolean currentActive;
   private boolean confirmingSOS = false;

   public WalkieTalkieScreen(InteractionHand hand) {
      super(Component.translatable("screen.dorp.walkietalkie.title"));
      this.hand = hand;
      Minecraft mc = Minecraft.getInstance();
      this.walkieTalkie = mc.player.getItemInHand(hand);
      this.currentFreq = WalkieTalkieItem.getFrequency(this.walkieTalkie);
      this.currentActive = WalkieTalkieItem.isActive(this.walkieTalkie);
   }

   protected void init() {
      int x = (this.width - 160) / 2;
      int y = (this.height - 180) / 2;
      if (this.confirmingSOS) {
         this.sosMsgField = new EditBox(this.font, x + 10, y + 80, 140, 20, Component.literal("SOS Message"));
         this.sosMsgField.setMaxLength(25);
         this.confirmButton = Button.builder(Component.literal("Confirm"), btn -> {
            String msg = this.sosMsgField.getValue().trim();
            PacketDistributor.sendToServer(new UpdateWalkieTalkiePayload(this.currentFreq, false, true, msg), new CustomPacketPayload[0]);
            this.onClose();
         }).bounds(x + 10, y + 110, 140, 20).build();
         this.cancelButton = Button.builder(Component.literal("Cancel"), btn -> {
            this.confirmingSOS = false;
            this.rebuildWidgets();
         }).bounds(x + 10, y + 140, 140, 20).build();
         this.addRenderableWidget(this.sosMsgField);
         this.addRenderableWidget(this.confirmButton);
         this.addRenderableWidget(this.cancelButton);
      } else {
         this.freqField = new EditBox(this.font, x + 10, y + 60, 140, 20, Component.translatable("screen.dorp.walkietalkie.frequency"));
         this.freqField.setValue(String.valueOf(this.currentFreq));
         this.freqField.setMaxLength(5);
         this.freqField.setFilter(text -> text.matches("^[0-9]*$"));
         this.addRenderableWidget(this.freqField);
         this.saveButton = Button.builder(Component.translatable("screen.dorp.walkietalkie.save"), btn -> {
            String val = this.freqField.getValue().trim();
            if (!val.isEmpty()) {
               try {
                  int f = Integer.parseInt(val);
                  if (f >= 1 && f <= 5000) {
                     this.currentFreq = f;
                     PacketDistributor.sendToServer(new UpdateWalkieTalkiePayload(this.currentFreq, false, false, ""), new CustomPacketPayload[0]);
                     this.onClose();
                  }
               } catch (NumberFormatException var4x) {
               }
            }
         }).bounds(x + 10, y + 90, 140, 20).build();
         this.toggleButton = Button.builder(Component.translatable("screen.dorp.walkietalkie.toggle"), btn -> {
            this.currentActive = !this.currentActive;
            PacketDistributor.sendToServer(new UpdateWalkieTalkiePayload(this.currentFreq, true, false, ""), new CustomPacketPayload[0]);
            this.onClose();
         }).bounds(x + 10, y + 120, 140, 20).build();
         int sosUses = WalkieTalkieItem.getSosUses(this.walkieTalkie);
         int maxUses = DorpConfig.SOS_MAX_USES.get();
         this.sosButton = Button.builder(Component.literal("Send SOS (" + sosUses + "/" + maxUses + ")"), btn -> {
            this.confirmingSOS = true;
            this.rebuildWidgets();
         }).bounds(x + 10, y + 150, 140, 20).build();
         this.sosButton.active = this.currentActive && sosUses > 0;
         this.addRenderableWidget(this.saveButton);
         this.addRenderableWidget(this.toggleButton);
         this.addRenderableWidget(this.sosButton);
         this.setInitialFocus(this.freqField);
      }
   }

   public void tick() {
      if (!this.confirmingSOS) {
         if (this.freqField != null && this.saveButton != null) {
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
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      g.fill(0, 0, this.width, this.height, 1426063360);
      int x = (this.width - 160) / 2;
      int y = (this.height - 180) / 2;
      g.fill(x, y, x + 160, y + 180, -3750202);
      g.fill(x, y, x + 160, y + 1, -1);
      g.fill(x, y, x + 1, y + 180, -1);
      g.fill(x + 160 - 1, y, x + 160, y + 180, -11184811);
      g.fill(x, y + 180 - 1, x + 160, y + 180, -11184811);
      g.drawCenteredString(this.font, this.title, this.width / 2, y + 8, -1);
      if (this.confirmingSOS) {
         this.drawCenteredNoShadow(g, "SEND SOS DISTRESS PING?", this.width / 2, y + 25, -43691);
         this.drawCenteredNoShadow(g, "This will broadcast your", this.width / 2, y + 45, -11184811);
         this.drawCenteredNoShadow(g, "coords to all frequencies", this.width / 2, y + 57, -11184811);
         this.drawCenteredNoShadow(g, "in a 2400-block range.", this.width / 2, y + 69, -11184811);
      } else {
         String statusStr = this.currentActive ? "ON" : "OFF";
         int statusColor = this.currentActive ? -11141291 : -43691;
         g.drawString(this.font, "Status: ", x + 10, y + 26, -11184811, false);
         g.drawString(this.font, statusStr, x + 50, y + 26, statusColor, false);
         g.drawString(this.font, "Freq: " + this.currentFreq, x + 85, y + 26, -11184811, false);
         g.drawString(this.font, "Frequency (1-5000):", x + 10, y + 46, -11184811, false);
      }
   }

   private void drawCenteredNoShadow(GuiGraphics g, String text, int x, int y, int color) {
      g.drawString(this.font, text, x - this.font.width(text) / 2, y, color, false);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.render(g, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.confirmingSOS && this.sosMsgField != null) {
         this.sosMsgField.mouseClicked(mouseX, mouseY, button);
      } else if (!this.confirmingSOS && this.freqField != null) {
         this.freqField.mouseClicked(mouseX, mouseY, button);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.onClose();
         return true;
      } else if (this.confirmingSOS && this.sosMsgField != null && this.sosMsgField.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else {
         return !this.confirmingSOS && this.freqField != null && this.freqField.keyPressed(keyCode, scanCode, modifiers)
            ? true
            : super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      if (this.confirmingSOS && this.sosMsgField != null && this.sosMsgField.charTyped(codePoint, modifiers)) {
         return true;
      } else {
         return !this.confirmingSOS && this.freqField != null && this.freqField.charTyped(codePoint, modifiers) ? true : super.charTyped(codePoint, modifiers);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
