package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record UpdateRadioPayload(BlockPos pos, int frequency, boolean toggleActive) implements CustomPacketPayload {
   public static final Type<UpdateRadioPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "update_radio"));
   public static final StreamCodec<FriendlyByteBuf, UpdateRadioPayload> STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      UpdateRadioPayload::pos,
      ByteBufCodecs.INT,
      UpdateRadioPayload::frequency,
      ByteBufCodecs.BOOL,
      UpdateRadioPayload::toggleActive,
      UpdateRadioPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
