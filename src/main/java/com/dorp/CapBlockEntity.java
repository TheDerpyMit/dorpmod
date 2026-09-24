package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CapBlockEntity extends BlockEntity {
   private ItemStack savedStack = ItemStack.EMPTY;

   public CapBlockEntity(BlockPos pos, BlockState blockState) {
      super((BlockEntityType)DorpMod.CAP_BLOCK_ENTITY.get(), pos, blockState);
   }

   public ItemStack getSavedStack() {
      return this.savedStack;
   }

   public void setSavedStack(ItemStack stack) {
      this.savedStack = stack.copy();
      this.savedStack.setCount(1);
      this.setChanged();
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      if (!this.savedStack.isEmpty()) {
         tag.put("SavedStack", this.savedStack.save(registries));
      }
   }

   public void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.contains("SavedStack", 10)) {
         this.savedStack = ItemStack.parse(registries, tag.getCompound("SavedStack")).orElse(ItemStack.EMPTY);
      } else {
         this.savedStack = ItemStack.EMPTY;
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveWithoutMetadata(registries);
   }
}
