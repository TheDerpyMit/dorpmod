package com.dorp.client;

import com.dorp.OpenAuthManagerPayload;
import com.dorp.PasswordPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientAuthHandler {
   public static BlockPos lastOpenedChestPos = null;

   public static void handleOpenPrompt(OpenAuthManagerPayload payload, IPayloadContext context) {
      context.enqueueWork(
         () -> Minecraft.getInstance()
            .setScreen(new AuthManagerScreen(payload.pos(), payload.trustedPlayers(), payload.serverPlayers(), payload.autoAuthOwner()))
      );
   }

   public static void handleOpenSetupPassword(PasswordPayloads.OpenSetupPassword payload, IPayloadContext context) {
      context.enqueueWork(() -> Minecraft.getInstance().setScreen(new SetupPasswordScreen(payload.pos())));
   }

   public static void handleOpenEnterPassword(PasswordPayloads.OpenEnterPassword payload, IPayloadContext context) {
      context.enqueueWork(() -> Minecraft.getInstance().setScreen(new EnterPasswordScreen(payload.pos())));
   }

   public static void handleActiveLockedChest(PasswordPayloads.ActiveLockedChest payload, IPayloadContext context) {
      context.enqueueWork(() -> lastOpenedChestPos = payload.pos());
   }
}
