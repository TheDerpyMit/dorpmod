package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadioBlockEntity extends BlockEntity {
   private int frequency = 1;
   private boolean active = false;
   private long lastActivatedTime = -1L;
   private long lastDeactivatedTime = -1L;

   public RadioBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
      super(type, pos, blockState);
   }

   public RadioBlockEntity(BlockPos pos, BlockState blockState) {
      this((BlockEntityType<?>)DorpMod.RADIO_BLOCK_ENTITY.get(), pos, blockState);
   }

   public int getFrequency() {
      return this.frequency;
   }

   public void setFrequency(int frequency) {
      this.frequency = Math.max(1, Math.min(5000, frequency));
      this.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public boolean isActive() {
      return this.active;
   }

   public long getLastActivatedTime() {
      return this.lastActivatedTime;
   }

   public void setLastActivatedTime(long lastActivatedTime) {
      this.lastActivatedTime = lastActivatedTime;
   }

   public long getLastDeactivatedTime() {
      return this.lastDeactivatedTime;
   }

   public void setLastDeactivatedTime(long lastDeactivatedTime) {
      this.lastDeactivatedTime = lastDeactivatedTime;
   }

   public void setActive(boolean active) {
      if (active && !this.active) {
         this.lastActivatedTime = System.currentTimeMillis();
         this.lastDeactivatedTime = -1L;
      } else if (!active && this.active) {
         this.lastDeactivatedTime = System.currentTimeMillis();
         this.lastActivatedTime = -1L;
      }

      this.active = active;
      this.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         if (!this.level.isClientSide()) {
            if (active) {
               ChatHandler.ACTIVE_RADIOS.add(this);
            } else {
               ChatHandler.ACTIVE_RADIOS.remove(this);
            }
         }
      }
   }

   public void onLoad() {
      super.onLoad();
      if (this.active && this.level != null && !this.level.isClientSide()) {
         ChatHandler.ACTIVE_RADIOS.add(this);
      }
   }

   public void setRemoved() {
      super.setRemoved();
      ChatHandler.ACTIVE_RADIOS.remove(this);
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.putInt("frequency", this.frequency);
      tag.putBoolean("active", this.active);
   }

   public void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.contains("frequency")) {
         this.frequency = tag.getInt("frequency");
      }

      if (tag.contains("active")) {
         boolean nextActive = tag.getBoolean("active");
         if (nextActive && !this.active) {
            this.lastActivatedTime = System.currentTimeMillis();
            this.lastDeactivatedTime = -1L;
         } else if (!nextActive && this.active) {
            this.lastDeactivatedTime = System.currentTimeMillis();
            this.lastActivatedTime = -1L;
         }

         this.active = nextActive;
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveWithoutMetadata(registries);
   }
}
