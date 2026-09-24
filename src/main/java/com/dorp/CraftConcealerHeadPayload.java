package com.dorp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record CraftConcealerHeadPayload(String targetName, String targetUuid, String texValue, String texSig) implements CustomPacketPayload {
   public static final Type<CraftConcealerHeadPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "craft_concealer_head"));
   public static final StreamCodec<FriendlyByteBuf, CraftConcealerHeadPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CraftConcealerHeadPayload::targetName,
      ByteBufCodecs.STRING_UTF8,
      CraftConcealerHeadPayload::targetUuid,
      ByteBufCodecs.STRING_UTF8,
      CraftConcealerHeadPayload::texValue,
      ByteBufCodecs.STRING_UTF8,
      CraftConcealerHeadPayload::texSig,
      CraftConcealerHeadPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
