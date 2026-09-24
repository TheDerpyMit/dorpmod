package com.dorp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public class PasswordPayloads {
   public record ActiveLockedChest(BlockPos pos) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.ActiveLockedChest> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "active_locked_chest"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.ActiveLockedChest> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC, PasswordPayloads.ActiveLockedChest::pos, PasswordPayloads.ActiveLockedChest::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record AuthMenuClosed(BlockPos pos) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.AuthMenuClosed> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "auth_menu_closed"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.AuthMenuClosed> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC, PasswordPayloads.AuthMenuClosed::pos, PasswordPayloads.AuthMenuClosed::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record OpenEnterPassword(BlockPos pos) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.OpenEnterPassword> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "open_enter_password"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.OpenEnterPassword> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC, PasswordPayloads.OpenEnterPassword::pos, PasswordPayloads.OpenEnterPassword::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record OpenSetupPassword(BlockPos pos) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.OpenSetupPassword> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "open_setup_password"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.OpenSetupPassword> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC, PasswordPayloads.OpenSetupPassword::pos, PasswordPayloads.OpenSetupPassword::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record RequestAuthManager(BlockPos pos) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.RequestAuthManager> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "request_auth_manager"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.RequestAuthManager> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC, PasswordPayloads.RequestAuthManager::pos, PasswordPayloads.RequestAuthManager::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record SubmitEnterPassword(BlockPos pos, String password) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.SubmitEnterPassword> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "submit_enter_password"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.SubmitEnterPassword> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC,
         PasswordPayloads.SubmitEnterPassword::pos,
         ByteBufCodecs.STRING_UTF8,
         PasswordPayloads.SubmitEnterPassword::password,
         PasswordPayloads.SubmitEnterPassword::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record SubmitSetupPassword(BlockPos pos, String password, boolean autoAuthOwner) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.SubmitSetupPassword> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "submit_setup_password"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.SubmitSetupPassword> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC,
         PasswordPayloads.SubmitSetupPassword::pos,
         ByteBufCodecs.STRING_UTF8,
         PasswordPayloads.SubmitSetupPassword::password,
         ByteBufCodecs.BOOL,
         PasswordPayloads.SubmitSetupPassword::autoAuthOwner,
         PasswordPayloads.SubmitSetupPassword::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record ToggleAutoAuth(BlockPos pos, boolean autoAuthOwner) implements CustomPacketPayload {
      public static final Type<PasswordPayloads.ToggleAutoAuth> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("dorp", "toggle_auto_auth"));
      public static final StreamCodec<FriendlyByteBuf, PasswordPayloads.ToggleAutoAuth> STREAM_CODEC = StreamCodec.composite(
         BlockPos.STREAM_CODEC,
         PasswordPayloads.ToggleAutoAuth::pos,
         ByteBufCodecs.BOOL,
         PasswordPayloads.ToggleAutoAuth::autoAuthOwner,
         PasswordPayloads.ToggleAutoAuth::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
