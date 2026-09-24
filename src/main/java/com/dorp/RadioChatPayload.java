package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record RadioChatPayload(int frequency, String senderName, String message) implements CustomPacketPayload {
   public static final Type<RadioChatPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "radio_chat"));
   public static final StreamCodec<FriendlyByteBuf, RadioChatPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      RadioChatPayload::frequency,
      ByteBufCodecs.STRING_UTF8,
      RadioChatPayload::senderName,
      ByteBufCodecs.STRING_UTF8,
      RadioChatPayload::message,
      RadioChatPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
