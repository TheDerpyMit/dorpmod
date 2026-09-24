package com.dorp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;
import net.neoforged.neoforge.client.event.ScreenEvent.Render.Pre;

@EventBusSubscriber(modid = "dorp", value = Dist.CLIENT)
public class ClientForbiddenWorkbenchHandler {
   @SubscribeEvent
   public static void onScreenInit(Post event) {
      if (event.getScreen() instanceof CraftingScreen craftingScreen
         && craftingScreen.getTitle().getString().equals(Component.translatable("container.dorp.forbidden_workbench").getString())) {
         int left = craftingScreen.getGuiLeft();
         int top = craftingScreen.getGuiTop();
         event.addListener(
            Button.builder(Component.literal("Book"), button -> Minecraft.getInstance().setScreen(new ForbiddenWorkbenchRecipeBookScreen(craftingScreen)))
               .bounds(left + 142, top + 4, 30, 18)
               .build()
         );
      }
   }

   @SubscribeEvent
   public static void onScreenRenderPre(Pre event) {
      if (event.getScreen() instanceof CraftingScreen craftingScreen
         && craftingScreen.getTitle().getString().equals(Component.translatable("container.dorp.forbidden_workbench").getString())) {
         int left = craftingScreen.getGuiLeft();
         int top = craftingScreen.getGuiTop();

         for (GuiEventListener child : craftingScreen.children()) {
            if (child instanceof Button button && button.getMessage().getString().equals("Book")) {
               button.setX(left + 142);
               button.setY(top + 4);
            }
         }
      }
   }
}
