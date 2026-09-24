package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record UpdateHeadsetPayload(int frequency, boolean toggleActive) implements CustomPacketPayload {
   public static final Type<UpdateHeadsetPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "update_headset"));
   public static final StreamCodec<FriendlyByteBuf, UpdateHeadsetPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, UpdateHeadsetPayload::frequency, ByteBufCodecs.BOOL, UpdateHeadsetPayload::toggleActive, UpdateHeadsetPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
