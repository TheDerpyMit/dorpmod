package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record BsodPayload() implements CustomPacketPayload {
   public static final Type<BsodPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "bsod"));
   public static final StreamCodec<FriendlyByteBuf, BsodPayload> STREAM_CODEC = StreamCodec.unit(new BsodPayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
