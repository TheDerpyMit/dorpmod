package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ElytraWarningPayload() implements CustomPacketPayload {
   public static final Type<ElytraWarningPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "elytra_warning"));
   public static final StreamCodec<FriendlyByteBuf, ElytraWarningPayload> STREAM_CODEC = StreamCodec.unit(new ElytraWarningPayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
