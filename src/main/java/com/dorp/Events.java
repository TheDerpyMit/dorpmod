package com.dorp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.NameFormat;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@EventBusSubscriber(modid = "dorp", bus = Bus.GAME)
public class Events {
   @SubscribeEvent
   public static void onBlockBreak(BreakEvent event) {
      if (event.getLevel().getBlockEntity(event.getPos()) instanceof LockedChestBlockEntity lockedChest) {
         Player player = event.getPlayer();
         if (player != null && !player.getUUID().equals(lockedChest.getOwnerUUID())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("You cannot break this chest! It is owned by " + lockedChest.getOwnerName() + "."), true);
         }
      }
   }

   private static String getConcealedName(Player player) {
      ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
      if (headStack.is((Item)DorpMod.PAPER_BAG.get())) {
         return "Concealed Player";
      } else {
         if (headStack.is(Items.PLAYER_HEAD)) {
            CustomData customData = (CustomData)headStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.copyTag().getBoolean("concealer")) {
               ResolvableProfile profile = (ResolvableProfile)headStack.get(DataComponents.PROFILE);
               if (profile != null && profile.name().isPresent()) {
                  return (String)profile.name().get();
               }
            }
         }

         return null;
      }
   }

   public static String getCustomDisplayNameString(Player player) {
      String concealed = getConcealedName(player);
      if (concealed != null) {
         return concealed;
      } else {
         return player.getPersistentData().contains("DorpCustomDisplayName")
            ? player.getPersistentData().getString("DorpCustomDisplayName")
            : player.getScoreboardName();
      }
   }

   @SubscribeEvent
   public static void onNameFormat(NameFormat event) {
      String customName = getCustomDisplayNameString(event.getEntity());
      if (customName != null && !customName.equals(event.getEntity().getScoreboardName())) {
         event.setDisplayname(Component.literal(customName));
      }
   }

   @SubscribeEvent
   public static void onTabListNameFormat(TabListNameFormat event) {
      String customName = getCustomDisplayNameString(event.getEntity());
      if (customName != null && !customName.equals(event.getEntity().getScoreboardName())) {
         event.setDisplayName(Component.literal(customName));
      }
   }

   @SubscribeEvent
   public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getSlot() == EquipmentSlot.HEAD) {
         player.refreshDisplayName();
         if (player.getServer() != null) {
            player.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(Action.UPDATE_DISPLAY_NAME, player));
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(Post event) {
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         Player player = event.getEntity();
         player.getLastDeathLocation().ifPresent(pos -> {
            ItemStack paper = new ItemStack(Items.PAPER);
            paper.set(DataComponents.CUSTOM_NAME, Component.literal("Death Info").withStyle(ChatFormatting.RED));
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Component> loreList = new ArrayList<>();
            loreList.add(Component.literal("X: " + pos.pos().getX() + " Y: " + pos.pos().getY() + " Z: " + pos.pos().getZ()).withStyle(ChatFormatting.GRAY));
            loreList.add(Component.literal("Dimension: " + pos.dimension().location().toString()).withStyle(ChatFormatting.GRAY));
            loreList.add(Component.literal("Time: " + time).withStyle(ChatFormatting.GRAY));
            paper.set(DataComponents.LORE, new ItemLore(loreList));
            if (!player.getInventory().add(paper)) {
               player.drop(paper, false);
            }
         });
      }
   }

   @SubscribeEvent
   public static void onItemCrafted(ItemCraftedEvent event) {
      ItemStack stack = event.getCrafting();
      if (!stack.isEmpty() && stack.getItem().builtInRegistryHolder().key().location().toString().equals("immersivecomputing:device")) {
         ItemStack manufactured = ImmersiveLaptopHelper.createLaptopStack();
         if (!manufactured.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, (CustomData)manufactured.get(DataComponents.CUSTOM_DATA));
            stack.set(DataComponents.CUSTOM_NAME, (Component)manufactured.get(DataComponents.CUSTOM_NAME));
         }
      }
   }
}
