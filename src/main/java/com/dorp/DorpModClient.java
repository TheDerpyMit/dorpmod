package com.dorp;

import com.dorp.client.BreachHackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class DorpModClient {
    public static void init(IEventBus modEventBus) {
      ClientScareHandler.register(modEventBus);
      ClientPaperBagHandler.register(modEventBus);
      ClientHeadsetHandler.register(modEventBus);
      ClientRingHandler.register(modEventBus);
      ClientBsodHandler.register(modEventBus);
      ClientDisconnectHandler.register();
      ClientBombVestHandler.register();
      ClientSledgehammerHandler.register();
   modEventBus.addListener(DorpModClient::onClientSetup);
   modEventBus.addListener(DorpModClient::onRegisterRenderers);
   modEventBus.addListener(DorpModClient::onRegisterClientExtensions);
   }

   private static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         CuriosRendererRegistry.register((Item)DorpMod.HEADSET.get(), HeadsetRenderer::new);
         CuriosRendererRegistry.register((Item)DorpMod.HEAD_TORCH.get(), HeadsetRenderer::new);
         CuriosRendererRegistry.register((Item)DorpMod.CRIMSON_THREAD.get(), RingRenderer::new);
         CuriosRendererRegistry.register((Item)DorpMod.IRON_RING.get(), RingRenderer::new);
      });
   }

   private static void onRegisterRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)DorpMod.RADIO_BLOCK_ENTITY.get(), RadioBlockEntityRenderer::new);
      event.registerEntityRenderer((EntityType)DorpMod.CRIMSON_PHANTOM.get(), CrimsonPhantomRenderer::new);
      event.registerEntityRenderer((EntityType)DorpMod.TEST_PLAYER_ENTITY.get(), TestPlayerRenderer::new);
   }

   private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
      event.registerItem(new IClientItemExtensions() {
         @Override
         public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return HimariVisorRenderer.GEO_RENDERER;
         }

         @Override
         public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(
               net.minecraft.world.entity.LivingEntity entity,
               net.minecraft.world.item.ItemStack stack,
               net.minecraft.world.entity.EquipmentSlot slot,
               net.minecraft.client.model.HumanoidModel<?> original) {
            HimariVisorItem.currentMood = HimariVisorItem.moodFor(entity);
            HimariVisorArmorRenderer.INSTANCE.prepForRender(entity, stack, slot, original);
            return HimariVisorArmorRenderer.INSTANCE;
         }
      }, (Item)DorpMod.HIMARI_VISOR.get());
   }

   public static void openRadioScreen(RadioBlockEntity radio) {
      Minecraft.getInstance().setScreen(new RadioScreen(radio));
   }

   public static void openInterceptorScreen(InterceptorBlockEntity interceptor) {
      Minecraft.getInstance().setScreen(new InterceptorScreen(interceptor));
   }

   public static void openBreachHackScreen(BlockPos pos) {
      Minecraft.getInstance().setScreen(new BreachHackScreen(pos));
   }

   public static void openGuideBookScreen() {
      Minecraft.getInstance().setScreen(new ForbiddenWorkbenchRecipeBookScreen(Minecraft.getInstance().screen));
   }

   public static void openInventoryCheckerScreen(BlockPos pos) {
      Minecraft.getInstance().setScreen(new InventoryCheckerScreen(pos));
   }
}
