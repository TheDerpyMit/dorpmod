package com.dorp.compat.jei;

import com.dorp.DorpMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class DorpJeiPlugin implements IModPlugin {
   private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("dorp", "jei_plugin");

   public ResourceLocation getPluginUid() {
      return PLUGIN_ID;
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(new ItemStack((ItemLike)DorpMod.FORBIDDEN_WORKBENCH.get()), new RecipeType[]{RecipeTypes.CRAFTING});
   }
}
