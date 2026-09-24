package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ScanPlayerPayload(BlockPos blockPos) implements CustomPacketPayload {
   public static final Type<ScanPlayerPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "scan_player"));
   public static final StreamCodec<FriendlyByteBuf, ScanPlayerPayload> STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC, ScanPlayerPayload::blockPos, ScanPlayerPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
