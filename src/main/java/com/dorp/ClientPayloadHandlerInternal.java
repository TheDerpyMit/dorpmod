package com.dorp;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public class ClientPayloadHandlerInternal {
   public static void handleScare(ScarePayload payload, IPayloadContext context) {
      context.enqueueWork(ClientScareHandler::trigger);
   }

   public static void handleRingScare(RingScarePayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientRingHandler.triggerCameraLerp(payload.targetX(), payload.targetY(), payload.targetZ()));
   }

   public static void handleRingAmbient(RingAmbientPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientRingHandler.playAmbientSound(payload.soundId()));
   }

   public static void handleCrimsonWarning(CrimsonWarningPayload payload, IPayloadContext context) {
      context.enqueueWork(ClientRingHandler::handleCrimsonWarning);
   }

   public static void handleRingTotem(RingTotemPayload payload, IPayloadContext context) {
      context.enqueueWork(ClientRingHandler::triggerTotemAnimation);
   }

   public static void handleBsod(BsodPayload payload, IPayloadContext context) {
      context.enqueueWork(ClientBsodHandler::trigger);
   }

   public static void handleTimeStop(TimeStopPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientTimeStopHandler.start(payload.durationTicks()));
   }

   public static void handleMonsterRoar(MonsterRoarPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientMonsterRoarHandler.handle(payload.x(), payload.y(), payload.z()));
   }

   public static void handleCameraFreeze(CameraFreezePayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientCameraFreezeHandler.setFrozen(payload.freeze()));
   }

   public static void handleStopwatchTotem(StopwatchTotemPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null) {
            mc.gameRenderer.displayItemActivation(new ItemStack((ItemLike)DorpMod.DIEGO_STOPWATCH.get()));
            ClientMonsterRoarHandler.handle(mc.player.getX(), -10000.0, mc.player.getZ());
         }
      });
   }

   public static void handleRadioChat(RadioChatPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> RadioClientMessageLog.addMessage(payload.frequency(), payload.senderName(), payload.message()));
   }

   public static void handleElytraWarning(ElytraWarningPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         mc.setScreen(new ElytraWarningScreen());
      });
   }

   public static void handleScannedInventory(ScannedInventoryPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.screen instanceof InventoryCheckerScreen screen) {
            screen.receiveScannedData(payload.playerName(), payload.inventoryData());
         }
      });
   }
}
