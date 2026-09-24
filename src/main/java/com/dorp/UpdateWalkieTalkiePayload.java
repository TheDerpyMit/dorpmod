package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record UpdateWalkieTalkiePayload(int frequency, boolean toggleActive, boolean sendSos, String sosMessage) implements CustomPacketPayload {
   public static final Type<UpdateWalkieTalkiePayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "update_walkie_talkie"));
   public static final StreamCodec<FriendlyByteBuf, UpdateWalkieTalkiePayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      UpdateWalkieTalkiePayload::frequency,
      ByteBufCodecs.BOOL,
      UpdateWalkieTalkiePayload::toggleActive,
      ByteBufCodecs.BOOL,
      UpdateWalkieTalkiePayload::sendSos,
      ByteBufCodecs.STRING_UTF8,
      UpdateWalkieTalkiePayload::sosMessage,
      UpdateWalkieTalkiePayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
