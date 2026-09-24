package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ScarePayload() implements CustomPacketPayload {
   public static final Type<ScarePayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "scare"));
   public static final StreamCodec<FriendlyByteBuf, ScarePayload> STREAM_CODEC = StreamCodec.unit(new ScarePayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
