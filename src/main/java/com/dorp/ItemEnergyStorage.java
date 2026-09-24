package com.dorp;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ItemEnergyStorage implements IEnergyStorage {
   private final ItemStack stack;
   private final int capacity;

   public ItemEnergyStorage(ItemStack stack, int capacity) {
      this.stack = stack;
      this.capacity = capacity;
   }

   public int receiveEnergy(int maxReceive, boolean simulate) {
      int energy = this.getEnergy();
      int energyReceived = Math.min(this.capacity - energy, maxReceive);
      if (!simulate && energyReceived > 0) {
         this.setEnergy(energy + energyReceived);
      }

      return energyReceived;
   }

   public int extractEnergy(int maxExtract, boolean simulate) {
      int energy = this.getEnergy();
      int energyExtracted = Math.min(energy, maxExtract);
      if (!simulate && energyExtracted > 0) {
         this.setEnergy(energy - energyExtracted);
      }

      return energyExtracted;
   }

   public int getEnergyStored() {
      return this.getEnergy();
   }

   public int getMaxEnergyStored() {
      return this.capacity;
   }

   public boolean canExtract() {
      return true;
   }

   public boolean canReceive() {
      return true;
   }

   private int getEnergy() {
      CustomData customData = (CustomData)this.stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         CompoundTag tag = customData.copyTag();
         if (tag.contains("Energy")) {
            return tag.getInt("Energy");
         }
      }

      return 0;
   }

   private void setEnergy(int energy) {
      if (this.stack.getItem() instanceof WalkieTalkieItem) {
         WalkieTalkieItem.updateEnergyAndSos(this.stack, energy, this.capacity);
      } else {
         CompoundTag tag = new CompoundTag();
         CustomData customData = (CustomData)this.stack.get(DataComponents.CUSTOM_DATA);
         if (customData != null) {
            tag = customData.copyTag();
         }

         tag.putInt("Energy", Math.max(0, Math.min(this.capacity, energy)));
         this.stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      }
   }
}
