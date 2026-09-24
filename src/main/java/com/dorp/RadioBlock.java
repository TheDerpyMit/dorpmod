package com.dorp;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

public class RadioBlock extends BaseEntityBlock {
   public static final MapCodec<RadioBlock> CODEC = simpleCodec(RadioBlock::new);
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(2.0, 0.0, 4.0, 14.0, 8.0, 12.0);
   private static final VoxelShape SHAPE_EAST_WEST = Block.box(4.0, 0.0, 2.0, 12.0, 8.0, 14.0);

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public RadioBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new RadioBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.isClientSide && level.getBlockEntity(pos) instanceof RadioBlockEntity radio) {
         this.openRadioScreen(radio);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Direction direction = (Direction)state.getValue(FACING);
      return direction != Direction.NORTH && direction != Direction.SOUTH ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
   }

   private void openRadioScreen(RadioBlockEntity radio) {
      if (FMLEnvironment.dist.isClient()) {
         DorpModClient.openRadioScreen(radio);
      }
   }
}
