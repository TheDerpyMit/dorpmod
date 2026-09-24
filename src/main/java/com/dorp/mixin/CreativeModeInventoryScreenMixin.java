package com.dorp.mixin;

import com.dorp.DorpMod;
import com.dorp.client.DorpCreativeTab;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
   private static boolean isDorpTab() {
      CreativeModeTab tab = CreativeModeInventoryScreenAccessor.getSelectedTab();
      return tab == DorpMod.DORP_ITEMS_TAB.get();
   }

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"
      )
   )
   private void dorp$renderBanners(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if (isDorpTab()) {
          DorpCreativeTab.renderBanners((CreativeModeInventoryScreen)(Object)this, graphics, mouseX, mouseY);
      }
   }

   @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
   private void dorp$getTooltipFromContainerItem(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
      List<Component> tooltip = (List<Component>)cir.getReturnValue();
      if (tooltip != null) {
         String category = DorpCreativeTab.getItemCategory(stack);
         if (category != null) {
            int index = Math.min(1, tooltip.size());
            Component categoryName = Component.translatable("dorp.creative_tab." + category).withStyle(ChatFormatting.BLUE);
            tooltip.add(index, categoryName);
         }
      }
   }
}
