package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record BreachChestPayload(BlockPos pos) implements CustomPacketPayload {
   public static final Type<BreachChestPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "breach_chest"));
   public static final StreamCodec<FriendlyByteBuf, BreachChestPayload> STREAM_CODEC = StreamCodec.ofMember(BreachChestPayload::write, BreachChestPayload::new);

   public BreachChestPayload(FriendlyByteBuf buf) {
      this(buf.readBlockPos());
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(this.pos);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
