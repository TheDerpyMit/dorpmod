package com.dorp;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

@OnlyIn(Dist.CLIENT)
public class ClientHeadsetHandler {
   public static final KeyMapping CHANGE_FREQUENCY_KEY = new KeyMapping("key.dorp.change_headset_frequency", Type.KEYSYM, 75, "key.categories.dorp");

   public static void register(IEventBus modEventBus) {
      modEventBus.addListener(ClientHeadsetHandler::onRegisterKeyMappings);
      NeoForge.EVENT_BUS.addListener(ClientHeadsetHandler::onClientTick);
   }

   private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
      event.register(CHANGE_FREQUENCY_KEY);
   }

   private static void onClientTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.screen == null) {
         while (CHANGE_FREQUENCY_KEY.consumeClick()) {
            boolean hasHeadset = CuriosApi.getCuriosInventory(mc.player).flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEADSET.get())).isPresent();
            if (hasHeadset) {
               mc.setScreen(new HeadsetScreen());
            } else {
               boolean hasHeadTorch = CuriosApi.getCuriosInventory(mc.player)
                  .flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEAD_TORCH.get()))
                  .isPresent();
               if (hasHeadTorch) {
                  PacketDistributor.sendToServer(new ToggleHeadTorchPayload(), new CustomPacketPayload[0]);
               }
            }
         }
      }
   }
}
