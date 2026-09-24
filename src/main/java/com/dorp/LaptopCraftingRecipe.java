package com.dorp;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class LaptopCraftingRecipe extends CustomRecipe {
   public LaptopCraftingRecipe(CraftingBookCategory category) {
      super(category);
   }

   public boolean matches(CraftingInput input, Level level) {
      return input.width() >= 3 && input.height() >= 3
         ? this.isIron(input.getItem(0, 0))
            && this.isIron(input.getItem(1, 0))
            && this.isIron(input.getItem(2, 0))
            && this.isRedstone(input.getItem(0, 1))
            && this.isGlass(input.getItem(1, 1))
            && this.isRedstone(input.getItem(2, 1))
            && this.isIron(input.getItem(0, 2))
            && this.isIron(input.getItem(1, 2))
            && this.isIron(input.getItem(2, 2))
         : false;
   }

   private boolean isIron(ItemStack stack) {
      return stack.is(Items.IRON_INGOT);
   }

   private boolean isRedstone(ItemStack stack) {
      return stack.is(Items.REDSTONE);
   }

   private boolean isGlass(ItemStack stack) {
      return stack.is(Items.GLASS_PANE);
   }

   public ItemStack assemble(CraftingInput input, Provider registries) {
      return ImmersiveLaptopHelper.createLaptopStack();
   }

   public ItemStack getResultItem(Provider registries) {
      return ImmersiveLaptopHelper.createLaptopStack();
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width >= 3 && height >= 3;
   }

   public RecipeSerializer<?> getSerializer() {
      return DorpMod.LAPTOP_RECIPE_SERIALIZER.get();
   }
}
