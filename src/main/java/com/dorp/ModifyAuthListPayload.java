package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ModifyAuthListPayload(BlockPos pos, String targetName, boolean isAdd) implements CustomPacketPayload {
   public static final Type<ModifyAuthListPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "modify_auth_list"));
   public static final StreamCodec<FriendlyByteBuf, ModifyAuthListPayload> STREAM_CODEC = StreamCodec.ofMember(
      ModifyAuthListPayload::write, ModifyAuthListPayload::new
   );

   public ModifyAuthListPayload(FriendlyByteBuf buf) {
      this(buf.readBlockPos(), buf.readUtf(), buf.readBoolean());
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.pos);
      buf.writeUtf(this.targetName);
      buf.writeBoolean(this.isAdd);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
