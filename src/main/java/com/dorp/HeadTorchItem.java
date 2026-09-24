package com.dorp;

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

public class HeadTorchItem extends Item implements ICurioItem {
   public static final int MAX_ENERGY = 7200;

   public HeadTorchItem(Properties properties) {
      super(properties);
   }

   public static boolean isActive(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return customData == null ? false : customData.copyTag().getBoolean("active") && getEnergy(stack) > 0;
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

      tag.putInt("Energy", Math.max(0, Math.min(7200, energy)));
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   public int getBarWidth(ItemStack stack) {
      return Math.round(13.0F * getEnergy(stack) / 7200.0F);
   }

   public int getBarColor(ItemStack stack) {
      return 16755200;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      return InteractionResultHolder.pass(stack);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      boolean active = isActive(stack);
      int energy = getEnergy(stack);
      Component statusComponent = active
         ? Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("ON").withStyle(ChatFormatting.GREEN))
         : Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("OFF").withStyle(ChatFormatting.RED));
      tooltipComponents.add(
         Component.literal("A standard issue military head torch used for night patrols.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("Illuminates dark trenches without compromising weapon readiness.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(statusComponent);
      tooltipComponents.add(
         Component.literal("Energy: ").withStyle(ChatFormatting.GRAY).append(Component.literal(energy + " / 7200 FE").withStyle(ChatFormatting.GOLD))
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Wear as Curios Head Wearable").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      tooltipComponents.add(
         Component.literal("Press [K] while wearing to toggle light").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Niko").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public static void handleTogglePayload(ServerPlayer player) {
      Optional<SlotResult> slotResultOpt = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEAD_TORCH.get()));
      if (!slotResultOpt.isEmpty()) {
         ItemStack stack = slotResultOpt.get().stack();
         boolean active = isActive(stack);
         if (!active) {
            if (getEnergy(stack) <= 0) {
               player.displayClientMessage(Component.literal("Head Torch has no energy! Charge it first.").withStyle(ChatFormatting.RED), true);
               return;
            }

            setActive(stack, true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)DorpMod.FLASHON.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("Head Torch light enabled").withStyle(ChatFormatting.GOLD), true);
         } else {
            setActive(stack, false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)DorpMod.FLASHOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("Head Torch light disabled").withStyle(ChatFormatting.RED), true);
         }
      }
   }
}
