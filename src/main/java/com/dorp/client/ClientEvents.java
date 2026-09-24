package com.dorp.client;

import com.dorp.DorpMod;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

@EventBusSubscriber(modid = "dorp", bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
   @SubscribeEvent
   public static void registerRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)DorpMod.LOCKED_CHEST_BLOCK_ENTITY.get(), ChestRenderer::new);
   }
}
