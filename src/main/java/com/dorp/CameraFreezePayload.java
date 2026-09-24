package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record CameraFreezePayload(boolean freeze) implements CustomPacketPayload {
   public static final Type<CameraFreezePayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "camera_freeze"));
   public static final StreamCodec<FriendlyByteBuf, CameraFreezePayload> STREAM_CODEC = StreamCodec.ofMember(
      CameraFreezePayload::write, CameraFreezePayload::new
   );

   public CameraFreezePayload(FriendlyByteBuf buffer) {
      this(buffer.readBoolean());
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.freeze);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
