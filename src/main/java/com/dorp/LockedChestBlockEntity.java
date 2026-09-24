package com.dorp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class LockedChestBlockEntity extends ChestBlockEntity implements WorldlyContainer {
   private UUID ownerUUID = null;
   private String ownerName = "Unknown";
   private final List<String> trustedPlayers = new ArrayList<>();
   private String password = "";
   private boolean autoAuthOwner = false;
   private final List<UUID> sessionAuthenticated = new ArrayList<>();

   public LockedChestBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)DorpMod.LOCKED_CHEST_BLOCK_ENTITY.get(), pos, state);
   }

   public LockedChestBlockEntity getConnectedHalf() {
      BlockState state = this.getBlockState();
      if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
         Direction connectedDir = ChestBlock.getConnectedDirection(state);
         if (connectedDir != null
            && this.level != null
            && this.level.getBlockEntity(this.worldPosition.relative(connectedDir)) instanceof LockedChestBlockEntity lockedChest) {
            return lockedChest;
         }
      }

      return null;
   }

   public void setOwner(UUID uuid, String name) {
      this.setOwner(uuid, name, true);
   }

   public void setOwner(UUID uuid, String name, boolean sync) {
      this.ownerUUID = uuid;
      this.ownerName = name;
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }

      if (sync) {
         LockedChestBlockEntity connected = this.getConnectedHalf();
         if (connected != null) {
            connected.setOwner(uuid, name, false);
         }
      }
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public String getOwnerName() {
      return this.ownerName;
   }

   public boolean isTrusted(String name) {
      return this.trustedPlayers.contains(name);
   }

   public List<String> getTrustedList() {
      return new ArrayList<>(this.trustedPlayers);
   }

   public void addTrusted(String name) {
      this.addTrusted(name, true);
   }

   public void addTrusted(String name, boolean sync) {
      if (!this.trustedPlayers.contains(name)) {
         this.trustedPlayers.add(name);
         this.setChanged();
      }

      if (sync) {
         LockedChestBlockEntity connected = this.getConnectedHalf();
         if (connected != null) {
            connected.addTrusted(name, false);
         }
      }
   }

   public void removeTrusted(String name) {
      this.removeTrusted(name, true);
   }

   public void removeTrusted(String name, boolean sync) {
      this.trustedPlayers.remove(name);
      this.setChanged();
      if (sync) {
         LockedChestBlockEntity connected = this.getConnectedHalf();
         if (connected != null) {
            connected.removeTrusted(name, false);
         }
      }
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.setPassword(password, true);
   }

   public void setPassword(String password, boolean sync) {
      this.password = password;
      this.setChanged();
      if (sync) {
         LockedChestBlockEntity connected = this.getConnectedHalf();
         if (connected != null) {
            connected.setPassword(password, false);
         }
      }
   }

   public boolean isAutoAuthOwner() {
      return this.autoAuthOwner;
   }

   public void setAutoAuthOwner(boolean autoAuthOwner) {
      this.setAutoAuthOwner(autoAuthOwner, true);
   }

   public void setAutoAuthOwner(boolean autoAuthOwner, boolean sync) {
      this.autoAuthOwner = autoAuthOwner;
      this.setChanged();
      if (sync) {
         LockedChestBlockEntity connected = this.getConnectedHalf();
         if (connected != null) {
            connected.setAutoAuthOwner(autoAuthOwner, false);
         }
      }
   }

   public boolean isSessionAuthenticated(UUID playerUUID) {
      return this.sessionAuthenticated.contains(playerUUID);
   }

   public void authenticateSession(UUID playerUUID) {
      this.authenticateSession(playerUUID, true);
   }

   public void authenticateSession(UUID playerUUID, boolean sync) {
      if (!this.sessionAuthenticated.contains(playerUUID)) {
         this.sessionAuthenticated.add(playerUUID);
      }

      if (sync) {
         LockedChestBlockEntity connected = this.getConnectedHalf();
         if (connected != null) {
            connected.authenticateSession(playerUUID, false);
         }
      }
   }

   public int[] getSlotsForFace(Direction side) {
      return new int[0];
   }

   public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
      return false;
   }

   public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
      return false;
   }

   protected Component getDefaultName() {
      return Component.translatable("container.dorp.locked_chest");
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.contains("OwnerUUID")) {
         this.ownerUUID = tag.getUUID("OwnerUUID");
      }

      if (tag.contains("OwnerName")) {
         this.ownerName = tag.getString("OwnerName");
      }

      this.trustedPlayers.clear();
      if (tag.contains("TrustedPlayers", 9)) {
         ListTag list = tag.getList("TrustedPlayers", 8);

         for (int i = 0; i < list.size(); i++) {
            this.trustedPlayers.add(list.getString(i));
         }
      }

      if (tag.contains("ChestPassword")) {
         this.password = tag.getString("ChestPassword");
      }

      if (tag.contains("AutoAuthOwner")) {
         this.autoAuthOwner = tag.getBoolean("AutoAuthOwner");
      }
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag(Provider registries) {
      CompoundTag tag = new CompoundTag();
      if (this.ownerUUID != null) {
         tag.putUUID("OwnerUUID", this.ownerUUID);
      }

      tag.putString("OwnerName", this.ownerName);
      return tag;
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      if (this.ownerUUID != null) {
         tag.putUUID("OwnerUUID", this.ownerUUID);
      }

      tag.putString("OwnerName", this.ownerName);
      ListTag list = new ListTag();

      for (String trusted : this.trustedPlayers) {
         list.add(StringTag.valueOf(trusted));
      }

      tag.put("TrustedPlayers", list);
      tag.putString("ChestPassword", this.password);
      tag.putBoolean("AutoAuthOwner", this.autoAuthOwner);
   }
}
