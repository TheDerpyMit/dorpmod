package com.dorp;

import com.dorp.config.DorpConfig;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class InterceptorBlockEntity extends BlockEntity implements IEnergyStorage {
   public static final Set<InterceptorBlockEntity> ACTIVE_INTERCEPTORS = Collections.newSetFromMap(new WeakHashMap<>());
   private boolean active = false;
   private int energy = 0;

   public InterceptorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
      super(type, pos, blockState);
   }

   public InterceptorBlockEntity(BlockPos pos, BlockState blockState) {
      this((BlockEntityType<?>)DorpMod.INTERCEPTOR_BLOCK_ENTITY.get(), pos, blockState);
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
      this.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         if (!this.level.isClientSide()) {
            if (active) {
               ACTIVE_INTERCEPTORS.add(this);
            } else {
               ACTIVE_INTERCEPTORS.remove(this);
            }
         }
      }
   }

   public boolean consumeEnergyForMessage() {
      int cost = DorpConfig.INTERCEPTOR_ENERGY_PER_MESSAGE.get();
      if (this.energy >= cost) {
         this.energy -= cost;
         this.setChanged();
         if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         }

         return true;
      } else {
         return false;
      }
   }

   public void onLoad() {
      super.onLoad();
      if (this.active && this.level != null && !this.level.isClientSide()) {
         ACTIVE_INTERCEPTORS.add(this);
      }
   }

   public void setRemoved() {
      super.setRemoved();
      ACTIVE_INTERCEPTORS.remove(this);
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.putBoolean("active", this.active);
      tag.putInt("energy", this.energy);
   }

   public void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.contains("active")) {
         this.active = tag.getBoolean("active");
      }

      if (tag.contains("energy")) {
         this.energy = tag.getInt("energy");
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   public int receiveEnergy(int maxReceive, boolean simulate) {
      int max = DorpConfig.INTERCEPTOR_MAX_ENERGY.get();
      int energyReceived = Math.min(max - this.energy, maxReceive);
      if (!simulate) {
         this.energy += energyReceived;
         this.setChanged();
      }

      return energyReceived;
   }

   public int extractEnergy(int maxExtract, boolean simulate) {
      int energyExtracted = Math.min(this.energy, maxExtract);
      if (!simulate) {
         this.energy -= energyExtracted;
         this.setChanged();
      }

      return energyExtracted;
   }

   public int getEnergyStored() {
      return this.energy;
   }

   public int getMaxEnergyStored() {
      return DorpConfig.INTERCEPTOR_MAX_ENERGY.get();
   }

   public boolean canExtract() {
      return true;
   }

   public boolean canReceive() {
      return true;
   }
}
