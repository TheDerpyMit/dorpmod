package com.dorp;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

public class InterceptorBlock extends BaseEntityBlock {
   public static final MapCodec<InterceptorBlock> CODEC = simpleCodec(InterceptorBlock::new);
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
   private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0.25, 0.0, 0.0, 15.75, 12.25, 16.0);
   private static final VoxelShape SHAPE_EAST_WEST = Block.box(0.0, 0.0, 0.25, 16.0, 12.25, 15.75);

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public InterceptorBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(PART, BedPart.FOOT)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, PART});
   }

   public BlockState updateShape(
      BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos
   ) {
      if (direction != getNeighbourDirection((BedPart)state.getValue(PART), (Direction)state.getValue(FACING))) {
         return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
      } else {
         return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART) ? state : Blocks.AIR.defaultBlockState();
      }
   }

   private static Direction getNeighbourDirection(BedPart part, Direction direction) {
      return part == BedPart.FOOT ? direction : direction.getOpposite();
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide) {
         BlockPos headPos = pos.relative((Direction)state.getValue(FACING));
         level.setBlock(headPos, (BlockState)state.setValue(PART, BedPart.HEAD), 3);
         level.blockUpdated(pos, Blocks.AIR);
         state.updateNeighbourShapes(level, pos, 3);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getHorizontalDirection().getOpposite();
      BlockPos blockpos = context.getClickedPos();
      BlockPos headPos = blockpos.relative(direction);
      Level level = context.getLevel();
      return level.getBlockState(headPos).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(headPos)
         ? (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction)).setValue(PART, BedPart.FOOT)
         : null;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return state.getValue(PART) == BedPart.FOOT ? new InterceptorBlockEntity(pos, state) : null;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide && player.isCreative()) {
         BedPart bedpart = (BedPart)state.getValue(PART);
         if (bedpart == BedPart.FOOT) {
            BlockPos headPos = pos.relative((Direction)state.getValue(FACING));
            BlockState headState = level.getBlockState(headPos);
            if (headState.is(this) && headState.getValue(PART) == BedPart.HEAD) {
               level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 35);
               level.levelEvent(player, 2001, headPos, Block.getId(headState));
            }
         } else {
            BlockPos footPos = pos.relative(((Direction)state.getValue(FACING)).getOpposite());
            BlockState footState = level.getBlockState(footPos);
            if (footState.is(this) && footState.getValue(PART) == BedPart.FOOT) {
               level.setBlock(footPos, Blocks.AIR.defaultBlockState(), 35);
               level.levelEvent(player, 2001, footPos, Block.getId(footState));
            }
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.isClientSide) {
         BlockPos entityPos = state.getValue(PART) == BedPart.FOOT ? pos : pos.relative(((Direction)state.getValue(FACING)).getOpposite());
         if (level.getBlockEntity(entityPos) instanceof InterceptorBlockEntity interceptor) {
            this.openInterceptorScreen(interceptor);
         }
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Direction direction = (Direction)state.getValue(FACING);
      return direction != Direction.NORTH && direction != Direction.SOUTH ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
   }

   private void openInterceptorScreen(InterceptorBlockEntity interceptor) {
      if (FMLEnvironment.dist.isClient()) {
         DorpModClient.openInterceptorScreen(interceptor);
      }
   }
}
