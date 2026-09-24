package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record RingTotemPayload() implements CustomPacketPayload {
   public static final Type<RingTotemPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "ring_totem"));
   public static final StreamCodec<FriendlyByteBuf, RingTotemPayload> STREAM_CODEC = StreamCodec.unit(new RingTotemPayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
