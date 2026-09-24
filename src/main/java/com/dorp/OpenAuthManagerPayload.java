package com.dorp;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record OpenAuthManagerPayload(BlockPos pos, List<String> trustedPlayers, List<String> serverPlayers, boolean autoAuthOwner)
   implements CustomPacketPayload {
   public static final Type<OpenAuthManagerPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "open_auth_manager"));
   public static final StreamCodec<FriendlyByteBuf, OpenAuthManagerPayload> STREAM_CODEC = StreamCodec.ofMember(
      OpenAuthManagerPayload::write, OpenAuthManagerPayload::new
   );

   public OpenAuthManagerPayload(FriendlyByteBuf buf) {
      this((BlockPos)BlockPos.STREAM_CODEC.decode(buf), buf.readList(FriendlyByteBuf::readUtf), buf.readList(FriendlyByteBuf::readUtf), buf.readBoolean());
   }

   public void write(FriendlyByteBuf buf) {
      BlockPos.STREAM_CODEC.encode(buf, this.pos);
      buf.writeCollection(this.trustedPlayers, FriendlyByteBuf::writeUtf);
      buf.writeCollection(this.serverPlayers, FriendlyByteBuf::writeUtf);
      buf.writeBoolean(this.autoAuthOwner);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
