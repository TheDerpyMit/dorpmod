package com.dorp.client;

import com.dorp.PasswordPayloads;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class EnterPasswordScreen extends Screen {
   private final BlockPos pos;
   private EditBox digit1;
   private EditBox digit2;
   private EditBox digit3;
   private Button submitButton;

   public EnterPasswordScreen(BlockPos pos) {
      super(Component.literal("Enter Password"));
      this.pos = pos;
   }

   public void onClose() {
      PacketDistributor.sendToServer(new PasswordPayloads.AuthMenuClosed(this.pos), new CustomPacketPayload[0]);
      super.onClose();
   }

   protected void init() {
      super.init();
      int cx = this.width / 2;
      int cy = this.height / 2;
      int boxWidth = 30;
      int spacing = 10;
      int startX = cx - (boxWidth * 3 + spacing * 2) / 2;
      this.digit1 = new EditBox(this.font, startX, cy - 20, boxWidth, 20, Component.literal(""));
      this.digit1.setMaxLength(1);
      this.digit1.setFilter(s -> s.matches("\\d?"));
      this.addRenderableWidget(this.digit1);
      this.digit2 = new EditBox(this.font, startX + boxWidth + spacing, cy - 20, boxWidth, 20, Component.literal(""));
      this.digit2.setMaxLength(1);
      this.digit2.setFilter(s -> s.matches("\\d?"));
      this.addRenderableWidget(this.digit2);
      this.digit3 = new EditBox(this.font, startX + (boxWidth + spacing) * 2, cy - 20, boxWidth, 20, Component.literal(""));
      this.digit3.setMaxLength(1);
      this.digit3.setFilter(s -> s.matches("\\d?"));
      this.addRenderableWidget(this.digit3);
      this.submitButton = Button.builder(Component.literal("Submit"), b -> {
         String pass = this.digit1.getValue() + this.digit2.getValue() + this.digit3.getValue();
         if (pass.length() == 3 && pass.matches("\\d{3}")) {
            PacketDistributor.sendToServer(new PasswordPayloads.SubmitEnterPassword(this.pos, pass), new CustomPacketPayload[0]);
            this.onClose();
         }
      }).bounds(cx - 50, cy + 10, 100, 20).build();
      this.submitButton.active = false;
      Consumer<String> responder = s -> {
         boolean valid = this.digit1.getValue().length() == 1 && this.digit2.getValue().length() == 1 && this.digit3.getValue().length() == 1;
         this.submitButton.active = valid;
      };
      this.digit1.setResponder(s -> {
         responder.accept(s);
         if (s.length() == 1) {
            this.setFocused(this.digit2);
         }
      });
      this.digit2.setResponder(s -> {
         responder.accept(s);
         if (s.length() == 1) {
            this.setFocused(this.digit3);
         } else if (s.isEmpty()) {
            this.setFocused(this.digit1);
         }
      });
      this.digit3.setResponder(s -> {
         responder.accept(s);
         if (s.isEmpty()) {
            this.setFocused(this.digit2);
         }
      });
      this.addRenderableWidget(this.submitButton);
   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(gui, mouseX, mouseY, partialTick);
      super.render(gui, mouseX, mouseY, partialTick);
      gui.drawCenteredString(this.font, "Enter Password", this.width / 2, this.height / 2 - 50, 16777215);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
