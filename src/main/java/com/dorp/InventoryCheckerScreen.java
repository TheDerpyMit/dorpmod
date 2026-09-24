package com.dorp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class InventoryCheckerScreen extends Screen {
   private final BlockPos pos;
   private String statusText = "Ready to scan";
   private final Map<String, Map<Integer, ItemStack>> allScannedData = new HashMap<>();
   private final List<String> detectedPlayers = new ArrayList<>();
   private String selectedPlayer = null;

   public InventoryCheckerScreen(BlockPos pos) {
      super(Component.translatable("gui.dorp.inventory_checker"));
      this.pos = pos;
   }

   protected void init() {
      super.init();
      this.buildUI();
   }

   private void buildUI() {
      this.clearWidgets();
      int centerX = this.width / 2;
      int bottomY = this.height - 40;
      this.addRenderableWidget(Button.builder(Component.literal("Scan"), btn -> {
         this.statusText = "Scanning...";
         this.allScannedData.clear();
         this.detectedPlayers.clear();
         this.selectedPlayer = null;
         this.buildUI();
         PacketDistributor.sendToServer(new ScanPlayerPayload(this.pos), new CustomPacketPayload[0]);
      }).bounds(centerX - 50, bottomY, 100, 20).build());
      int rightX = centerX + 81 + 10;
      int startY = 50;

      for (int i = 0; i < this.detectedPlayers.size(); i++) {
         String pName = this.detectedPlayers.get(i);
         int btnY = startY + i * 24;
         Component label = Component.literal(pName);
         if (pName.equals(this.selectedPlayer)) {
            label = Component.literal("> " + pName + " <").withStyle(ChatFormatting.YELLOW);
         }

         this.addRenderableWidget(Button.builder(label, btn -> {
            this.selectedPlayer = pName;
            this.buildUI();
         }).bounds(rightX, btnY, 100, 20).build());
      }
   }

   public void receiveScannedData(String playerName, CompoundTag inventoryData) {
      this.allScannedData.clear();
      this.detectedPlayers.clear();
      this.selectedPlayer = null;
      if (inventoryData.isEmpty()) {
         this.statusText = "No player detected";
      } else {
         for (String key : inventoryData.getAllKeys()) {
            this.detectedPlayers.add(key);
            CompoundTag invTag = inventoryData.getCompound(key);
            Map<Integer, ItemStack> items = new HashMap<>();
            if (invTag.contains("Items", 9)) {
               ListTag list = invTag.getList("Items", 10);

               for (int i = 0; i < list.size(); i++) {
                  CompoundTag itemWrapper = list.getCompound(i);
                  int slot = itemWrapper.getInt("Slot");
                  ItemStack stack = ItemStack.parseOptional(this.minecraft.level.registryAccess(), itemWrapper.getCompound("Item"));
                  if (!stack.isEmpty()) {
                     items.put(slot, stack);
                  }
               }
            }

            this.allScannedData.put(key, items);
         }

         if (!this.detectedPlayers.isEmpty()) {
            this.statusText = "Detected " + this.detectedPlayers.size() + " player(s)";
            if (playerName != null && !playerName.isEmpty()) {
               this.selectedPlayer = playerName;
            } else {
               this.selectedPlayer = this.detectedPlayers.get(0);
            }
         }
      }

      this.buildUI();
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      int centerX = this.width / 2;
      guiGraphics.drawCenteredString(this.font, this.statusText, centerX, 20, 16777215);
      if (this.selectedPlayer != null && this.allScannedData.containsKey(this.selectedPlayer)) {
         Map<Integer, ItemStack> currentItems = this.allScannedData.get(this.selectedPlayer);
         int startX = centerX - 81;
         int startY = 50;

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               int slotIndex = 9 + row * 9 + col;
               int x = startX + col * 18;
               int y = startY + row * 18;
               this.renderSlot(guiGraphics, currentItems, x, y, slotIndex, mouseX, mouseY);
            }
         }

         int hotbarY = startY + 54 + 4;

         for (int col = 0; col < 9; col++) {
            int x = startX + col * 18;
            this.renderSlot(guiGraphics, currentItems, x, hotbarY, col, mouseX, mouseY);
         }

         int armorX = startX - 30;

         for (int i = 0; i < 4; i++) {
            int slotIndex = 39 - i;
            int y = startY + i * 18;
            this.renderSlot(guiGraphics, currentItems, armorX, y, slotIndex, mouseX, mouseY);
         }

         this.renderSlot(guiGraphics, currentItems, armorX, startY + 72 + 4, 40, mouseX, mouseY);
      }
   }

   private void renderSlot(GuiGraphics guiGraphics, Map<Integer, ItemStack> items, int x, int y, int slotIndex, int mouseX, int mouseY) {
      guiGraphics.fill(x, y, x + 18, y + 18, -2004318072);
      guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, -2013265920);
      ItemStack stack = items.get(slotIndex);
      if (stack != null && !stack.isEmpty()) {
         guiGraphics.renderItem(stack, x + 1, y + 1);
         guiGraphics.renderItemDecorations(this.font, stack, x + 1, y + 1);
         if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
            guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
         }
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
