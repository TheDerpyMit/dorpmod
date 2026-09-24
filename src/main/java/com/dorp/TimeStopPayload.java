package com.dorp;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record TimeStopPayload(int durationTicks) implements CustomPacketPayload {
   public static final Type<TimeStopPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "time_stop"));
   public static final StreamCodec<ByteBuf, TimeStopPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, TimeStopPayload::durationTicks, TimeStopPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
