package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DebugInterceptorBlockEntity extends InterceptorBlockEntity {
   public DebugInterceptorBlockEntity(BlockPos pos, BlockState blockState) {
      super((BlockEntityType<?>)DorpMod.DEBUG_INTERCEPTOR_BLOCK_ENTITY.get(), pos, blockState);
   }

   @Override
   public boolean consumeEnergyForMessage() {
      return true;
   }
}
