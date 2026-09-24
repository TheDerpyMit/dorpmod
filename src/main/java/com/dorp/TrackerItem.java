package com.dorp;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class TrackerItem extends Item implements ICurioItem {
   public TrackerItem(Properties properties) {
      super(properties);
   }

   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
      if (player.isShiftKeyDown()) {
         if (player.level().isClientSide()) {
            return InteractionResult.CONSUME;
         }

         if (isBricked(stack)) {
            player.displayClientMessage(Component.literal("This tracker is bricked and cannot be used.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
         }

         int trackerId = 100 + player.level().random.nextInt(900);
         if (!(interactionTarget instanceof Player)) {
            CompoundTag targetPersist = interactionTarget.getPersistentData();
            if (targetPersist.contains("MobTrackerID") && !targetPersist.getBoolean("MobTrackerBricked")) {
               String targetName = interactionTarget.hasCustomName() ? interactionTarget.getCustomName().getString() : interactionTarget.getName().getString();
               player.displayClientMessage(Component.literal(targetName + " is already tracked!").withStyle(ChatFormatting.RED), true);
               return InteractionResult.FAIL;
            }

            targetPersist.putInt("MobTrackerID", trackerId);
            targetPersist.putBoolean("MobTrackerBricked", false);
            player.level().playSound(null, player.blockPosition(), (SoundEvent)SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            String targetName = interactionTarget.hasCustomName() ? interactionTarget.getCustomName().getString() : interactionTarget.getName().getString();
            ItemStack viewer = new ItemStack((ItemLike)DorpMod.TRACKER_VIEWER.get());
            CompoundTag tag = new CompoundTag();
            tag.putUUID("TrackedPlayerUUID", interactionTarget.getUUID());
            tag.putString("TrackedPlayerName", targetName);
            tag.putInt("TrackerID", trackerId);
            tag.putBoolean("IsMob", true);
            viewer.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            if (!player.getInventory().add(viewer)) {
               player.drop(viewer, false);
            }

            stack.shrink(1);
            player.displayClientMessage(
               Component.literal("Tracker [ID: " + trackerId + "] placed on " + targetName + "!").withStyle(ChatFormatting.GREEN), true
            );
            return InteractionResult.SUCCESS;
         }

         Optional<ICuriosItemHandler> curioInvOpt = CuriosApi.getCuriosInventory(interactionTarget);
         if (curioInvOpt.isPresent()) {
            ICuriosItemHandler handler = curioInvOpt.get();
            Optional<ICurioStacksHandler> stacksHandlerOpt = handler.getStacksHandler("back");
            if (stacksHandlerOpt.isPresent()) {
               ICurioStacksHandler stacksHandler = stacksHandlerOpt.get();
               IDynamicStackHandler inventory = stacksHandler.getStacks();
               boolean placed = false;

               for (int i = 0; i < inventory.getSlots(); i++) {
                  if (inventory.getStackInSlot(i).isEmpty()) {
                     ItemStack trackerCopy = stack.copy();
                     trackerCopy.setCount(1);
                     CustomData data = (CustomData)trackerCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                     CompoundTag tag = data.copyTag();
                     tag.putInt("TrackerID", trackerId);
                     trackerCopy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                     inventory.setStackInSlot(i, trackerCopy);
                     placed = true;
                     break;
                  }
               }

               if (placed) {
                  player.level().playSound(null, player.blockPosition(), (SoundEvent)SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                  String targetName = interactionTarget.hasCustomName()
                     ? interactionTarget.getCustomName().getString()
                     : interactionTarget.getName().getString();
                  ItemStack viewer = new ItemStack((ItemLike)DorpMod.TRACKER_VIEWER.get());
                  CompoundTag tag = new CompoundTag();
                  tag.putUUID("TrackedPlayerUUID", interactionTarget.getUUID());
                  tag.putString("TrackedPlayerName", targetName);
                  tag.putInt("TrackerID", trackerId);
                  viewer.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                  if (!player.getInventory().add(viewer)) {
                     player.drop(viewer, false);
                  }

                  stack.shrink(1);
                  player.displayClientMessage(
                     Component.literal("Tracker [ID: " + trackerId + "] placed on " + targetName + "!").withStyle(ChatFormatting.GREEN), true
                  );
                  return InteractionResult.SUCCESS;
               }

               String targetName = interactionTarget.hasCustomName() ? interactionTarget.getCustomName().getString() : interactionTarget.getName().getString();
               player.displayClientMessage(Component.literal(targetName + " has no empty back slot!").withStyle(ChatFormatting.RED), true);
               return InteractionResult.FAIL;
            }
         }
      }

      return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
      ItemStack stack = player.getItemInHand(usedHand);
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      boolean hasId = data != null && data.copyTag().contains("TrackerID");
      if (!level.isClientSide() && !isBricked(stack) && hasId) {
         setBricked(stack, true);
         level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
         player.displayClientMessage(Component.literal("You have bricked the tracker.").withStyle(ChatFormatting.RED), true);
         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      boolean hasId = data != null && data.copyTag().contains("TrackerID");
      tooltipComponents.add(
         Component.literal("Used to track any player or entity without them knowing.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("Unless they catch on and brick your tracker...").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      if (isBricked(stack)) {
         tooltipComponents.add(Component.literal("Bricked Tracker").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}));
         tooltipComponents.add(Component.literal("This tracker has been rendered useless.").withStyle(ChatFormatting.GRAY));
      } else if (hasId) {
         int id = data.copyTag().getInt("TrackerID");
         tooltipComponents.add(Component.literal("Linked ID: " + id).withStyle(ChatFormatting.YELLOW));
         tooltipComponents.add(Component.literal("Right-click to brick.").withStyle(ChatFormatting.DARK_GRAY));
      } else {
         tooltipComponents.add(Component.literal("Shift + Right-Click an entity to track them.").withStyle(ChatFormatting.GRAY));
      }

      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public Component getName(ItemStack stack) {
      return (Component)(isBricked(stack) ? Component.literal("Bricked Tracker").withStyle(ChatFormatting.RED) : super.getName(stack));
   }

   public static boolean isBricked(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? false : data.copyTag().getBoolean("bricked");
   }

   public static void setBricked(ItemStack stack, boolean bricked) {
      CompoundTag tag = new CompoundTag();
      CustomData existing = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (existing != null) {
         tag = existing.copyTag();
      }

      tag.putBoolean("bricked", bricked);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }
}
