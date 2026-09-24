package com.dorp.mixin;

import com.dorp.client.DorpCreativeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$ItemPickerMenu")
public abstract class ItemPickerMenuMixin {
   @Shadow
   protected abstract int getRowIndexForScroll(float var1);

   @Inject(method = "scrollTo", at = @At("HEAD"))
   private void dorp$scrollTo(float scroll, CallbackInfo ci) {
      DorpCreativeTab.CURRENT_ROW = this.getRowIndexForScroll(scroll);
   }
}
