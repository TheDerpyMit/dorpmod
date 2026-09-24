package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record RingAmbientPayload(int soundId) implements CustomPacketPayload {
   public static final Type<RingAmbientPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "ring_ambient"));
   public static final StreamCodec<FriendlyByteBuf, RingAmbientPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, RingAmbientPayload::soundId, RingAmbientPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
