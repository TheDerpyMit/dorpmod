package com.dorp;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DebugRadioBlockEntity extends RadioBlockEntity {
   private int tickCounter = 0;

   public DebugRadioBlockEntity(BlockPos pos, BlockState blockState) {
      super((BlockEntityType<?>)DorpMod.DEBUG_RADIO_BLOCK_ENTITY.get(), pos, blockState);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, DebugRadioBlockEntity entity) {
      if (entity.isActive()) {
         entity.tickCounter++;
         if (entity.tickCounter >= 100) {
            entity.tickCounter = 0;
            int freq = entity.getFrequency();
            Component msg = Component.empty()
               .append(Component.literal("[DEBUG RADIO] ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal("Ping from freq " + freq).withStyle(ChatFormatting.WHITE));
            if (level.getServer() != null) {
               for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                  player.sendSystemMessage(msg);
               }
            }
         }
      }
   }
}
