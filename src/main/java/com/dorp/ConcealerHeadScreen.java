package com.dorp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class ConcealerHeadScreen extends Screen {
   private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
   private static final int PANEL_W = 160;
   private static final int PANEL_H = 175;
   private EditBox nameField;
   private Button craftButton;
   private ItemStack previewStack = ItemStack.EMPTY;
   private String lastLookup = "";
   private String errorMsg = "";
   private ConcealerHeadScreen.State state = ConcealerHeadScreen.State.IDLE;
   private UUID resolvedUuid = null;
   private String resolvedTexVal = "";
   private String resolvedTexSig = "";
   private CompletableFuture<GameProfile> pendingFuture = null;

   public ConcealerHeadScreen() {
      super(Component.literal("Concealer Head"));
   }

   protected void init() {
      int x = (this.width - 160) / 2;
      int y = (this.height - 175) / 2;
      this.nameField = new EditBox(this.font, x + 10, y + 100, 140, 20, Component.literal("Player Name"));
      this.nameField.setMaxLength(16);
      this.addWidget(this.nameField);
      this.craftButton = Button.builder(
            Component.literal("Craft Head"),
            btn -> {
               if (this.state == ConcealerHeadScreen.State.FOUND && this.resolvedUuid != null) {
                  PacketDistributor.sendToServer(
                     new CraftConcealerHeadPayload(this.nameField.getValue().trim(), this.resolvedUuid.toString(), this.resolvedTexVal, this.resolvedTexSig),
                     new CustomPacketPayload[0]
                  );
                  this.onClose();
               }
            }
         )
         .bounds(x + 10, y + 128, 140, 20)
         .build();
      this.craftButton.active = false;
      this.addRenderableWidget(this.craftButton);
      this.setInitialFocus(this.nameField);
   }

   public void tick() {
      String name = this.nameField.getValue().trim();
      this.craftButton.active = this.state == ConcealerHeadScreen.State.FOUND;
      if (!name.equals(this.lastLookup)) {
         this.lastLookup = name;
         this.errorMsg = "";
         this.previewStack = ItemStack.EMPTY;
         this.resolvedUuid = null;
         this.resolvedTexVal = "";
         this.resolvedTexSig = "";
         this.state = ConcealerHeadScreen.State.IDLE;
         if (this.pendingFuture != null) {
            this.pendingFuture.cancel(true);
            this.pendingFuture = null;
         }

         if (VALID_NAME.matcher(name).matches()) {
            this.state = ConcealerHeadScreen.State.LOADING;
            this.pendingFuture = CompletableFuture.<GameProfile>supplyAsync(() -> fetchFullProfile(name)).whenCompleteAsync((profile, err) -> {
               if (name.equals(this.nameField.getValue().trim())) {
                  if (profile != null) {
                     this.resolvedUuid = profile.getId();
                     Collection<Property> texProps = profile.getProperties().get("textures");
                     if (!texProps.isEmpty()) {
                        Property p = texProps.iterator().next();
                        this.resolvedTexVal = p.value();
                        this.resolvedTexSig = p.signature() != null ? p.signature() : "";
                     }

                     this.state = ConcealerHeadScreen.State.FOUND;
                     this.errorMsg = "";
                     ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
                     skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
                     this.previewStack = skull;
                  } else {
                     this.state = ConcealerHeadScreen.State.NOT_FOUND;
                     this.errorMsg = "Player not found!";
                  }
               }
            }, Minecraft.getInstance()::execute);
         }
      }
   }

   public static GameProfile fetchFullProfile(String name) {
      try {
         HttpURLConnection c1 = (HttpURLConnection)new URI("https://api.mojang.com/users/profiles/minecraft/" + name).toURL().openConnection();
         c1.setConnectTimeout(5000);
         c1.setReadTimeout(5000);
         c1.setRequestProperty("User-Agent", "DorpMod/1.0");
         if (c1.getResponseCode() != 200) {
            return null;
         } else {
            JsonObject obj1 = JsonParser.parseString(new String(c1.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            String rawId = obj1.get("id").getAsString();
            String realName = obj1.get("name").getAsString();
            UUID uuid = UUID.fromString(rawId.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
            GameProfile profile = new GameProfile(uuid, realName);
            HttpURLConnection c2 = (HttpURLConnection)new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + rawId + "?unsigned=false")
               .toURL()
               .openConnection();
            c2.setConnectTimeout(5000);
            c2.setReadTimeout(5000);
            c2.setRequestProperty("User-Agent", "DorpMod/1.0");
            if (c2.getResponseCode() == 200) {
               JsonObject obj2 = JsonParser.parseString(new String(c2.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
               if (obj2.has("properties")) {
                  for (JsonElement el : obj2.getAsJsonArray("properties")) {
                     JsonObject p = el.getAsJsonObject();
                     String pn = p.get("name").getAsString();
                     String pv = p.get("value").getAsString();
                     String ps = p.has("signature") ? p.get("signature").getAsString() : null;
                     profile.getProperties().put(pn, new Property(pn, pv, ps));
                  }
               }
            }

            return profile;
         }
      } catch (Exception var15) {
         return null;
      }
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      g.fill(0, 0, this.width, this.height, 1426063360);
      int x = (this.width - 160) / 2;
      int y = (this.height - 175) / 2;
      g.fill(x, y, x + 160, y + 175, -3750202);
      g.fill(x, y, x + 160, y + 1, -1);
      g.fill(x, y, x + 1, y + 175, -1);
      g.fill(x + 160 - 1, y, x + 160, y + 175, -11184811);
      g.fill(x, y + 175 - 1, x + 160, y + 175, -11184811);
      g.drawCenteredString(this.font, this.title, this.width / 2, y + 6, -1);
      g.drawCenteredString(this.font, "Preview", this.width / 2, y + 17, -2236963);
      int fx = this.width / 2 - 20;
      int fy = y + 26;
      g.fill(fx - 1, fy - 1, fx + 41, fy + 41, -13158601);
      g.fill(fx, fy, fx + 40, fy + 40, -7631989);
      if (this.state == ConcealerHeadScreen.State.FOUND && !this.previewStack.isEmpty()) {
         g.pose().pushPose();
         g.pose().translate(fx + 20.0F, fy + 20.0F, 200.0F);
         g.pose().scale(2.5F, 2.5F, 2.5F);
         g.renderItem(this.previewStack, -8, -8);
         g.pose().popPose();
      } else if (this.state == ConcealerHeadScreen.State.LOADING) {
         int dots = (int)(System.currentTimeMillis() / 400L % 4L);
         g.drawCenteredString(this.font, ".".repeat(dots), this.width / 2, fy + 14, -6710887);
      }

      if (this.state == ConcealerHeadScreen.State.LOADING) {
         g.drawCenteredString(this.font, "Fetching skin...", this.width / 2, fy + 46, -5592406);
      } else if (this.state == ConcealerHeadScreen.State.FOUND) {
         g.drawCenteredString(this.font, this.nameField.getValue().trim(), this.width / 2, fy + 46, -11184811);
      }

      g.drawString(this.font, "Player Name:", x + 10, y + 90, -11184811, false);
      if (!this.errorMsg.isEmpty()) {
         g.drawCenteredString(this.font, this.errorMsg, this.width / 2, y + 175 - 6, -48060);
      }

      for (Renderable renderable : this.renderables) {
         renderable.render(g, mouseX, mouseY, partialTick);
      }

      this.nameField.render(g, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.nameField.mouseClicked(mouseX, mouseY, button);
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.onClose();
         return true;
      } else {
         return this.nameField.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      return this.nameField.charTyped(codePoint, modifiers) ? true : super.charTyped(codePoint, modifiers);
   }

   public void onClose() {
      if (this.pendingFuture != null) {
         this.pendingFuture.cancel(true);
      }

      super.onClose();
   }

   public boolean isPauseScreen() {
      return false;
   }

   private static enum State {
      IDLE,
      LOADING,
      FOUND,
      NOT_FOUND;
   }
}
