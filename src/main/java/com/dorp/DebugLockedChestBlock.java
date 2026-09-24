package com.dorp;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class DebugLockedChestBlock extends LockedChestBlock {
   public DebugLockedChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType) {
      super(properties, blockEntityType);
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest) {
         lockedChest.setOwner(UUID.fromString("00000000-0000-0000-0000-000000000000"), "DebugOwner");
         lockedChest.setPassword("debug123");
      }
   }
}
