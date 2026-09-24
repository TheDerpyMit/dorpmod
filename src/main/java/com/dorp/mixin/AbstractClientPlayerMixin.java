package com.dorp.mixin;

import com.dorp.DorpMod;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
   @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
   private void dorp$onGetSkin(CallbackInfoReturnable<PlayerSkin> cir) {
       AbstractClientPlayer player = (AbstractClientPlayer)(Object)this;
      ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
      if (headStack.is((Item)DorpMod.PAPER_BAG.get())) {
         cir.setReturnValue(DefaultPlayerSkin.get(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed4")));
      } else if (headStack.is(Items.PLAYER_HEAD)) {
         CustomData customData = (CustomData)headStack.get(DataComponents.CUSTOM_DATA);
         if (customData != null && customData.copyTag().getBoolean("concealer")) {
            ResolvableProfile profile = (ResolvableProfile)headStack.get(DataComponents.PROFILE);
            if (profile != null && profile.gameProfile() != null) {
               PlayerSkin targetSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile.gameProfile());
               if (targetSkin != null) {
                  cir.setReturnValue(targetSkin);
               }
            }
         }
      }
   }
}
