package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ElytraAcceptPayload() implements CustomPacketPayload {
   public static final Type<ElytraAcceptPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "elytra_accept"));
   public static final StreamCodec<FriendlyByteBuf, ElytraAcceptPayload> STREAM_CODEC = StreamCodec.unit(new ElytraAcceptPayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
