package com.dorp;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.common.NeoForge;

@OnlyIn(Dist.CLIENT)
public class ClientDisconnectHandler {
   public static void register() {
      NeoForge.EVENT_BUS.addListener(ClientDisconnectHandler::onLoggingOut);
   }

   private static void onLoggingOut(LoggingOut event) {
      ClientRingHandler.resetState();
      ClientCameraFreezeHandler.resetState();
      ClientBsodHandler.resetState();
      ClientScareHandler.resetState();
   }
}
