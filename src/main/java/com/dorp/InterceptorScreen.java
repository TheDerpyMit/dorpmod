package com.dorp;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;

public class InterceptorScreen extends Screen {
   private final InterceptorBlockEntity interceptor;
   private Button toggleButton;

   public InterceptorScreen(InterceptorBlockEntity interceptor) {
      super(Component.translatable("gui.dorp.interceptor.title"));
      this.interceptor = interceptor;
   }

   protected void init() {
      super.init();
      int startX = (this.width - 150) / 2;
      int startY = (this.height - 100) / 2;
      boolean isActive = this.interceptor.isActive();
      this.toggleButton = Button.builder(Component.literal(isActive ? "Turn OFF" : "Turn ON"), button -> {
         boolean nextState = !this.interceptor.isActive();
         this.interceptor.setActive(nextState);
         button.setMessage(Component.literal(nextState ? "Turn OFF" : "Turn ON"));
         PacketDistributor.sendToServer(new UpdateInterceptorPayload(this.interceptor.getBlockPos(), nextState), new CustomPacketPayload[0]);
      }).bounds(startX + 10, startY + 30, 130, 20).build();
      this.addRenderableWidget(this.toggleButton);
      this.addRenderableWidget(Button.builder(Component.literal("Close"), btn -> this.onClose()).bounds(startX + 10, startY + 60, 130, 20).build());
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
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, startY, 16777215);
      int energy = this.interceptor.getEnergyStored();
      int maxEnergy = this.interceptor.getMaxEnergyStored();
      guiGraphics.drawCenteredString(this.font, Component.literal("Energy: " + energy + " / " + maxEnergy + " FE"), this.width / 2, startY + 15, 65535);
      boolean connected = false;
      Level level = this.interceptor.getLevel();
      if (level != null && this.interceptor.getBlockState().hasProperty(InterceptorBlock.FACING)) {
         Direction facing = (Direction)this.interceptor.getBlockState().getValue(InterceptorBlock.FACING);
         Direction rightFace = facing.getClockWise();
         BlockPos neighborPos = this.interceptor.getBlockPos().relative(rightFace);
         IEnergyStorage cap = (IEnergyStorage)level.getCapability(EnergyStorage.BLOCK, neighborPos, rightFace.getOpposite());
         if (cap != null && cap.canExtract()) {
            connected = true;
         }
      }

      if (connected) {
         guiGraphics.drawCenteredString(this.font, Component.literal("Power Source Detected: OK"), this.width / 2, startY + 85, 65280);
      } else {
         guiGraphics.drawCenteredString(this.font, Component.literal("Power Source Detected: None"), this.width / 2, startY + 85, 16711680);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
