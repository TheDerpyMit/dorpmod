package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ForbiddenWorkbenchBlock extends CraftingTableBlock {
   private static final Component CONTAINER_TITLE = Component.translatable("container.dorp.forbidden_workbench");
   private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 13.825, 16.0);

   public ForbiddenWorkbenchBlock(Properties properties) {
      super(properties);
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
      return new SimpleMenuProvider((containerId, playerInventory, player) -> {
         ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
         return new ForbiddenWorkbenchMenu(containerId, playerInventory, access) {
            public boolean stillValid(Player pPlayer) {
               return stillValid(this.access, pPlayer, (Block)DorpMod.FORBIDDEN_WORKBENCH_BLOCK.get());
            }
         };
      }, CONTAINER_TITLE);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      super.animateTick(state, level, pos, random);
      if (random.nextInt(3) == 0) {
         double d0 = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
         double d1 = pos.getY() + 1.0 + random.nextDouble() * 0.2;
         double d2 = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
         level.addParticle(ParticleTypes.ENCHANT, d0, d1, d2, 0.0, 0.1, 0.0);
      }

      if (random.nextInt(4) == 0) {
         double d0 = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
         double d1 = pos.getY() + 1.0 + random.nextDouble() * 0.2;
         double d2 = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
         level.addParticle(ParticleTypes.WITCH, d0, d1, d2, 0.0, 0.0, 0.0);
      }
   }
}
