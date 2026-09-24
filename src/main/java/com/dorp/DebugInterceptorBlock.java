package com.dorp;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;

public class DebugInterceptorBlock extends InterceptorBlock {
   public static final MapCodec<DebugInterceptorBlock> CODEC = simpleCodec(DebugInterceptorBlock::new);

   @Override
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public DebugInterceptorBlock(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new DebugInterceptorBlockEntity(pos, state);
   }
}
