package com.dorp;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@OnlyIn(Dist.CLIENT)
public class ClientPayloadHandler {
   public static void registerClientPayloads(PayloadRegistrar registrar) {
      registrar.playToClient(ScarePayload.TYPE, ScarePayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleScare);
      registrar.playToClient(RingScarePayload.TYPE, RingScarePayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleRingScare);
      registrar.playToClient(RingAmbientPayload.TYPE, RingAmbientPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleRingAmbient);
      registrar.playToClient(CrimsonWarningPayload.TYPE, CrimsonWarningPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleCrimsonWarning);
      registrar.playToClient(RingTotemPayload.TYPE, RingTotemPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleRingTotem);
      registrar.playToClient(BsodPayload.TYPE, BsodPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleBsod);
      registrar.playToClient(TimeStopPayload.TYPE, TimeStopPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleTimeStop);
      registrar.playToClient(MonsterRoarPayload.TYPE, MonsterRoarPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleMonsterRoar);
      registrar.playToClient(CameraFreezePayload.TYPE, CameraFreezePayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleCameraFreeze);
      registrar.playToClient(StopwatchTotemPayload.TYPE, StopwatchTotemPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleStopwatchTotem);
      registrar.playToClient(RadioChatPayload.TYPE, RadioChatPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleRadioChat);
      registrar.playToClient(ElytraWarningPayload.TYPE, ElytraWarningPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleElytraWarning);
      registrar.playToClient(ScannedInventoryPayload.TYPE, ScannedInventoryPayload.STREAM_CODEC, ClientPayloadHandlerInternal::handleScannedInventory);
   }
}
