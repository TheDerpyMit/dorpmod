package com.dorp;

import com.dorp.config.DorpConfig;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class HeadsetItem extends Item implements ICurioItem {
   public HeadsetItem(Properties properties) {
      super(properties);
   }

   public static boolean isActive(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return customData == null ? false : customData.copyTag().getBoolean("active") && getEnergy(stack) >= 100;
   }

   public static void setActive(ItemStack stack, boolean active) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      tag.putBoolean("active", active);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static int getFrequency(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         CompoundTag tag = customData.copyTag();
         if (tag.contains("frequency")) {
            return tag.getInt("frequency");
         }
      }

      return 1;
   }

   public static void setFrequency(ItemStack stack, int frequency) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      tag.putInt("frequency", Math.max(1, Math.min(5000, frequency)));
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static int getEnergy(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         CompoundTag tag = customData.copyTag();
         if (tag.contains("Energy")) {
            return tag.getInt("Energy");
         }
      }

      return 0;
   }

   public static void setEnergy(ItemStack stack, int energy) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      int maxEnergy = DorpConfig.HEADSET_MAX_ENERGY.get();
      tag.putInt("Energy", Math.max(0, Math.min(maxEnergy, energy)));
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   public int getBarWidth(ItemStack stack) {
      float maxEnergy = DorpConfig.HEADSET_MAX_ENERGY.get();
      return Math.round(13.0F * getEnergy(stack) / maxEnergy);
   }

   public int getBarColor(ItemStack stack) {
      return 65535;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      return InteractionResultHolder.pass(stack);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      boolean active = isActive(stack);
      int freq = getFrequency(stack);
      int energy = getEnergy(stack);
      Component statusComponent = active
         ? Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("ON").withStyle(ChatFormatting.GREEN))
         : Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("OFF").withStyle(ChatFormatting.RED));
      tooltipComponents.add(
         Component.literal("A tactical communications headset designed for field operations.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("Used by scouts to transmit coordinates and intelligence across battlefields.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(statusComponent);
      tooltipComponents.add(
         Component.literal("Frequency: ").withStyle(ChatFormatting.GRAY).append(Component.literal(String.valueOf(freq)).withStyle(ChatFormatting.AQUA))
      );
      int maxEnergy = DorpConfig.HEADSET_MAX_ENERGY.get();
      tooltipComponents.add(
         Component.literal("Energy: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(energy + " / " + maxEnergy + " FE").withStyle(ChatFormatting.GOLD))
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Range: ").withStyle(ChatFormatting.GRAY).append(Component.literal("630 Blocks").withStyle(ChatFormatting.AQUA)));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Wear as Curios Neck Wearable").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      tooltipComponents.add(
         Component.literal("Press [K] while wearing to change settings").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Niko").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public static void handleUpdatePayload(ServerPlayer player, int frequency, boolean toggleActive) {
      Optional<SlotResult> slotResultOpt = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEADSET.get()));
      if (!slotResultOpt.isEmpty()) {
         ItemStack stack = slotResultOpt.get().stack();
         boolean active = isActive(stack);
         if (toggleActive) {
            if (getEnergy(stack) < 100) {
               player.displayClientMessage(Component.literal("Headset has no energy! Charge it first.").withStyle(ChatFormatting.RED), true);
               return;
            }

            active = !active;
            setActive(stack, active);
            SoundEvent sound = active ? (SoundEvent)DorpMod.WALKIETALKIE_ON.get() : (SoundEvent)DorpMod.WALKIETALKIE_OFF.get();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
         }

         if (frequency >= 1 && frequency <= 5000) {
            setFrequency(stack, frequency);
         }

         int freq = getFrequency(stack);
         String translationKey = active ? "actionbar.dorp.headset.on" : "actionbar.dorp.headset.off";
         Component msg = active ? Component.translatable(translationKey, new Object[]{freq}) : Component.translatable(translationKey);
         player.displayClientMessage(msg, true);
      }
   }
}
