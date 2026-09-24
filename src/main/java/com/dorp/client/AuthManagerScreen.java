package com.dorp.client;

import com.dorp.ConcealerHeadScreen;
import com.dorp.ModifyAuthListPayload;
import com.dorp.PasswordPayloads;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.neoforge.network.PacketDistributor;

public class AuthManagerScreen extends Screen {
   private final BlockPos chestPos;
   private final List<String> trustedPlayers;
   private final List<String> serverPlayers;
   private EditBox nameField;
   private Checkbox autoAuthCheckbox;
   private boolean autoAuthOwner;
   private final Map<String, ItemStack> headCache = new HashMap<>();
   private static final int PANEL_W = 320;
   private static final int PANEL_H = 200;

   public AuthManagerScreen(BlockPos chestPos, List<String> trustedPlayers, List<String> serverPlayers, boolean autoAuthOwner) {
      super(Component.literal("Auth Manager"));
      this.chestPos = chestPos;
      this.trustedPlayers = new ArrayList<>(trustedPlayers);
      this.serverPlayers = serverPlayers;
      this.autoAuthOwner = autoAuthOwner;

      for (String p : trustedPlayers) {
         this.requestHead(p);
      }
   }

   private void requestHead(String name) {
      if (!this.headCache.containsKey(name)) {
         CompletableFuture.<GameProfile>supplyAsync(() -> ConcealerHeadScreen.fetchFullProfile(name)).thenAccept(profile -> {
            if (profile != null) {
               ItemStack head = new ItemStack(Items.PLAYER_HEAD);
               head.set(DataComponents.PROFILE, new ResolvableProfile(profile));
               this.headCache.put(name, head);
            } else {
               this.headCache.put(name, ItemStack.EMPTY);
            }
         });
      }
   }

   protected void init() {
      super.init();
      int x = (this.width - 320) / 2;
      int y = (this.height - 200) / 2;
      this.nameField = new EditBox(this.font, x + 160, y + 170, 100, 20, Component.literal("Player Name"));
      this.nameField.setMaxLength(16);
      this.addRenderableWidget(this.nameField);
      this.addRenderableWidget(
         Button.builder(Component.literal("Add"), button -> this.modify(true, this.nameField.getValue())).bounds(x + 265, y + 170, 45, 20).build()
      );
      this.autoAuthCheckbox = Checkbox.builder(Component.literal("Auto-Auth"), this.font)
         .pos(x + 10, y + 200 - 25)
         .selected(this.autoAuthOwner)
         .onValueChange((checkbox, selected) -> {
            this.autoAuthOwner = selected;
            PacketDistributor.sendToServer(new PasswordPayloads.ToggleAutoAuth(this.chestPos, selected), new CustomPacketPayload[0]);
         })
         .build();
      this.addRenderableWidget(this.autoAuthCheckbox);
      int listY = y + 40;

      for (String p : this.serverPlayers) {
         if (listY > y + 150) {
            break;
         }

         this.addRenderableWidget(Button.builder(Component.literal(p), button -> this.modify(true, p)).bounds(x + 10, listY, 130, 20).build());
         listY += 22;
         this.requestHead(p);
      }
   }

   private void modify(boolean isAdd, String name) {
      name = name.trim();
      if (!name.isEmpty()) {
         PacketDistributor.sendToServer(new ModifyAuthListPayload(this.chestPos, name, isAdd), new CustomPacketPayload[0]);
         if (isAdd && !this.trustedPlayers.contains(name)) {
            this.trustedPlayers.add(name);
            this.requestHead(name);
         }

         if (!isAdd) {
            this.trustedPlayers.remove(name);
         }

         this.nameField.setValue("");
      }
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(g, mouseX, mouseY, partialTick);
      g.fill(0, 0, this.width, this.height, -1442840576);
      int x = (this.width - 320) / 2;
      int y = (this.height - 200) / 2;
      g.fill(x, y, x + 320, y + 200, -300016092);
      g.fill(x, y, x + 320, y + 1, -11184811);
      g.fill(x, y + 200 - 1, x + 320, y + 200, -15658735);
      g.fill(x, y, x + 1, y + 200, -13421773);
      g.fill(x + 320 - 1, y, x + 320, y + 200, -15658735);
      g.drawCenteredString(this.font, this.title, this.width / 2, y + 10, -1);
      g.fill(x + 150, y + 30, x + 151, y + 200 - 35, -13421773);

      for (Renderable renderable : this.renderables) {
         renderable.render(g, mouseX, mouseY, partialTick);
      }

      g.drawString(this.font, "Server Players", x + 15, y + 25, 11184810);
      g.drawString(this.font, "Trusted Players", x + 160, y + 25, 5635925);
      int ty = y + 40;

      for (String tp : this.trustedPlayers) {
         if (ty > y + 150) {
            break;
         }

         ItemStack head = this.headCache.get(tp);
         if (head != null && !head.isEmpty()) {
            g.renderItem(head, x + 160, ty);
         } else {
            g.fill(x + 160, ty, x + 176, ty + 16, 1442840575);
         }

         g.drawString(this.font, tp, x + 180, ty + 4, 16777215);
         boolean hoverX = mouseX >= x + 320 - 20 && mouseX <= x + 320 - 5 && mouseY >= ty && mouseY <= ty + 16;
         g.drawString(this.font, "X", x + 320 - 15, ty + 4, hoverX ? -43691 : -5592406);
         ty += 20;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         int x = (this.width - 320) / 2;
         int y = (this.height - 200) / 2;
         int ty = y + 40;

         for (int i = 0; i < this.trustedPlayers.size() && ty <= y + 150; i++) {
            if (mouseX >= x + 320 - 20 && mouseX <= x + 320 - 5 && mouseY >= ty && mouseY <= ty + 16) {
               this.modify(false, this.trustedPlayers.get(i));
               return true;
            }

            ty += 20;
         }

         return false;
      }
   }

   public void onClose() {
      PacketDistributor.sendToServer(new PasswordPayloads.AuthMenuClosed(this.chestPos), new CustomPacketPayload[0]);
      super.onClose();
   }

   public boolean isPauseScreen() {
      return false;
   }
}
