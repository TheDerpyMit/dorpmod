package com.dorp;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

public class LockedChestBlock extends ChestBlock {
   public static final MapCodec<LockedChestBlock> CODEC = simpleCodec(properties -> new LockedChestBlock(properties, DorpMod.LOCKED_CHEST_BLOCK_ENTITY::get));

   public LockedChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType) {
      super(properties, blockEntityType);
   }

   public MapCodec<? extends ChestBlock> codec() {
      return CODEC;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new LockedChestBlockEntity(pos, state);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = super.getStateForPlacement(context);
      if (state != null && (state.getValue(TYPE) == ChestType.LEFT || state.getValue(TYPE) == ChestType.RIGHT)) {
         Direction connectedDir = getConnectedDirection(state);
         if (connectedDir != null
            && context.getLevel().getBlockEntity(context.getClickedPos().relative(connectedDir)) instanceof LockedChestBlockEntity lockedChest) {
            if (this instanceof DebugLockedChestBlock) {
               return state;
            }

            if (!context.getPlayer().getUUID().equals(lockedChest.getOwnerUUID())) {
               return (BlockState)state.setValue(TYPE, ChestType.SINGLE);
            }
         }
      }

      return state;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest && placer instanceof Player player) {
         lockedChest.setOwner(player.getUUID(), player.getName().getString());
         if (state.hasProperty(TYPE) && state.getValue(TYPE) != ChestType.SINGLE) {
            Direction connectedDir = getConnectedDirection(state);
            if (connectedDir != null && level.getBlockEntity(pos.relative(connectedDir)) instanceof LockedChestBlockEntity adjacentLockedChest) {
               lockedChest.setPassword(adjacentLockedChest.getPassword(), false);
               lockedChest.setAutoAuthOwner(adjacentLockedChest.isAutoAuthOwner(), false);

               for (String trusted : adjacentLockedChest.getTrustedList()) {
                  lockedChest.addTrusted(trusted, false);
               }
            }
         }
      }
   }

   protected ItemInteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (!(stack.getItem() instanceof BreacherItem && level.getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest)) {
         return super.useItemOn(stack, state, level, pos, player, hand, hit);
      } else if (player.getUUID().equals(lockedChest.getOwnerUUID())) {
         if (!level.isClientSide()) {
            player.displayClientMessage(Component.literal("You already own this chest!"), false);
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide());
      } else {
         if (FMLEnvironment.dist.isClient()) {
            DorpModClient.openBreachHackScreen(pos);
         }

         return ItemInteractionResult.sidedSuccess(level.isClientSide());
      }
   }

   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!(!level.isClientSide() && level.getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest)) {
         return InteractionResult.SUCCESS;
      } else if (player.getUUID().equals(lockedChest.getOwnerUUID())) {
         if (lockedChest.getPassword() == null || lockedChest.getPassword().isEmpty()) {
            PacketDistributor.sendToPlayer((ServerPlayer)player, new PasswordPayloads.OpenSetupPassword(pos), new CustomPacketPayload[0]);
            return InteractionResult.SUCCESS;
         } else if (lockedChest.isAutoAuthOwner()) {
            PacketDistributor.sendToPlayer((ServerPlayer)player, new PasswordPayloads.ActiveLockedChest(pos), new CustomPacketPayload[0]);
            return super.useWithoutItem(state, level, pos, player, hit);
         } else {
            PacketDistributor.sendToPlayer((ServerPlayer)player, new PasswordPayloads.OpenEnterPassword(pos), new CustomPacketPayload[0]);
            return InteractionResult.SUCCESS;
         }
      } else if (lockedChest.isTrusted(player.getName().getString())) {
         return super.useWithoutItem(state, level, pos, player, hit);
      } else if (lockedChest.getPassword() == null || lockedChest.getPassword().isEmpty()) {
         player.displayClientMessage(Component.literal("This chest is locked and has not been set up by the owner yet!"), true);
         return InteractionResult.SUCCESS;
      } else if (lockedChest.isSessionAuthenticated(player.getUUID())) {
         return super.useWithoutItem(state, level, pos, player, hit);
      } else {
         PacketDistributor.sendToPlayer((ServerPlayer)player, new PasswordPayloads.OpenEnterPassword(pos), new CustomPacketPayload[0]);
         return InteractionResult.SUCCESS;
      }
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof LockedChestBlockEntity lockedChest) {
            Containers.dropContents(level, pos, lockedChest);
            level.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }
}
