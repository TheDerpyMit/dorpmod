package com.dorp;

import com.dorp.config.DorpConfig;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class WalkieTalkieItem extends Item {
   public WalkieTalkieItem(Properties properties) {
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
      updateEnergyAndSos(stack, energy, DorpConfig.WALKIE_TALKIE_MAX_ENERGY.get());
   }

   public static void updateEnergyAndSos(ItemStack stack, int newEnergy, int maxCapacity) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      int clampedNewEnergy = Math.max(0, Math.min(maxCapacity, newEnergy));
      tag.putInt("Energy", clampedNewEnergy);
      if (clampedNewEnergy == maxCapacity) {
         tag.putInt("sos_uses", DorpConfig.SOS_MAX_USES.get());
      }

      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static int getSosUses(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         CompoundTag tag = customData.copyTag();
         if (tag.contains("sos_uses")) {
            return tag.getInt("sos_uses");
         }
      }

      return DorpConfig.SOS_MAX_USES.get();
   }

   public static void setSosUses(ItemStack stack, int uses) {
      CompoundTag tag = new CompoundTag();
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         tag = customData.copyTag();
      }

      tag.putInt("sos_uses", Math.max(0, uses));
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   public int getBarWidth(ItemStack stack) {
      float maxEnergy = DorpConfig.WALKIE_TALKIE_MAX_ENERGY.get();
      return Math.round(13.0F * getEnergy(stack) / maxEnergy);
   }

   public int getBarColor(ItemStack stack) {
      return 65535;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         if (!level.isClientSide()) {
            if (getEnergy(stack) < 100) {
               player.displayClientMessage(Component.literal("Walkie Talkie has no energy! Charge it first.").withStyle(ChatFormatting.RED), true);
               return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }

            boolean nextState = !isActive(stack);
            setActive(stack, nextState);
            int freq = getFrequency(stack);
            SoundEvent sound = nextState ? (SoundEvent)DorpMod.WALKIETALKIE_ON.get() : (SoundEvent)DorpMod.WALKIETALKIE_OFF.get();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            String translationKey = nextState ? "actionbar.dorp.walkietalkie.on" : "actionbar.dorp.walkietalkie.off";
            Component msg = nextState ? Component.translatable(translationKey, new Object[]{freq}) : Component.translatable(translationKey);
            player.displayClientMessage(msg, true);
         }

         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
      } else {
         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      boolean active = isActive(stack);
      int freq = getFrequency(stack);
      int energy = getEnergy(stack);
      Component statusComponent = active
         ? Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("ON").withStyle(ChatFormatting.GREEN))
         : Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(Component.literal("OFF").withStyle(ChatFormatting.RED));
      tooltipComponents.add(statusComponent);
      tooltipComponents.add(
         Component.literal("Frequency: ").withStyle(ChatFormatting.GRAY).append(Component.literal(String.valueOf(freq)).withStyle(ChatFormatting.AQUA))
      );
      int maxEnergy = DorpConfig.WALKIE_TALKIE_MAX_ENERGY.get();
      tooltipComponents.add(
         Component.literal("Energy: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(energy + " / " + maxEnergy + " FE").withStyle(ChatFormatting.GOLD))
      );
      int sosUses = getSosUses(stack);
      int maxUses = DorpConfig.SOS_MAX_USES.get();
      tooltipComponents.add(
         Component.literal("SOS Distress Pings: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(sosUses + " / " + maxUses).withStyle(ChatFormatting.RED))
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Range: ").withStyle(ChatFormatting.GRAY).append(Component.literal("930 blocks").withStyle(ChatFormatting.AQUA)));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Right-Click to set Frequency").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      tooltipComponents.add(
         Component.literal("Crouch+Right-Click to Toggle ON/OFF").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public static void handleUpdatePayload(ServerPlayer player, int frequency, boolean toggleActive, boolean sendSos, String sosMessage) {
      ItemStack stack = player.getMainHandItem();
      if (!(stack.getItem() instanceof WalkieTalkieItem)) {
         stack = player.getOffhandItem();
      }

      if (stack.getItem() instanceof WalkieTalkieItem) {
         if (sendSos) {
            int uses = getSosUses(stack);
            if (uses > 0 && isActive(stack)) {
               setSosUses(stack, uses - 1);
               sendSosBroadcast(player, stack, sosMessage);
            }
         } else {
            boolean active = isActive(stack);
            if (toggleActive) {
               active = !active;
               setActive(stack, active);
               SoundEvent sound = active ? (SoundEvent)DorpMod.WALKIETALKIE_ON.get() : (SoundEvent)DorpMod.WALKIETALKIE_OFF.get();
               player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            if (frequency >= 1 && frequency <= 5000) {
               setFrequency(stack, frequency);
            }

            int freq = getFrequency(stack);
            String translationKey = active ? "actionbar.dorp.walkietalkie.on" : "actionbar.dorp.walkietalkie.off";
            Component msg = active ? Component.translatable(translationKey, new Object[]{freq}) : Component.translatable(translationKey);
            player.displayClientMessage(msg, true);
         }
      }
   }

   private static Component buildSosMessage(ServerPlayer sender, String sosMessage, double distanceSq, double maxRangeSq) {
      String coordsStr = String.format("X: %d, Y: %d, Z: %d", sender.blockPosition().getX(), sender.blockPosition().getY(), sender.blockPosition().getZ());
      String distortedCoords = DistortionUtil.distort(coordsStr, distanceSq, maxRangeSq);
      MutableComponent sosMsgBase = Component.empty()
         .append(Component.literal("[SOS Incoming] ").withStyle(ChatFormatting.RED))
         .append(sender.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
         .append(Component.literal(" at ").withStyle(ChatFormatting.GRAY))
         .append(Component.literal(distortedCoords).withStyle(new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.UNDERLINE}));
      if (sosMessage != null && !sosMessage.trim().isEmpty()) {
         String distortedMsg = DistortionUtil.distort(sosMessage.trim().toUpperCase(Locale.ROOT), distanceSq, maxRangeSq);
         sosMsgBase.append(Component.literal(" - " + distortedMsg).withStyle(ChatFormatting.GOLD));
      }

      return sosMsgBase;
   }

   private static void sendSosBroadcast(ServerPlayer sender, ItemStack stack, String sosMessage) {
      Level level = sender.level();
      double rangeSq = 5760000.0;
      Set<ServerPlayer> receivedPlayers = new HashSet<>();
      receivedPlayers.add(sender);
      sender.sendSystemMessage(buildSosMessage(sender, sosMessage, 0.0, rangeSq));
      level.playSound(null, sender.getX(), sender.getY(), sender.getZ(), (SoundEvent)DorpMod.WW1_WHISTLE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

      for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
         if (!receivedPlayers.contains(player)) {
            double distSq = DistanceHelper.distanceSq(level, sender.position(), player.position());
            if (player.level() == level && distSq <= rangeSq && hasAnyActiveDevice(player)) {
               player.sendSystemMessage(buildSosMessage(sender, sosMessage, distSq, rangeSq));
               player.level()
                  .playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)DorpMod.WW1_WHISTLE.get(), SoundSource.PLAYERS, 0.8F, 1.2F);
               receivedPlayers.add(player);
            }
         }
      }

      for (RadioBlockEntity radio : ChatHandler.ACTIVE_RADIOS) {
         if (radio.getLevel() == level
            && !radio.isRemoved()
            && radio.isActive()
            && DistanceHelper.distanceSq(level, radio.getBlockPos().getCenter(), sender.position()) <= rangeSq) {
            for (ServerPlayer playerx : sender.server.getPlayerList().getPlayers()) {
               if (!receivedPlayers.contains(playerx)
                  && playerx.level() == level
                  && DistanceHelper.distanceSq(level, radio.getBlockPos().getCenter(), playerx.position()) <= 1600.0) {
                  playerx.sendSystemMessage(buildSosMessage(sender, sosMessage, 0.0, rangeSq));
                  playerx.level()
                     .playSound(null, playerx.getX(), playerx.getY(), playerx.getZ(), (SoundEvent)DorpMod.WW1_WHISTLE.get(), SoundSource.PLAYERS, 0.8F, 1.2F);
                  receivedPlayers.add(playerx);
               }
            }

            RadioChatPayload payload = new RadioChatPayload(
               radio.getFrequency(),
               Events.getCustomDisplayNameString(sender),
               "SOS: X=" + sender.blockPosition().getX() + " Y=" + sender.blockPosition().getY() + " Z=" + sender.blockPosition().getZ()
            );

            for (ServerPlayer playerxx : sender.server.getPlayerList().getPlayers()) {
               if (playerxx.level() == level && DistanceHelper.distanceSq(level, radio.getBlockPos().getCenter(), playerxx.position()) <= 1600.0) {
                  PacketDistributor.sendToPlayer(playerxx, payload, new CustomPacketPayload[0]);
               }
            }
         }
      }
   }

   private static boolean hasAnyActiveDevice(ServerPlayer player) {
      for (ItemStack stack : player.getInventory().items) {
         if (stack.getItem() instanceof WalkieTalkieItem && isActive(stack)) {
            return true;
         }
      }

      ItemStack offhand = player.getOffhandItem();
      if (offhand.getItem() instanceof WalkieTalkieItem && isActive(offhand)) {
         return true;
      } else {
         for (ItemStack armor : player.getInventory().armor) {
            if (armor.getItem() instanceof WalkieTalkieItem && isActive(armor)) {
               return true;
            }
         }

         Optional<SlotResult> curioOpt = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEADSET.get()));
         if (curioOpt.isPresent()) {
            ItemStack headsetStack = curioOpt.get().stack();
            if (HeadsetItem.isActive(headsetStack)) {
               return true;
            }
         }

         return false;
      }
   }
}
