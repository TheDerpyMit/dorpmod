package com.dorp;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ScannedInventoryPayload(String playerName, CompoundTag inventoryData) implements CustomPacketPayload {
   public static final Type<ScannedInventoryPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "scanned_inventory"));
   public static final StreamCodec<FriendlyByteBuf, ScannedInventoryPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      ScannedInventoryPayload::playerName,
      ByteBufCodecs.COMPOUND_TAG,
      ScannedInventoryPayload::inventoryData,
      ScannedInventoryPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
