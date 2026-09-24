package com.dorp;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Post;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;

@OnlyIn(Dist.CLIENT)
public class ClientPaperBagHandler {
   private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("dorp", "textures/misc/overlay.png");

   public static void register(IEventBus modEventBus) {
      modEventBus.addListener(ClientPaperBagHandler::onRegisterGuiLayers);
      NeoForge.EVENT_BUS.addListener(ClientPaperBagHandler::onRenderPlayerPre);
      NeoForge.EVENT_BUS.addListener(ClientPaperBagHandler::onRenderPlayerPost);
      NeoForge.EVENT_BUS.addListener(ClientPaperBagHandler::onRenderNameTag);
      NeoForge.EVENT_BUS.addListener(ClientPaperBagHandler::onRightClickItem);
   }

   private static void onRightClickItem(RightClickItem event) {
      if (event.getLevel().isClientSide()) {
         if (event.getItemStack().is((Item)DorpMod.CONCEALER_HEAD.get())) {
            Minecraft.getInstance().setScreen(new ConcealerHeadScreen());
            event.setCanceled(true);
         } else if (event.getItemStack().getItem() instanceof WalkieTalkieItem && !event.getEntity().isShiftKeyDown()) {
            Minecraft.getInstance().setScreen(new WalkieTalkieScreen(event.getHand()));
            event.setCanceled(true);
         }
      }
   }

   private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
      event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath("dorp", "paperbag_overlay"), ClientPaperBagHandler::renderOverlay);
   }

   private static void renderOverlay(GuiGraphics gui, DeltaTracker dt) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && isWearingBag(mc.player) && mc.options.getCameraType().isFirstPerson()) {
         int width = mc.getWindow().getGuiScaledWidth();
         int height = mc.getWindow().getGuiScaledHeight();
         gui.blit(OVERLAY_TEXTURE, 0, 0, 0.0F, 0.0F, width, height, width, height);
      }
   }

   private static boolean isWearingBag(Player player) {
      return player.getItemBySlot(EquipmentSlot.HEAD).is((Item)DorpMod.PAPER_BAG.get());
   }

   private static boolean hasConcealedName(Player player) {
      ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
      if (headStack.is((Item)DorpMod.PAPER_BAG.get())) {
         return true;
      } else if (!headStack.is(Items.PLAYER_HEAD)) {
         return false;
      } else {
         CustomData customData = (CustomData)headStack.get(DataComponents.CUSTOM_DATA);
         return customData != null && customData.copyTag().getBoolean("concealer");
      }
   }

   private static void onRenderPlayerPre(Pre event) {
      if (isWearingBag(event.getEntity())) {
         PlayerModel<?> model = (PlayerModel<?>)event.getRenderer().getModel();
         model.head.visible = false;
         model.hat.visible = false;
      }
   }

   private static void onRenderPlayerPost(Post event) {
      if (isWearingBag(event.getEntity())) {
         PlayerModel<?> model = (PlayerModel<?>)event.getRenderer().getModel();
         model.head.visible = true;
         model.hat.visible = true;
      }
   }

   private static void onRenderNameTag(RenderNameTagEvent event) {
      if (event.getEntity() instanceof Player player && hasConcealedName(player)) {
         event.setCanRender(TriState.FALSE);
      }
   }
}
