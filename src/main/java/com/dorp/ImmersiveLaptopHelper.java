package com.dorp;

import dev.leveloper.immersivecomputing.device.DeviceBlueprintReader;
import dev.leveloper.immersivecomputing.device.DeviceManifest;
import dev.leveloper.immersivecomputing.littletiles.LittleTilesDeviceItemData;
import dev.leveloper.immersivecomputing.registry.BuiltInBlueprints;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import team.creative.littletiles.api.common.tool.ILittleTool;

public class ImmersiveLaptopHelper {
   private static final String CUSTOM_TOOLTIP = "Ultralight laptop. Powered by LevelOS Revanced. Dorp Computers & Co.";
   private static ItemStack CACHED_LAPTOP = null;

   public static ItemStack createLaptopStack() {
      if (CACHED_LAPTOP != null && !CACHED_LAPTOP.isEmpty()) {
         return CACHED_LAPTOP.copy();
      } else {
         try {
            List<ItemStack> tabItems = BuiltInBlueprints.creativeTabItems();
            if (tabItems != null) {
               for (ItemStack stack : tabItems) {
                  if (stack != null && !stack.isEmpty()) {
                     Component name = (Component)stack.get(DataComponents.CUSTOM_NAME);
                     if (name != null && name.getString().equalsIgnoreCase("Laptop")) {
                        ItemStack copy = stack.copy();
                        applyCustomTooltip(copy);
                        CACHED_LAPTOP = copy;
                        return CACHED_LAPTOP.copy();
                     }
                  }
               }
            }
         } catch (Throwable var14) {
         }

         try {
            Item blueprintItem = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("littletiles", "blueprint"));
            Item deviceItem = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersivecomputing", "device"));
            if (blueprintItem == Items.AIR || deviceItem == Items.AIR) {
               return ItemStack.EMPTY;
            }

            String snbt = null;

            try (InputStream stream = ImmersiveLaptopHelper.class.getResourceAsStream("/assets/immersivecomputing/device_blueprints/laptop.snbt")) {
               if (stream != null) {
                  snbt = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
               }
            }

            if (snbt == null || snbt.isEmpty()) {
               try (InputStream streamx = ImmersiveLaptopHelper.class.getResourceAsStream("/data/dorp/laptop.snbt")) {
                  if (streamx != null) {
                     snbt = new String(streamx.readAllBytes(), StandardCharsets.UTF_8);
                  }
               }
            }

            if (snbt == null || snbt.isEmpty()) {
               return ItemStack.EMPTY;
            }

            CompoundTag snbtTag = TagParser.parseTag(snbt);
            ItemStack blueprintStack = new ItemStack(blueprintItem);
            CompoundTag toolData = ILittleTool.getData(blueprintStack);
            if (toolData == null) {
               toolData = new CompoundTag();
            }

            toolData.put("c", snbtTag);
            ILittleTool.setData(blueprintStack, toolData);
            blueprintStack.set(DataComponents.CUSTOM_NAME, Component.literal("Laptop").withStyle(style -> style.withItalic(false)));
            DeviceManifest manifest = DeviceBlueprintReader.read(blueprintStack);
            ItemStack manufacturedDevice = new ItemStack(deviceItem);
            ItemStack resultStack = LittleTilesDeviceItemData.manufactureFromBlueprint(manufacturedDevice, 0, manifest, blueprintStack);
            if (resultStack != null && !resultStack.isEmpty()) {
               applyCustomTooltip(resultStack);
               CACHED_LAPTOP = resultStack.copy();
               return CACHED_LAPTOP.copy();
            }
         } catch (Throwable var13) {
            var13.printStackTrace();
         }

         return ItemStack.EMPTY;
      }
   }

   private static void applyCustomTooltip(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         CompoundTag tag = customData.copyTag();
         if (tag.contains("c")) {
            CompoundTag c = tag.getCompound("c");
            c.putString("ic_tooltip", "Ultralight laptop. Powered by LevelOS Revanced. Dorp Computers & Co.");
         } else {
            tag.putString("ic_tooltip", "Ultralight laptop. Powered by LevelOS Revanced. Dorp Computers & Co.");
         }

         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      }
   }
}
