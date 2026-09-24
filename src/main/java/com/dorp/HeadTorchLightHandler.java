package com.dorp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

public class HeadTorchLightHandler {
   private static final int LIGHT_LEVEL = 14;

   private record PlacedLight(ResourceKey<Level> dim, BlockPos pos) {
   }

   private record KnownState(boolean active, ResourceKey<Level> dim, BlockPos feet) {
   }

   private static final Map<UUID, PlacedLight> PLACED = new HashMap<>();
   private static final Map<UUID, KnownState> KNOWN = new HashMap<>();

   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) {
         return;
      }
      if (player.tickCount % 5 != 0) {
         return;
      }
      updateLight(player);
   }

   public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         removeLight(player.getServer(), player.getUUID());
         KNOWN.remove(player.getUUID());
      }
   }

   private static boolean isTorchActive(ServerPlayer player) {
      return CuriosApi.getCuriosInventory(player)
         .flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEAD_TORCH.get()))
         .map(slotResult -> {
            ItemStack stack = slotResult.stack();
            return HeadTorchItem.isActive(stack);
         })
         .orElse(false);
   }

   private static void updateLight(ServerPlayer player) {
      UUID id = player.getUUID();
      if (player.isSpectator()) {
         removeLight(player.getServer(), id);
         KNOWN.remove(id);
         return;
      }
      boolean active = isTorchActive(player);
      ServerLevel level = player.serverLevel();
      BlockPos feet = player.blockPosition();
      KnownState known = KNOWN.get(id);
      if (known != null && known.active == active && known.dim.equals(level.dimension()) && known.feet.equals(feet)) {
         return;
      }
      KNOWN.put(id, new KnownState(active, level.dimension(), feet));
      BlockPos target = active ? feet : null;
      PlacedLight cur = PLACED.get(id);
      if (cur != null && (target == null || !cur.dim.equals(level.dimension()) || !cur.pos.equals(target))) {
         removeLight(player.getServer(), id);
         cur = null;
      }
      if (target != null && cur == null) {
         BlockState existing = level.getBlockState(target);
         if (existing.isAir()) {
            level.setBlock(target, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL), 3);
            PLACED.put(id, new PlacedLight(level.dimension(), target));
         } else if (existing.is(Blocks.WATER)) {
            level.setBlock(
               target,
               Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL).setValue(LightBlock.WATERLOGGED, true),
               3
            );
            PLACED.put(id, new PlacedLight(level.dimension(), target));
         }
      }
   }

   private static void removeLight(MinecraftServer server, UUID id) {
      PlacedLight light = PLACED.remove(id);
      if (light == null || server == null) {
         return;
      }
      ServerLevel oldLevel = server.getLevel(light.dim);
      if (oldLevel != null && oldLevel.getBlockState(light.pos).is(Blocks.LIGHT)) {
         oldLevel.setBlock(light.pos, Blocks.AIR.defaultBlockState(), 3);
      }
   }
}
