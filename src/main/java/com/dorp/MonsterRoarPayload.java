package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record MonsterRoarPayload(double x, double y, double z) implements CustomPacketPayload {
   public static final Type<MonsterRoarPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "monster_roar"));
   public static final StreamCodec<FriendlyByteBuf, MonsterRoarPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.DOUBLE,
      MonsterRoarPayload::x,
      ByteBufCodecs.DOUBLE,
      MonsterRoarPayload::y,
      ByteBufCodecs.DOUBLE,
      MonsterRoarPayload::z,
      MonsterRoarPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
