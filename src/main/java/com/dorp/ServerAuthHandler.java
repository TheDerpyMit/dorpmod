package com.dorp;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerAuthHandler {
   public static void handleModify(ModifyAuthListPayload payload, IPayloadContext context) {
      context.enqueueWork(
         () -> {
            if (context.player() instanceof ServerPlayer player) {
               BlockPos pos = payload.pos();
               if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0
                  && player.level().getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest
                  && player.getUUID().equals(lockedChest.getOwnerUUID())) {
                  if (payload.isAdd()) {
                     lockedChest.addTrusted(payload.targetName());
                  } else {
                     lockedChest.removeTrusted(payload.targetName());
                  }
               }
            }
         }
      );
   }

   public static void handleSubmitSetupPassword(PasswordPayloads.SubmitSetupPassword payload, IPayloadContext context) {
      context.enqueueWork(
         () -> {
            if (context.player() instanceof ServerPlayer player) {
               BlockPos pos = payload.pos();
               if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0
                  && player.level().getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest
                  && player.getUUID().equals(lockedChest.getOwnerUUID())) {
                  lockedChest.setPassword(payload.password());
                  lockedChest.setAutoAuthOwner(payload.autoAuthOwner());
                  lockedChest.authenticateSession(player.getUUID());
                  player.displayClientMessage(Component.literal("Password setup successful!").withStyle(ChatFormatting.GREEN), true);
               }
            }
         }
      );
   }

   public static void handleSubmitEnterPassword(PasswordPayloads.SubmitEnterPassword payload, IPayloadContext context) {
      context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player) {
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0) {
               BlockEntity be = player.level().getBlockEntity(pos);
               if (be instanceof LockedChestBlockEntity lockedChest) {
                  if (lockedChest.getPassword().equals(payload.password())) {
                     lockedChest.authenticateSession(player.getUUID());
                     if (be instanceof MenuProvider menuProvider) {
                        PacketDistributor.sendToPlayer(player, new PasswordPayloads.ActiveLockedChest(pos), new CustomPacketPayload[0]);
                        player.openMenu(menuProvider);
                     }
                  } else {
                     player.displayClientMessage(Component.literal("Incorrect password!").withStyle(ChatFormatting.RED), true);
                  }
               }
            }
         }
      });
   }

   public static void handleToggleAutoAuth(PasswordPayloads.ToggleAutoAuth payload, IPayloadContext context) {
      context.enqueueWork(
         () -> {
            if (context.player() instanceof ServerPlayer player) {
               BlockPos pos = payload.pos();
               if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0
                  && player.level().getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest
                  && player.getUUID().equals(lockedChest.getOwnerUUID())) {
                  lockedChest.setAutoAuthOwner(payload.autoAuthOwner());
               }
            }
         }
      );
   }

   public static void handleRequestAuthManager(PasswordPayloads.RequestAuthManager payload, IPayloadContext context) {
      context.enqueueWork(
         () -> {
            if (context.player() instanceof ServerPlayer player) {
               BlockPos pos = payload.pos();
               if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0
                  && player.level().getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest
                  && player.getUUID().equals(lockedChest.getOwnerUUID())) {
                  List<String> serverPlayers = new ArrayList<>();
                  File usercache = new File(player.server.getServerDirectory().toFile(), "usercache.json");
                  if (usercache.exists()) {
                     try (FileReader reader = new FileReader(usercache)) {
                        for (JsonElement el : JsonParser.parseReader(reader).getAsJsonArray()) {
                           serverPlayers.add(el.getAsJsonObject().get("name").getAsString());
                        }
                     } catch (Exception var14) {
                        var14.printStackTrace();
                     }
                  }

                  serverPlayers.sort(String.CASE_INSENSITIVE_ORDER);
                  PacketDistributor.sendToPlayer(
                     player,
                     new OpenAuthManagerPayload(pos, lockedChest.getTrustedList(), serverPlayers, lockedChest.isAutoAuthOwner()),
                     new CustomPacketPayload[0]
                  );
               }
            }
         }
      );
   }

   public static void handleAuthMenuClosed(PasswordPayloads.AuthMenuClosed payload, IPayloadContext context) {
      context.enqueueWork(
         () -> {
            if (context.player() instanceof ServerPlayer player) {
               BlockPos pos = payload.pos();
               if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0
                  && player.level().getBlockEntity(pos) instanceof LockedChestBlockEntity var5) {
                  ;
               }
            }
         }
      );
   }

   public static void handleBreachChest(BreachChestPayload payload, IPayloadContext context) {
      context.enqueueWork(
         () -> {
            if (context.player() instanceof ServerPlayer player) {
               BlockPos pos = payload.pos();
               if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0
                  && player.level().getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest) {
                  ItemStack breacherStack = ItemStack.EMPTY;
                  InteractionHand usedHand = null;
                  if (player.getMainHandItem().getItem() == DorpMod.BREACHER.get()) {
                     breacherStack = player.getMainHandItem();
                     usedHand = InteractionHand.MAIN_HAND;
                  } else if (player.getOffhandItem().getItem() == DorpMod.BREACHER.get()) {
                     breacherStack = player.getOffhandItem();
                     usedHand = InteractionHand.OFF_HAND;
                  }

                  if (!breacherStack.isEmpty() && usedHand != null) {
                     lockedChest.setOwner(player.getUUID(), player.getName().getString());
                     lockedChest.setPassword("");
                     lockedChest.setAutoAuthOwner(false);
                     lockedChest.getTrustedList().forEach(lockedChest::removeTrusted);
                     breacherStack.hurtAndBreak(1, player.serverLevel(), player, item -> {});
                     player.level().playSound(null, pos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 1.0F, 1.0F);
                     player.displayClientMessage(Component.literal("Chest Breached! You are now the owner.").withStyle(ChatFormatting.GREEN), true);
                  }
               }
            }
         }
      );
   }
}
