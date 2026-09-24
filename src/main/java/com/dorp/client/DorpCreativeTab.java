package com.dorp.client;

import com.dorp.DorpMod;
import com.dorp.mixin.AbstractContainerScreenAccessor;
import com.dorp.mixin.CreativeModeInventoryScreenAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class DorpCreativeTab {
   public static int CURRENT_ROW = 0;
   public static int TOTAL_ROWS = 0;
   public static final Object2IntOpenHashMap<String> SECTION_BANNER_ROW = new Object2IntOpenHashMap();
   public static final ResourceLocation COMM_BANNER = ResourceLocation.fromNamespaceAndPath("dorp", "banner/communication");
   public static final ResourceLocation HATS_BANNER = ResourceLocation.fromNamespaceAndPath("dorp", "banner/hats");
   public static final ResourceLocation CRIM_BANNER = ResourceLocation.fromNamespaceAndPath("dorp", "banner/crimson");
   public static final ResourceLocation MISC_BANNER = ResourceLocation.fromNamespaceAndPath("dorp", "banner/misc");
   public static final ResourceLocation DEBUG_BANNER = ResourceLocation.fromNamespaceAndPath("dorp", "banner/debug");
   public static final List<DorpCreativeTab.Section> SECTIONS = new ArrayList<>();
   private static final int VISIBLE_ROWS = 5;
   private static final int SLOT_SIZE = 18;
   private static final int BANNER_WIDTH = 162;
   private static final int BANNER_HEIGHT = 18;

   public static void populateItems() {
      for (DorpCreativeTab.Section s : SECTIONS) {
         s.items.clear();
      }

      getSection("communication")
         .items
         .addAll(
            List.of(
               new ItemStack((ItemLike)DorpMod.WALKIE_TALKIE.get()),
               new ItemStack((ItemLike)DorpMod.HEADSET.get()),
               new ItemStack((ItemLike)DorpMod.RADIO.get()),
               new ItemStack((ItemLike)DorpMod.INTERCEPTOR.get()),
               new ItemStack((ItemLike)DorpMod.TRACKER.get()),
               new ItemStack((ItemLike)DorpMod.TRACKER_VIEWER.get()),
               new ItemStack((ItemLike)DorpMod.OFFICER_WHISTLE.get())
            )
         );
      getSection("hats")
         .items
         .addAll(
            List.of(
               new ItemStack((ItemLike)DorpMod.PAPER_BAG.get()),
               new ItemStack((ItemLike)DorpMod.CONCEALER_HEAD.get()),
               new ItemStack((ItemLike)DorpMod.BLUE_CAP.get()),
               new ItemStack((ItemLike)DorpMod.POOP_CAP.get()),
               new ItemStack((ItemLike)DorpMod.NAZI_CAP.get()),
               new ItemStack((ItemLike)DorpMod.CAPTAIN_CAP.get()),
               new ItemStack((ItemLike)DorpMod.SUOMI_FIELD_CAP.get()),
               new ItemStack((ItemLike)DorpMod.COMMONWEALTH_FIELD_CAP.get()),
               new ItemStack((ItemLike)DorpMod.REICH_FIELD_CAP.get()),
               new ItemStack((ItemLike)DorpMod.LIBERTE_CAP.get()),
               new ItemStack((ItemLike)DorpMod.WEHRMACHT_OFFICER_CAP.get()),
               new ItemStack((ItemLike)DorpMod.SUNLIGHT_HELMET.get()),
               new ItemStack((ItemLike)DorpMod.HEAD_TORCH.get()),
               new ItemStack((ItemLike)DorpMod.HIMARI_VISOR.get())
            )
         );
      getSection("crimson")
         .items
         .addAll(
            List.of(
               new ItemStack((ItemLike)DorpMod.CRIMSON_KRYANITE.get()),
               new ItemStack((ItemLike)DorpMod.CRIMSON_PLATE.get()),
               new ItemStack((ItemLike)DorpMod.IRON_RING.get()),
               new ItemStack((ItemLike)DorpMod.CRIMSON_THREAD.get()),
               new ItemStack((ItemLike)DorpMod.ANOMALOUS_SLEDGEHAMMER.get()),
               new ItemStack((ItemLike)DorpMod.FORBIDDEN_WORKBENCH.get()),
               new ItemStack((ItemLike)DorpMod.DIEGO_STOPWATCH.get())
            )
         );
      getSection("misc")
         .items
         .addAll(
            List.of(
               new ItemStack((ItemLike)DorpMod.LOCKED_CHEST_ITEM.get()),
               new ItemStack((ItemLike)DorpMod.BREACHER.get()),
               new ItemStack((ItemLike)DorpMod.BANANA_BURGER.get()),
               new ItemStack((ItemLike)DorpMod.BANANA.get()),
               new ItemStack((ItemLike)DorpMod.BOMB_VEST.get()),
               new ItemStack((ItemLike)DorpMod.CLOCK.get()),
               new ItemStack((ItemLike)DorpMod.TEST_PLAYER.get()),
               new ItemStack((ItemLike)DorpMod.CIGARETTE.get()),
               new ItemStack((ItemLike)DorpMod.CIGAR.get()),
               new ItemStack((ItemLike)DorpMod.INVENTORY_CHECKER.get())
            )
         );
      getSection("debug")
         .items
         .addAll(
            List.of(
               new ItemStack((ItemLike)DorpMod.DEBUG_LOCKED_CHEST_ITEM.get()),
               new ItemStack((ItemLike)DorpMod.DEBUG_RADIO.get()),
               new ItemStack((ItemLike)DorpMod.DEBUG_INTERCEPTOR.get()),
               new ItemStack((ItemLike)DorpMod.DORP_GUIDE.get())
            )
         );
   }

   private static DorpCreativeTab.Section getSection(String name) {
      for (DorpCreativeTab.Section s : SECTIONS) {
         if (s.name.equals(name)) {
            return s;
         }
      }

      throw new IllegalArgumentException("Unknown section: " + name);
   }

   public static void processItems(Consumer<ItemStack> displayConsumer, Consumer<ItemStack> searchConsumer) {
      populateItems();
      SECTION_BANNER_ROW.clear();
      int gridRow = 0;

      for (int i = 0; i < SECTIONS.size(); i++) {
         DorpCreativeTab.Section s = SECTIONS.get(i);
         SECTION_BANNER_ROW.put(s.name, gridRow);

         for (int k = 0; k < 9; k++) {
            displayConsumer.accept(ItemStack.EMPTY);
         }

         gridRow++;

         for (ItemStack stack : s.items) {
            displayConsumer.accept(stack);
            searchConsumer.accept(stack);
         }

         int itemCount = s.items.size();
         int itemRows = (itemCount + 8) / 9;
         if (itemRows == 0) {
            itemRows = 1;
         }

         gridRow += itemRows;
         if (i < SECTIONS.size() - 1) {
            int remainder = itemCount % 9;
            int pad = remainder == 0 ? 0 : 9 - remainder;

            for (int p = 0; p < pad; p++) {
               displayConsumer.accept(ItemStack.EMPTY);
            }
         }
      }

      TOTAL_ROWS = gridRow;
   }

   public static String getItemCategory(ItemStack stack) {
      for (DorpCreativeTab.Section s : SECTIONS) {
         for (ItemStack is : s.items) {
            if (is.getItem() == stack.getItem()) {
               return s.name;
            }
         }
      }

      return null;
   }

   public static void renderBanners(CreativeModeInventoryScreen screen, GuiGraphics graphics, int mouseX, int mouseY) {
      AbstractContainerScreenAccessor containerAcc = (AbstractContainerScreenAccessor)screen;
      CreativeModeInventoryScreenAccessor screenAcc = (CreativeModeInventoryScreenAccessor)screen;
      int leftPos = containerAcc.getLeftPos() + 8;
      int topPos = containerAcc.getTopPos() + 17;
      float scrollOffs = screenAcc.getScrollOffs();
      int maxScroll = Math.max(0, TOTAL_ROWS - 5);
      CURRENT_ROW = (int)(scrollOffs * maxScroll + 0.5F);
      Font font = Minecraft.getInstance().font;
      RenderSystem.disableDepthTest();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(leftPos, topPos, 200.0F);

      for (DorpCreativeTab.Section s : SECTIONS) {
         int bannerRow = SECTION_BANNER_ROW.getInt(s.name);
         int relativeRow = bannerRow - CURRENT_ROW;
         if (relativeRow >= 0 && relativeRow < 5) {
            int yOffset = relativeRow * 18;
            graphics.blitSprite(s.sprite, 0, yOffset, 162, 18);
            int textWidth = font.width(s.title);
            graphics.fill(2, yOffset + 1, textWidth + 10, yOffset + 18 - 1, s.backgroundColor & -855638017);
            drawOutlinedText(graphics, s.title, s.textColor, s.outlineColor, 6, yOffset + 5);
         }
      }

      pose.popPose();
      RenderSystem.enableDepthTest();
   }

   public static void drawOutlinedText(GuiGraphics graphics, Component text, int color, int outlineColor, int x, int y) {
      Font font = Minecraft.getInstance().font;
      graphics.drawString(font, text, x + 1, y + 1, outlineColor, false);
      graphics.drawString(font, text, x, y, color, false);
   }

   static {
      SECTIONS.add(new DorpCreativeTab.Section("communication", "dorp.creative_tab.communication", COMM_BANNER, -15788246, -1906448, -13058568));
      SECTIONS.add(new DorpCreativeTab.Section("hats", "dorp.creative_tab.hats", HATS_BANNER, -12248573, -68665, -680437));
      SECTIONS.add(new DorpCreativeTab.Section("crimson", "dorp.creative_tab.crimson", CRIM_BANNER, -13826554, -72990, -1096636));
      SECTIONS.add(new DorpCreativeTab.Section("misc", "dorp.creative_tab.misc", MISC_BANNER, -14735049, -789258, -6511697));
      SECTIONS.add(new DorpCreativeTab.Section("debug", "dorp.creative_tab.debug", DEBUG_BANNER, -16777216, -16711936, -16755456));
   }

   public static class Section {
      public final String name;
      public final Component title;
      public final ResourceLocation sprite;
      public final int backgroundColor;
      public final int textColor;
      public final int outlineColor;
      public final List<ItemStack> items = new ArrayList<>();

      public Section(String name, String translateKey, ResourceLocation sprite, int backgroundColor, int textColor, int outlineColor) {
         this.name = name;
         this.title = Component.translatable(translateKey);
         this.sprite = sprite;
         this.backgroundColor = backgroundColor;
         this.textColor = textColor;
         this.outlineColor = outlineColor;
      }
   }
}
