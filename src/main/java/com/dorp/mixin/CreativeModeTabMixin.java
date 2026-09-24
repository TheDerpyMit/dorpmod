package com.dorp.mixin;

import com.dorp.DorpMod;
import com.dorp.client.DorpCreativeTab;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
   @Shadow
   private Collection<ItemStack> displayItems;
   @Shadow
   private Set<ItemStack> displayItemsSearchTab;

   @WrapMethod(method = "buildContents")
   private void dorp$buildContents(ItemDisplayParameters parameters, Operation<Void> original) {
       CreativeModeTab tab = (CreativeModeTab)(Object)this;
      if (tab == DorpMod.DORP_ITEMS_TAB.get()) {
         List<ItemStack> display = new LinkedList<>();
         Set<ItemStack> search = new LinkedHashSet<>();
         DorpCreativeTab.processItems(display::add, search::add);
         this.displayItems = display;
         this.displayItemsSearchTab = search;
      } else {
         original.call(new Object[]{parameters});
      }
   }
}
