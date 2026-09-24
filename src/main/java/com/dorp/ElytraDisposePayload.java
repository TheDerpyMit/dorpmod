package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ElytraDisposePayload() implements CustomPacketPayload {
   public static final Type<ElytraDisposePayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "elytra_dispose"));
   public static final StreamCodec<FriendlyByteBuf, ElytraDisposePayload> STREAM_CODEC = StreamCodec.unit(new ElytraDisposePayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
