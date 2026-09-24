package com.dorp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ForbiddenWorkbenchRecipeBookScreen extends Screen {
   private final Screen parentScreen;
   private List<ForbiddenWorkbenchRecipeBookScreen.RecipeEntry> recipes;
   private int leftPageIndex = 0;
   private ForbiddenWorkbenchRecipeBookScreen.RecipeEntry selectedRecipe = null;
   private Button prevButton;
   private Button nextButton;
   private Button backButton;

   public ForbiddenWorkbenchRecipeBookScreen(Screen parentScreen) {
      super(Component.literal("Forbidden Workbench Recipes"));
      this.parentScreen = parentScreen;
   }

   private void initRecipes() {
      if (this.recipes == null) {
         this.recipes = new ArrayList<>();
         ItemStack bronzeIngot = new ItemStack(Items.COPPER_INGOT);

         try {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/bronze"));
            Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(tagKey);
            if (tag.isPresent() && tag.get().iterator().hasNext()) {
               bronzeIngot = new ItemStack((ItemLike)((Holder)tag.get().iterator().next()).value());
            }
         } catch (Exception var5) {
         }

         Item heShell = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("createbigcannons", "he_shell"));
         Item ironPlate = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", "iron_sheet"));
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.BLUE_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.BLUE_DYE,
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.POOP_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.BROWN_DYE,
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.NAZI_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  (Item)DorpMod.BANANA.get(),
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.CAPTAIN_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.CYAN_DYE,
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.SUOMI_FIELD_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.LIGHT_BLUE_DYE,
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.COMMONWEALTH_FIELD_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.GREEN_DYE,
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.REICH_FIELD_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.BLACK_DYE,
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.LIBERTE_CAP.get()),
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.RED_DYE,
                  Items.DIAMOND,
                  null,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.BOMB_VEST.get()),
                  heShell,
                  Items.DIAMOND,
                  heShell,
                  Items.DIAMOND,
                  Items.LEATHER_CHESTPLATE,
                  Items.DIAMOND,
                  null,
                  heShell,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.HEADSET.get()),
                  Items.DIAMOND,
                  null,
                  Items.DIAMOND,
                  Items.REDSTONE,
                  null,
                  Items.REDSTONE,
                  null,
                  Items.DIAMOND,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.WALKIE_TALKIE.get()),
                  null,
                  Items.REDSTONE,
                  null,
                  Items.IRON_INGOT,
                  Items.IRON_INGOT,
                  Items.IRON_INGOT,
                  Items.IRON_INGOT,
                  Items.LIGHTNING_ROD,
                  Items.IRON_INGOT
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.RADIO.get()),
                  null,
                  Items.LIGHTNING_ROD,
                  null,
                  Items.IRON_INGOT,
                  Items.DIAMOND,
                  Items.IRON_INGOT,
                  Items.REDSTONE,
                  Items.REDSTONE,
                  Items.REDSTONE
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.INTERCEPTOR.get()),
                  Items.LIGHTNING_ROD,
                  null,
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  Items.NETHERITE_INGOT,
                  Items.NETHERITE_INGOT,
                  Items.REDSTONE,
                  Items.DIAMOND,
                  Items.DIAMOND,
                  Items.REDSTONE
               )
            );
         this.recipes.add(this.createShapeless(new ItemStack((ItemLike)DorpMod.TRACKER.get()), ironPlate, ironPlate, ironPlate, ironPlate, Items.DIAMOND));
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.DIEGO_STOPWATCH.get()),
                  Items.NETHERITE_INGOT,
                  Items.NETHERITE_INGOT,
                  Items.NETHERITE_INGOT,
                  Items.NETHERITE_INGOT,
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  Items.NETHERITE_INGOT,
                  Items.NETHERITE_INGOT,
                  Items.NETHERITE_INGOT,
                  Items.NETHERITE_INGOT
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.HEAD_TORCH.get()),
                  null,
                  null,
                  null,
                  Items.LEATHER,
                  Items.LANTERN,
                  Items.LEATHER,
                  Items.LAPIS_LAZULI,
                  Items.LAPIS_LAZULI,
                  Items.LAPIS_LAZULI
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.CIGARETTE.get()), null, null, null, Items.DRIED_KELP, Items.PAPER, Items.GUNPOWDER, null, null, null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.OFFICER_WHISTLE.get()),
                  null,
                  Items.NAUTILUS_SHELL,
                  null,
                  null,
                  bronzeIngot.getItem(),
                  null,
                  null,
                  bronzeIngot.getItem(),
                  null
               )
            );
         this.recipes.add(this.createShapeless(new ItemStack((ItemLike)DorpMod.BANANA.get()), Items.APPLE, Items.SUGAR));
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.BANANA_BURGER.get()),
                  Items.BREAD,
                  null,
                  null,
                  (Item)DorpMod.BANANA.get(),
                  null,
                  null,
                  Items.BREAD,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.PAPER_BAG.get()), Items.PAPER, Items.PAPER, Items.PAPER, Items.PAPER, null, Items.PAPER, null, null, null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.CLOCK.get()),
                  null,
                  Items.DIAMOND,
                  null,
                  Items.GOLD_INGOT,
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  Items.GOLD_INGOT,
                  null,
                  Items.GOLD_INGOT,
                  null
               )
            );
         Item castIronIngot = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("createbigcannons", "cast_iron_ingot"));
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.INVENTORY_CHECKER.get()),
                  castIronIngot,
                  Items.DIAMOND,
                  castIronIngot,
                  castIronIngot,
                  Items.OBSERVER,
                  castIronIngot,
                  Items.DIAMOND,
                  castIronIngot,
                  Items.DIAMOND
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.CONCEALER_HEAD.get()),
                  Items.LEATHER,
                  Items.LEATHER,
                  Items.LEATHER,
                  Items.LEATHER,
                  Items.IRON_INGOT,
                  Items.LEATHER,
                  Items.LEATHER,
                  Items.LEATHER,
                  Items.LEATHER
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.IRON_RING.get()),
                  Items.IRON_NUGGET,
                  Items.IRON_NUGGET,
                  Items.IRON_NUGGET,
                  Items.IRON_NUGGET,
                  null,
                  Items.IRON_NUGGET,
                  Items.IRON_NUGGET,
                  Items.IRON_NUGGET,
                  Items.IRON_NUGGET
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.CRIMSON_THREAD.get()),
                  (Item)DorpMod.CRIMSON_KRYANITE.get(),
                  null,
                  null,
                  (Item)DorpMod.IRON_RING.get(),
                  null,
                  null,
                  Items.STRING,
                  null,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.FORBIDDEN_WORKBENCH.get()),
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  Items.NETHERITE_INGOT,
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  Items.OBSIDIAN,
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  Items.CRAFTING_TABLE,
                  (Item)DorpMod.CRIMSON_PLATE.get()
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.ANOMALOUS_SLEDGEHAMMER.get()),
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  (Item)DorpMod.CRIMSON_KRYANITE.get(),
                  (Item)DorpMod.CRIMSON_PLATE.get(),
                  null,
                  Items.STICK,
                  null,
                  null,
                  Items.STICK,
                  null
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.CIGAR.get()), null, Items.DRIED_KELP, null, null, Items.DRIED_KELP, null, null, Items.DRIED_KELP, null
               )
            );
         this.recipes.add(this.createShapeless(new ItemStack((ItemLike)DorpMod.DORP_GUIDE.get()), Items.BOOK, (Item)DorpMod.BANANA.get()));
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.LOCKED_CHEST_ITEM.get()),
                  Items.IRON_INGOT,
                  Items.OAK_PLANKS,
                  Items.IRON_INGOT,
                  Items.OAK_PLANKS,
                  Items.CHEST,
                  Items.OAK_PLANKS,
                  Items.IRON_INGOT,
                  Items.OAK_PLANKS,
                  Items.IRON_INGOT
               )
            );
         this.recipes
            .add(
               this.createShaped(
                  new ItemStack((ItemLike)DorpMod.BREACHER.get()),
                  null,
                  castIronIngot,
                  castIronIngot,
                  null,
                  Items.NETHERITE_INGOT,
                  Items.DIAMOND,
                  Items.NETHERITE_INGOT,
                  null,
                  castIronIngot
               )
            );
      }
   }

   private ForbiddenWorkbenchRecipeBookScreen.RecipeEntry createShaped(ItemStack output, Item... items) {
      ItemStack[] grid = new ItemStack[9];

      for (int i = 0; i < 9; i++) {
         grid[i] = i < items.length && items[i] != null ? new ItemStack(items[i]) : ItemStack.EMPTY;
      }

      return new ForbiddenWorkbenchRecipeBookScreen.RecipeEntry(output, grid, false);
   }

   private ForbiddenWorkbenchRecipeBookScreen.RecipeEntry createShapeless(ItemStack output, Item... items) {
      ItemStack[] grid = new ItemStack[9];
      Arrays.fill(grid, ItemStack.EMPTY);

      for (int i = 0; i < Math.min(9, items.length); i++) {
         grid[i] = items[i] != null ? new ItemStack(items[i]) : ItemStack.EMPTY;
      }

      return new ForbiddenWorkbenchRecipeBookScreen.RecipeEntry(output, grid, true);
   }

   protected void init() {
      super.init();
      this.initRecipes();
      int startX = (this.width - 220) / 2;
      int startY = (this.height - 170) / 2;
      this.prevButton = Button.builder(Component.literal("<"), btn -> {
         if (this.leftPageIndex > 0) {
            this.leftPageIndex--;
            this.updateButtonVisibility();
         }
      }).bounds(startX + 12, startY + 142, 16, 16).build();
      this.nextButton = Button.builder(Component.literal(">"), btn -> {
         if ((this.leftPageIndex + 1) * 5 < this.recipes.size()) {
            this.leftPageIndex++;
            this.updateButtonVisibility();
         }
      }).bounds(startX + 78, startY + 142, 16, 16).build();
      this.backButton = Button.builder(Component.literal("Close"), btn -> Minecraft.getInstance().setScreen(this.parentScreen))
         .bounds(startX + 120, startY + 142, 88, 16)
         .build();
      this.addRenderableWidget(this.prevButton);
      this.addRenderableWidget(this.nextButton);
      this.addRenderableWidget(this.backButton);
      this.updateButtonVisibility();
   }

   private void updateButtonVisibility() {
      this.prevButton.active = this.leftPageIndex > 0;
      this.nextButton.active = (this.leftPageIndex + 1) * 5 < this.recipes.size();
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      int startX = (this.width - 220) / 2;
      int startY = (this.height - 170) / 2;
      guiGraphics.fill(startX, startY, startX + 220, startY + 170, -3750202);
      guiGraphics.fill(startX, startY, startX + 220, startY + 1, -1);
      guiGraphics.fill(startX, startY, startX + 1, startY + 170, -1);
      guiGraphics.fill(startX + 219, startY, startX + 220, startY + 170, -11184811);
      guiGraphics.fill(startX, startY + 169, startX + 220, startY + 170, -11184811);

      for (Renderable renderable : this.renderables) {
         renderable.render(guiGraphics, mouseX, mouseY, partialTick);
      }

      this.drawLeftPage(guiGraphics, startX, startY, mouseX, mouseY);
      this.drawRightPage(guiGraphics, startX, startY, mouseX, mouseY);
      this.renderTooltips(guiGraphics, startX, startY, mouseX, mouseY);
   }

   private void drawLeftPage(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, Component.literal("Crafting Guide"), startX + 12, startY + 10, -12566464, false);
      String pageStr = this.leftPageIndex + 1 + "/" + (this.recipes.size() + 4) / 5;
      int pWidth = this.font.width(pageStr);
      guiGraphics.drawString(this.font, pageStr, startX + 53 - pWidth / 2, startY + 146, -12566464, false);
      int startIdx = this.leftPageIndex * 5;

      for (int i = 0; i < 5; i++) {
         int recipeIdx = startIdx + i;
         if (recipeIdx >= this.recipes.size()) {
            break;
         }

         ForbiddenWorkbenchRecipeBookScreen.RecipeEntry entry = this.recipes.get(recipeIdx);
         int itemY = startY + 24 + i * 23;
         boolean isHovered = mouseX >= startX + 10 && mouseX <= startX + 100 && mouseY >= itemY - 2 && mouseY < itemY + 18;
         if (isHovered || this.selectedRecipe == entry) {
            guiGraphics.fill(startX + 10, itemY - 2, startX + 100, itemY + 18, isHovered ? 1090519039 : 1073741824);
         }

         guiGraphics.renderFakeItem(entry.output, startX + 12, itemY);
         String displayName = this.getShortName(entry.output.getHoverName().getString());
         guiGraphics.drawString(this.font, displayName, startX + 32, itemY + 4, -12566464, false);
      }
   }

   private void drawRightPage(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
      if (this.selectedRecipe == null) {
         int textY = startY + 30;
         guiGraphics.drawString(this.font, "Crafting Guide", startX + 118, textY, -12566464, false);
         guiGraphics.drawString(this.font, "Select an item", startX + 120, textY + 20, -11513776, false);
         guiGraphics.drawString(this.font, "on the left page", startX + 120, textY + 32, -11513776, false);
         guiGraphics.drawString(this.font, "to see how to", startX + 120, textY + 44, -11513776, false);
         guiGraphics.drawString(this.font, "craft it here.", startX + 120, textY + 56, -11513776, false);
      } else {
         ItemStack output = this.selectedRecipe.output;
         String title = output.getHoverName().getString();
         int tWidth = this.font.width(title);
         if (tWidth > 90) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(startX + 164, startY + 10, 0.0F);
            guiGraphics.pose().scale(0.8F, 0.8F, 1.0F);
            this.drawCenteredStringNoShadow(guiGraphics, title, 0, 0, -12566464);
            guiGraphics.pose().popPose();
         } else {
            this.drawCenteredStringNoShadow(guiGraphics, title, startX + 164, startY + 10, -12566464);
         }

         int gridX = startX + 137;
         int gridY = startY + 24;

         for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
               int slotX = gridX + c * 18;
               int slotY = gridY + r * 18;
               guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, -7631989);
               guiGraphics.fill(slotX, slotY, slotX + 15, slotY + 1, -13158601);
               guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 15, -13158601);
               guiGraphics.fill(slotX + 15, slotY + 1, slotX + 16, slotY + 16, -1);
               guiGraphics.fill(slotX + 1, slotY + 15, slotX + 16, slotY + 16, -1);
               guiGraphics.fill(slotX + 1, slotY + 1, slotX + 15, slotY + 15, -7631989);
               ItemStack ing = this.selectedRecipe.grid[r * 3 + c];
               if (ing != null && !ing.isEmpty()) {
                  guiGraphics.renderFakeItem(ing, slotX, slotY);
               }
            }
         }

         this.drawCenteredStringNoShadow(guiGraphics, "↓", startX + 164, startY + 80, -11513776);
         int outX = startX + 156;
         int outY = startY + 92;
         guiGraphics.fill(outX, outY, outX + 16, outY + 16, -7631989);
         guiGraphics.fill(outX, outY, outX + 15, outY + 1, -13158601);
         guiGraphics.fill(outX, outY, outX + 1, outY + 15, -13158601);
         guiGraphics.fill(outX + 15, outY + 1, outX + 16, outY + 16, -1);
         guiGraphics.fill(outX + 1, outY + 15, outX + 16, outY + 16, -1);
         guiGraphics.fill(outX + 1, outY + 1, outX + 15, outY + 15, -3750202);
         guiGraphics.renderFakeItem(output, outX, outY);
         boolean isForbiddenOnly = this.isForbiddenOnly(output.getItem());
         if (isForbiddenOnly) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(startX + 164, startY + 114, 0.0F);
            guiGraphics.pose().scale(0.7F, 0.7F, 1.0F);
            this.drawCenteredStringNoShadow(guiGraphics, "Forbidden Workbench", 0, 0, -8388608);
            this.drawCenteredStringNoShadow(guiGraphics, "Only", 0, 10, -8388608);
            guiGraphics.pose().popPose();
         } else {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(startX + 164, startY + 114, 0.0F);
            guiGraphics.pose().scale(0.7F, 0.7F, 1.0F);
            this.drawCenteredStringNoShadow(guiGraphics, "Standard Crafting", 0, 0, -11513776);
            this.drawCenteredStringNoShadow(guiGraphics, "Table", 0, 10, -11513776);
            guiGraphics.pose().popPose();
         }
      }
   }

   private void renderTooltips(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
      int startIdx = this.leftPageIndex * 5;

      for (int i = 0; i < 5; i++) {
         int recipeIdx = startIdx + i;
         if (recipeIdx >= this.recipes.size()) {
            break;
         }

         int itemY = startY + 24 + i * 23;
         if (mouseX >= startX + 10 && mouseX <= startX + 100 && mouseY >= itemY - 2 && mouseY < itemY + 18) {
            ForbiddenWorkbenchRecipeBookScreen.RecipeEntry entry = this.recipes.get(recipeIdx);
            guiGraphics.renderTooltip(this.font, entry.output, mouseX, mouseY);
            return;
         }
      }

      if (this.selectedRecipe != null) {
         int gridX = startX + 137;
         int gridY = startY + 24;

         for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
               int slotX = gridX + c * 18;
               int slotY = gridY + r * 18;
               if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                  ItemStack ing = this.selectedRecipe.grid[r * 3 + c];
                  if (ing != null && !ing.isEmpty()) {
                     guiGraphics.renderTooltip(this.font, ing, mouseX, mouseY);
                     return;
                  }
               }
            }
         }

         int outX = startX + 156;
         int outY = startY + 92;
         if (mouseX >= outX && mouseX < outX + 16 && mouseY >= outY && mouseY < outY + 16) {
            guiGraphics.renderTooltip(this.font, this.selectedRecipe.output, mouseX, mouseY);
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int startX = (this.width - 220) / 2;
      int startY = (this.height - 170) / 2;
      if (mouseX >= startX + 10 && mouseX <= startX + 100) {
         int idx = (int)((mouseY - (startY + 22)) / 23.0);
         if (idx >= 0 && idx < 5) {
            int recipeIdx = this.leftPageIndex * 5 + idx;
            if (recipeIdx < this.recipes.size()) {
               this.selectedRecipe = this.recipes.get(recipeIdx);
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               return true;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private String getShortName(String fullName) {
      if (fullName.equals("Liberty Cap")) {
         return "Liberty Cap";
      } else if (fullName.equals("Imperial Cap")) {
         return "Imperial Cap";
      } else if (fullName.equals("Reich Cap")) {
         return "Reich Cap";
      } else if (fullName.equals("Captain Cap")) {
         return "Captain Cap";
      } else if (fullName.equals("Suomi Field Cap")) {
         return "Suomi Cap";
      } else if (fullName.equals("Common Wealth Cap")) {
         return "CW Cap";
      } else if (fullName.equals("Reich Field Cap")) {
         return "Reich F. Cap";
      } else if (fullName.equals("Liberte Cap")) {
         return "Liberte Cap";
      } else if (fullName.equals("Walkie Talkie")) {
         return "WalkieTalkie";
      } else if (fullName.equals("Radio/Transmitter")) {
         return "Radio";
      } else if (fullName.equals("Diego's Stopwatch")) {
         return "Stopwatch";
      } else if (fullName.equals("Forbidden Workbench")) {
         return "Workbench";
      } else if (fullName.equals("Anomalous Sledgehammer")) {
         return "Sledgehammer";
      } else {
         return fullName.length() > 10 ? fullName.substring(0, 9) + "." : fullName;
      }
   }

   private boolean isForbiddenOnly(Item item) {
      return DorpMod.isForbiddenOnly(item);
   }

   private void drawCenteredStringNoShadow(GuiGraphics guiGraphics, String text, int x, int y, int color) {
      int width = this.font.width(text);
      guiGraphics.drawString(this.font, text, x - width / 2, y, color, false);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private record RecipeEntry(ItemStack output, ItemStack[] grid, boolean shapeless) {
   }
}
