package com.dorp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre;

@EventBusSubscriber(modid = "dorp", value = Dist.CLIENT)
public class ClientCameraFreezeHandler {
   private static boolean isFrozen = false;
   private static float lockedYaw = 0.0F;
   private static float lockedPitch = 0.0F;

   public static void resetState() {
      isFrozen = false;
      lockedYaw = 0.0F;
      lockedPitch = 0.0F;
   }

   public static void setFrozen(boolean freeze) {
      if (freeze && !isFrozen) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            lockedYaw = player.getYRot();
            lockedPitch = player.getXRot();
         }
      }

      isFrozen = freeze;
   }

   @SubscribeEvent
   public static void onMovementInput(MovementInputUpdateEvent event) {
      if (isFrozen) {
         event.getInput().forwardImpulse = 0.0F;
         event.getInput().leftImpulse = 0.0F;
         event.getInput().up = false;
         event.getInput().down = false;
         event.getInput().left = false;
         event.getInput().right = false;
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(Pre event) {
      if (isFrozen) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null && event.getEntity() == player) {
            player.setYRot(lockedYaw);
            player.setXRot(lockedPitch);
            player.yRotO = lockedYaw;
            player.xRotO = lockedPitch;
            player.setYHeadRot(lockedYaw);
            player.yHeadRotO = lockedYaw;
         }
      }
   }
}
