package com.dorp;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Post;
import net.neoforged.neoforge.common.NeoForge;

@OnlyIn(Dist.CLIENT)
public class ClientBombVestHandler {
   public static void register() {
      NeoForge.EVENT_BUS.addListener(ClientBombVestHandler::onRenderPlayerPost);
   }

   private static void onRenderPlayerPost(Post event) {
   }
}
