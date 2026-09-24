package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record CrimsonWarningPayload() implements CustomPacketPayload {
   public static final Type<CrimsonWarningPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "crimson_warning"));
   public static final StreamCodec<FriendlyByteBuf, CrimsonWarningPayload> STREAM_CODEC = StreamCodec.unit(new CrimsonWarningPayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
