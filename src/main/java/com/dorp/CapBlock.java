package com.dorp;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CapBlock extends Block implements EntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0);

   public CapBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CapBlockEntity(pos, state);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof CapBlockEntity capBe) {
         capBe.setSavedStack(stack);
      }
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      BlockEntity be = (BlockEntity)builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if (be instanceof CapBlockEntity capBe) {
         ItemStack stack = capBe.getSavedStack();
         if (!stack.isEmpty()) {
            return Collections.singletonList(stack.copy());
         }
      }

      return super.getDrops(state, builder);
   }
}
