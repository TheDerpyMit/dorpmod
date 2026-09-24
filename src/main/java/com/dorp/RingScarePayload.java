package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record RingScarePayload(double targetX, double targetY, double targetZ) implements CustomPacketPayload {
   public static final Type<RingScarePayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "ring_scare"));
   public static final StreamCodec<FriendlyByteBuf, RingScarePayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.DOUBLE,
      RingScarePayload::targetX,
      ByteBufCodecs.DOUBLE,
      RingScarePayload::targetY,
      ByteBufCodecs.DOUBLE,
      RingScarePayload::targetZ,
      RingScarePayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
