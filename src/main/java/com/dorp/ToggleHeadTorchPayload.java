package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ToggleHeadTorchPayload() implements CustomPacketPayload {
   public static final Type<ToggleHeadTorchPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "toggle_head_torch"));
   public static final StreamCodec<FriendlyByteBuf, ToggleHeadTorchPayload> STREAM_CODEC = StreamCodec.unit(new ToggleHeadTorchPayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
