package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record StopwatchTotemPayload() implements CustomPacketPayload {
   public static final Type<StopwatchTotemPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "stopwatch_totem"));
   public static final StreamCodec<FriendlyByteBuf, StopwatchTotemPayload> STREAM_CODEC = StreamCodec.ofMember(
      StopwatchTotemPayload::write, StopwatchTotemPayload::new
   );

   public StopwatchTotemPayload(FriendlyByteBuf buffer) {
      this();
   }

   public void write(FriendlyByteBuf buffer) {
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
