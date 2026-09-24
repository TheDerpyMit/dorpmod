package com.dorp.client;

import com.dorp.PasswordPayloads;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ScreenEvent.Closing;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "dorp", bus = Bus.GAME, value = Dist.CLIENT)
public class ClientGameEvents {
   @SubscribeEvent
   public static void onScreenInit(Post event) {
      if (event.getScreen() instanceof AbstractContainerScreen<?> containerScreen && ClientAuthHandler.lastOpenedChestPos != null) {
         int guiLeft = containerScreen.getGuiLeft();
         int guiTop = containerScreen.getGuiTop();
         Button authButton = Button.builder(Component.literal("⚙ Auth"), b -> {
            if (ClientAuthHandler.lastOpenedChestPos != null) {
               PacketDistributor.sendToServer(new PasswordPayloads.RequestAuthManager(ClientAuthHandler.lastOpenedChestPos), new CustomPacketPayload[0]);
            }
         }).bounds(guiLeft + containerScreen.getXSize() - 50, guiTop - 20, 50, 20).build();
         event.addListener(authButton);
      }
   }

   @SubscribeEvent
   public static void onScreenClose(Closing event) {
      if (event.getScreen() instanceof AbstractContainerScreen && ClientAuthHandler.lastOpenedChestPos != null) {
         ClientAuthHandler.lastOpenedChestPos = null;
      }
   }
}
