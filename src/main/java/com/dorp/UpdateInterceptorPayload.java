package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record UpdateInterceptorPayload(BlockPos pos, boolean active) implements CustomPacketPayload {
   public static final Type<UpdateInterceptorPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "update_interceptor"));
   public static final StreamCodec<FriendlyByteBuf, UpdateInterceptorPayload> STREAM_CODEC = StreamCodec.ofMember(
      UpdateInterceptorPayload::write, UpdateInterceptorPayload::new
   );

   public UpdateInterceptorPayload(FriendlyByteBuf buffer) {
      this(buffer.readBlockPos(), buffer.readBoolean());
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(this.pos);
      buffer.writeBoolean(this.active);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
