package com.dorp;

import com.dorp.config.DorpConfig;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class ChatHandler {
   private static final Map<UUID, Long> SHOUT_COOLDOWNS = new HashMap<>();
   public static final Set<RadioBlockEntity> ACTIVE_RADIOS = Collections.newSetFromMap(new WeakHashMap<>());

   private static Component buildProximityMessage(ServerPlayer sender, String rawText, double distanceSq, double maxRangeSq, boolean isWhisper) {
      String distorted = DistortionUtil.distort(rawText, distanceSq, maxRangeSq);
      return isWhisper
         ? Component.empty()
            .append(Component.literal("[Whisper] ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("<"))
            .append(sender.getDisplayName())
            .append(Component.literal("> " + distorted).withStyle(ChatFormatting.WHITE))
         : Component.empty()
            .append(Component.literal("<"))
            .append(sender.getDisplayName())
            .append(Component.literal("> " + distorted).withStyle(ChatFormatting.WHITE));
   }

   private static Component buildWalkieMessage(ServerPlayer sender, String rawText, double distanceSq, double maxRangeSq) {
      String distorted = DistortionUtil.distort(rawText, distanceSq, maxRangeSq);
      return Component.empty()
         .append(Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.GREEN))
         .append(Component.literal("<"))
         .append(sender.getDisplayName())
         .append(Component.literal("> " + distorted).withStyle(ChatFormatting.WHITE));
   }

   private static Component buildHeadsetMessage(ServerPlayer sender, String rawText, double distanceSq, double maxRangeSq) {
      String distorted = DistortionUtil.distort(rawText, distanceSq, maxRangeSq);
      return Component.empty()
         .append(Component.literal("[Headset] ").withStyle(ChatFormatting.BLUE))
         .append(Component.literal("<"))
         .append(sender.getDisplayName())
         .append(Component.literal("> " + distorted).withStyle(ChatFormatting.WHITE));
   }

   private static Component buildInterceptorMessage(String rawText, double distanceSq, double maxRangeSq) {
      String distorted = DistortionUtil.distort(rawText, distanceSq, maxRangeSq);
      return Component.empty()
         .append(Component.literal("[Interceptor]: ").withStyle(ChatFormatting.GRAY))
         .append(Component.literal(distorted).withStyle(ChatFormatting.WHITE));
   }

   public static void onServerChat(ServerChatEvent event) {
      ServerPlayer sender = event.getPlayer();
      String rawText = event.getRawText();
      Level level = sender.level();
      double whisperRange = DorpConfig.PROXIMITY_RANGE_WHISPER.get();
      double normalRange = DorpConfig.PROXIMITY_RANGE_NORMAL.get();
      double radioTrans = DorpConfig.RADIO_BLOCK_TRANSMISSION_RANGE.get();
      double radioRec = DorpConfig.RADIO_BLOCK_RECEPTION_RANGE.get();
      double walkieTalkieRange = DorpConfig.WALKIE_TALKIE_RANGE.get();
      double interceptorRange = DorpConfig.INTERCEPTOR_RANGE.get();
      double interceptorReceiveRange = DorpConfig.INTERCEPTOR_RECEIVE_RANGE.get();
      double RADIO_BLOCK_TRANSMISSION_RANGE_SQ = radioTrans * radioTrans;
      double RADIO_BLOCK_RECEPTION_RANGE_SQ = radioRec * radioRec;
      double WALKIE_TALKIE_RANGE_SQ = walkieTalkieRange * walkieTalkieRange;
      boolean isWhisper = sender.isShiftKeyDown();
      double currentProximityRangeSq = isWhisper ? whisperRange * whisperRange : normalRange * normalRange;
      RadioBlockEntity nearbyRadio = null;

      for (RadioBlockEntity radio : ACTIVE_RADIOS) {
         if (radio.getLevel() == level
            && !radio.isRemoved()
            && radio.isActive()
            && DistanceHelper.distanceSq(level, radio.getBlockPos().getCenter(), sender.position()) <= RADIO_BLOCK_TRANSMISSION_RANGE_SQ) {
            nearbyRadio = radio;
            break;
         }
      }

      ItemStack activeTalkie = null;
      ItemStack activeHeadset = null;
      if (nearbyRadio == null) {
         ItemStack mainHand = sender.getMainHandItem();
         ItemStack offHand = sender.getOffhandItem();
         if (mainHand.getItem() instanceof WalkieTalkieItem && WalkieTalkieItem.isActive(mainHand)) {
            activeTalkie = mainHand;
         } else if (offHand.getItem() instanceof WalkieTalkieItem && WalkieTalkieItem.isActive(offHand)) {
            activeTalkie = offHand;
         }

         if (activeTalkie == null) {
            Optional<SlotResult> slotResultOpt = CuriosApi.getCuriosInventory(sender).flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEADSET.get()));
            if (slotResultOpt.isPresent()) {
               ItemStack headsetStack = slotResultOpt.get().stack();
               if (HeadsetItem.isActive(headsetStack)) {
                  activeHeadset = headsetStack;
               }
            }
         }
      }

      event.setCanceled(true);
      if (nearbyRadio != null) {
         int freq = nearbyRadio.getFrequency();
         Component radioMsg = Component.empty()
            .append(Component.literal("[Radio] ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("<"))
            .append(sender.getDisplayName())
            .append(Component.literal("> " + rawText).withStyle(ChatFormatting.WHITE));
         sender.sendSystemMessage(radioMsg);

         for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
            if (player != sender) {
               boolean received = false;
               double distSq = DistanceHelper.distanceSq(level, sender.position(), player.position());
               if (player.level() == level && distSq <= currentProximityRangeSq) {
                  player.sendSystemMessage(buildProximityMessage(sender, rawText, distSq, currentProximityRangeSq, isWhisper));
                  received = true;
               }

               if (!received && hasActiveWalkieTalkieOrHeadsetOnFrequency(player, freq)) {
                  player.sendSystemMessage(radioMsg);
                  received = true;
               }

               if (!received) {
                  for (RadioBlockEntity radiox : ACTIVE_RADIOS) {
                     if (!radiox.isRemoved() && radiox.isActive() && radiox.getFrequency() == freq) {
                        if (radiox instanceof DebugRadioBlockEntity) {
                           player.sendSystemMessage(radioMsg);
                           received = true;
                           break;
                        }

                        if (radiox.getLevel() == player.level()
                           && DistanceHelper.distanceSq(level, radiox.getBlockPos().getCenter(), player.position()) <= RADIO_BLOCK_RECEPTION_RANGE_SQ) {
                           player.sendSystemMessage(radioMsg);
                           received = true;
                           break;
                        }
                     }
                  }
               }
            }
         }

         sendToNearbyRadioClients(sender.server, freq, Events.getCustomDisplayNameString(sender), rawText);
      } else if (activeTalkie != null) {
         int freq = WalkieTalkieItem.getFrequency(activeTalkie);
         int currentEnergy = WalkieTalkieItem.getEnergy(activeTalkie);
         int cost = DorpConfig.WALKIE_TALKIE_ENERGY_PER_TX.get();
         WalkieTalkieItem.setEnergy(activeTalkie, currentEnergy - cost);
         sender.sendSystemMessage(buildWalkieMessage(sender, rawText, 0.0, WALKIE_TALKIE_RANGE_SQ));

         for (ServerPlayer playerx : sender.server.getPlayerList().getPlayers()) {
            if (playerx != sender) {
               boolean receivedx = false;
               double distSqx = DistanceHelper.distanceSq(level, sender.position(), playerx.position());
               if (playerx.level() == level && distSqx <= currentProximityRangeSq) {
                  playerx.sendSystemMessage(buildProximityMessage(sender, rawText, distSqx, currentProximityRangeSq, isWhisper));
                  receivedx = true;
               }

               if (!receivedx && playerx.level() == level && distSqx <= WALKIE_TALKIE_RANGE_SQ && hasActiveWalkieTalkieOrHeadsetOnFrequency(playerx, freq)) {
                  playerx.sendSystemMessage(buildWalkieMessage(sender, rawText, distSqx, WALKIE_TALKIE_RANGE_SQ));
                  receivedx = true;
               }

               if (!receivedx) {
                  for (RadioBlockEntity radioxx : ACTIVE_RADIOS) {
                     if (!radioxx.isRemoved() && radioxx.isActive() && radioxx.getFrequency() == freq) {
                        if (radioxx instanceof DebugRadioBlockEntity) {
                           playerx.sendSystemMessage(buildWalkieMessage(sender, rawText, 0.0, WALKIE_TALKIE_RANGE_SQ));
                           receivedx = true;
                           break;
                        }

                        if (radioxx.getLevel() == playerx.level()
                           && DistanceHelper.distanceSq(level, radioxx.getBlockPos().getCenter(), playerx.position()) <= RADIO_BLOCK_RECEPTION_RANGE_SQ) {
                           playerx.sendSystemMessage(buildWalkieMessage(sender, rawText, 0.0, WALKIE_TALKIE_RANGE_SQ));
                           receivedx = true;
                           break;
                        }
                     }
                  }
               }
            }
         }

         for (InterceptorBlockEntity interceptor : InterceptorBlockEntity.ACTIVE_INTERCEPTORS) {
            if (interceptor.getLevel() == level && !interceptor.isRemoved() && interceptor.isActive()) {
               double distToInterceptorSq = DistanceHelper.distanceSq(level, interceptor.getBlockPos().getCenter(), sender.position());
               if (distToInterceptorSq <= interceptorRange * interceptorRange && interceptor.consumeEnergyForMessage()) {
                  Component interceptorMsg = buildInterceptorMessage(rawText, distToInterceptorSq, interceptorRange * interceptorRange);
                  if (interceptor instanceof DebugInterceptorBlockEntity) {
                     for (ServerPlayer playerxx : sender.server.getPlayerList().getPlayers()) {
                        playerxx.sendSystemMessage(interceptorMsg);
                     }
                  } else {
                     for (ServerPlayer playerxx : sender.server.getPlayerList().getPlayers()) {
                        if (playerxx.level() == level
                           && DistanceHelper.distanceSq(level, playerxx.position(), interceptor.getBlockPos().getCenter())
                              <= interceptorReceiveRange * interceptorReceiveRange) {
                           playerxx.sendSystemMessage(interceptorMsg);
                        }
                     }
                  }
               }
            }
         }

         sendToNearbyRadioClients(sender.server, freq, Events.getCustomDisplayNameString(sender), rawText);
      } else if (activeHeadset != null) {
         int freq = HeadsetItem.getFrequency(activeHeadset);
         int currentEnergy = HeadsetItem.getEnergy(activeHeadset);
         int cost = DorpConfig.HEADSET_ENERGY_PER_TX.get();
         HeadsetItem.setEnergy(activeHeadset, currentEnergy - cost);
         double headsetConfigRange = DorpConfig.HEADSET_RANGE.get();
         double HEADSET_RANGE_SQ = headsetConfigRange * headsetConfigRange;
         sender.sendSystemMessage(buildHeadsetMessage(sender, rawText, 0.0, HEADSET_RANGE_SQ));

         for (ServerPlayer playerxxx : sender.server.getPlayerList().getPlayers()) {
            if (playerxxx != sender) {
               boolean receivedxx = false;
               double distSqxx = DistanceHelper.distanceSq(level, sender.position(), playerxxx.position());
               if (playerxxx.level() == level && distSqxx <= currentProximityRangeSq) {
                  playerxxx.sendSystemMessage(buildProximityMessage(sender, rawText, distSqxx, currentProximityRangeSq, isWhisper));
                  receivedxx = true;
               }

               if (!receivedxx && playerxxx.level() == level && distSqxx <= HEADSET_RANGE_SQ && hasActiveWalkieTalkieOrHeadsetOnFrequency(playerxxx, freq)) {
                  playerxxx.sendSystemMessage(buildHeadsetMessage(sender, rawText, distSqxx, HEADSET_RANGE_SQ));
                  receivedxx = true;
               }

               if (!receivedxx) {
                  for (RadioBlockEntity radioxxx : ACTIVE_RADIOS) {
                     if (!radioxxx.isRemoved() && radioxxx.isActive() && radioxxx.getFrequency() == freq) {
                        if (radioxxx instanceof DebugRadioBlockEntity) {
                           playerxxx.sendSystemMessage(buildHeadsetMessage(sender, rawText, 0.0, HEADSET_RANGE_SQ));
                           receivedxx = true;
                           break;
                        }

                        if (radioxxx.getLevel() == playerxxx.level()
                           && DistanceHelper.distanceSq(level, radioxxx.getBlockPos().getCenter(), playerxxx.position()) <= RADIO_BLOCK_RECEPTION_RANGE_SQ) {
                           playerxxx.sendSystemMessage(buildHeadsetMessage(sender, rawText, 0.0, HEADSET_RANGE_SQ));
                           receivedxx = true;
                           break;
                        }
                     }
                  }
               }
            }
         }

         Component interceptorMsg = Component.empty()
            .append(Component.literal("[Interceptor]: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(rawText).withStyle(ChatFormatting.WHITE));

         for (InterceptorBlockEntity interceptorx : InterceptorBlockEntity.ACTIVE_INTERCEPTORS) {
            if (interceptorx.getLevel() == level
               && !interceptorx.isRemoved()
               && interceptorx.isActive()
               && DistanceHelper.distanceSq(level, interceptorx.getBlockPos().getCenter(), sender.position()) <= interceptorRange * interceptorRange
               && interceptorx.consumeEnergyForMessage()) {
               if (interceptorx instanceof DebugInterceptorBlockEntity) {
                  for (ServerPlayer playerxxxx : sender.server.getPlayerList().getPlayers()) {
                     playerxxxx.sendSystemMessage(interceptorMsg);
                  }
               } else {
                  for (ServerPlayer playerxxxx : sender.server.getPlayerList().getPlayers()) {
                     if (playerxxxx.level() == level
                        && DistanceHelper.distanceSq(level, playerxxxx.position(), interceptorx.getBlockPos().getCenter())
                           <= interceptorReceiveRange * interceptorReceiveRange) {
                        playerxxxx.sendSystemMessage(interceptorMsg);
                     }
                  }
               }
            }
         }

         sendToNearbyRadioClients(sender.server, freq, Events.getCustomDisplayNameString(sender), rawText);
      } else {
         sender.sendSystemMessage(buildProximityMessage(sender, rawText, 0.0, currentProximityRangeSq, isWhisper));

         for (ServerPlayer playerxxxxx : sender.server.getPlayerList().getPlayers()) {
            if (playerxxxxx != sender && playerxxxxx.level() == level) {
               double distanceSq = DistanceHelper.distanceSq(level, sender.position(), playerxxxxx.position());
               if (distanceSq <= currentProximityRangeSq) {
                  playerxxxxx.sendSystemMessage(buildProximityMessage(sender, rawText, distanceSq, currentProximityRangeSq, isWhisper));
               }
            }
         }
      }
   }

   public static void onCommand(CommandEvent event) {
      String rawCommand = event.getParseResults().getReader().getString();
      if (rawCommand != null) {
         String cmd = rawCommand.trim();
         if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
         }

         String lower = cmd.toLowerCase();
         String[] parts = lower.split("\\s+");
         if (parts.length > 0) {
            String firstWord = parts[0];
            String cleanFirst = firstWord;
            if (firstWord.contains(":")) {
               cleanFirst = firstWord.substring(firstWord.indexOf(":") + 1);
            }

            if (cleanFirst.equals("msg") || cleanFirst.equals("tell") || cleanFirst.equals("teammsg") || cleanFirst.equals("tm")) {
               event.setCanceled(true);
               if (((CommandSourceStack)event.getParseResults().getContext().getSource()).getEntity() instanceof ServerPlayer player) {
                  player.sendSystemMessage(
                     Component.literal("Private messaging is disabled on this server to enforce proximity chat!").withStyle(ChatFormatting.RED)
                  );
               }
            }
         }
      }
   }

   private static void sendToNearbyRadioClients(MinecraftServer server, int freq, String senderName, String rawText) {
      RadioChatPayload payload = new RadioChatPayload(freq, senderName, rawText);
      double radioRec = DorpConfig.RADIO_BLOCK_RECEPTION_RANGE.get();
      double RADIO_BLOCK_RECEPTION_RANGE_SQ = radioRec * radioRec;

      for (RadioBlockEntity radio : ACTIVE_RADIOS) {
         if (!radio.isRemoved() && radio.isActive() && radio.getFrequency() == freq) {
            Level radioLevel = radio.getLevel();
            if (radioLevel != null) {
               for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                  if (player.level() == radioLevel
                     && DistanceHelper.distanceSq(radioLevel, radio.getBlockPos().getCenter(), player.position()) <= RADIO_BLOCK_RECEPTION_RANGE_SQ) {
                     PacketDistributor.sendToPlayer(player, payload, new CustomPacketPayload[0]);
                  }
               }
            }
         }
      }
   }

   private static boolean hasActiveWalkieTalkieOnFrequency(ServerPlayer player, int frequency) {
      for (ItemStack stack : player.getInventory().items) {
         if (stack.getItem() instanceof WalkieTalkieItem && WalkieTalkieItem.isActive(stack) && WalkieTalkieItem.getFrequency(stack) == frequency) {
            return true;
         }
      }

      ItemStack offhand = player.getOffhandItem();
      if (offhand.getItem() instanceof WalkieTalkieItem && WalkieTalkieItem.isActive(offhand) && WalkieTalkieItem.getFrequency(offhand) == frequency) {
         return true;
      } else {
         for (ItemStack armor : player.getInventory().armor) {
            if (armor.getItem() instanceof WalkieTalkieItem && WalkieTalkieItem.isActive(armor) && WalkieTalkieItem.getFrequency(armor) == frequency) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean hasActiveWalkieTalkieOrHeadsetOnFrequency(ServerPlayer player, int frequency) {
      if (hasActiveWalkieTalkieOnFrequency(player, frequency)) {
         return true;
      } else {
         Optional<SlotResult> slotResultOpt = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio((Item)DorpMod.HEADSET.get()));
         if (!slotResultOpt.isPresent()) {
            return false;
         } else {
            ItemStack headsetStack = slotResultOpt.get().stack();
            return HeadsetItem.isActive(headsetStack) && HeadsetItem.getFrequency(headsetStack) == frequency;
         }
      }
   }

   public static void onRegisterCommands(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("shout")
                  .executes(
                     context -> {
                        ((CommandSourceStack)context.getSource())
                           .getPlayerOrException()
                           .sendSystemMessage(Component.literal("Usage: /shout <message>").withStyle(ChatFormatting.RED));
                        return 1;
                     }
                  ))
               .then(Commands.argument("message", StringArgumentType.greedyString()).executes(context -> {
                  ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                  String msg = StringArgumentType.getString(context, "message");
                  handleShoutCommand(player, msg);
                  return 1;
               }))
         );
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("whisper")
                  .executes(
                     context -> {
                        ((CommandSourceStack)context.getSource())
                           .getPlayerOrException()
                           .sendSystemMessage(Component.literal("Usage: /whisper <message>").withStyle(ChatFormatting.RED));
                        return 1;
                     }
                  ))
               .then(Commands.argument("message", StringArgumentType.greedyString()).executes(context -> {
                  ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                  String msg = StringArgumentType.getString(context, "message");
                  handleWhisperCommand(player, msg);
                  return 1;
               }))
         );
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("w")
                  .executes(
                     context -> {
                        ((CommandSourceStack)context.getSource())
                           .getPlayerOrException()
                           .sendSystemMessage(Component.literal("Usage: /w <message>").withStyle(ChatFormatting.RED));
                        return 1;
                     }
                  ))
               .then(Commands.argument("message", StringArgumentType.greedyString()).executes(context -> {
                  ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                  String msg = StringArgumentType.getString(context, "message");
                  handleWhisperCommand(player, msg);
                  return 1;
               }))
         );
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("name")
                           .executes(context -> {
                              ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("Usage: /name set <name>"));
                              return 0;
                           }))
                        .then(Commands.literal("set").then(Commands.argument("new_name", StringArgumentType.greedyString()).executes(context -> {
                           ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                           String newName = StringArgumentType.getString(context, "new_name");
                           return handleSetName((CommandSourceStack)context.getSource(), player, newName);
                        }))))
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("setplayer").requires(source -> source.hasPermission(2)))
                           .then(
                              Commands.argument("target", EntityArgument.player())
                                 .then(Commands.argument("new_name", StringArgumentType.greedyString()).executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                    String newName = StringArgumentType.getString(context, "new_name");
                                    return handleSetName((CommandSourceStack)context.getSource(), target, newName);
                                 }))
                           )
                     ))
                  .then(((LiteralArgumentBuilder)Commands.literal("clear").requires(source -> source.hasPermission(2))).executes(context -> {
                     ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                     return handleClearName((CommandSourceStack)context.getSource(), player);
                  })))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("clearplayer").requires(source -> source.hasPermission(2)))
                     .then(Commands.argument("target", EntityArgument.player()).executes(context -> {
                        ServerPlayer target = EntityArgument.getPlayer(context, "target");
                        return handleClearName((CommandSourceStack)context.getSource(), target);
                     }))
               )
         );
      event.getDispatcher().register(buildDebugCommand("dorpdebug"));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> buildDebugCommand(String name) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                              name
                           )
                           .then(((LiteralArgumentBuilder)Commands.literal("items").requires(source -> source.hasPermission(2))).executes(context -> {
                              ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                              player.getInventory().add(new ItemStack((ItemLike)DorpMod.DEBUG_RADIO.get()));
                              player.getInventory().add(new ItemStack((ItemLike)DorpMod.DEBUG_INTERCEPTOR.get()));
                              player.sendSystemMessage(Component.literal("Given Debug Radio and Interceptor").withStyle(ChatFormatting.GREEN));
                              return 1;
                           })))
                        .then(
                           ((LiteralArgumentBuilder)Commands.literal("cooldowns").requires(source -> source.hasPermission(2)))
                              .then(Commands.literal("reset").executes(context -> {
                                 ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();

                                 for (DeferredHolder<Item, ? extends Item> entry : DorpMod.ITEMS.getEntries()) {
                                    player.getCooldowns().removeCooldown((Item)entry.get());
                                 }

                                 player.sendSystemMessage(Component.literal("Cleared all Dorp Mod item cooldowns!").withStyle(ChatFormatting.GREEN));
                                 return 1;
                              }))
                        ))
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("ring").requires(source -> source.hasPermission(2)))
                           .then(Commands.literal("reset").executes(context -> {
                              ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                              player.getPersistentData().remove("CrimsonSleepDeprived");
                              player.getPersistentData().remove("CrimsonPendingJumpscare");
                              player.getPersistentData().remove("CrimsonJumpscareSpawned");
                              player.getPersistentData().remove("CrimsonJumpscareTime");

                              for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                 ItemStack s = player.getInventory().getItem(i);
                                 if (s.is((Item)DorpMod.CRIMSON_THREAD.get())) {
                                    CrimsonThreadItem.setUses(s, 0);
                                 }
                              }

                              Optional<ICuriosItemHandler> curioInvOpt = CuriosApi.getCuriosInventory(player);
                              if (curioInvOpt.isPresent()) {
                                 for (SlotResult slotResult : curioInvOpt.get().findCurios((Item)DorpMod.CRIMSON_THREAD.get())) {
                                    CrimsonThreadItem.setUses(slotResult.stack(), 0);
                                 }
                              }

                              player.sendSystemMessage(Component.literal("Reset all ring states and item uses!").withStyle(ChatFormatting.GREEN));
                              return 1;
                           }))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("cigarette").requires(source -> source.hasPermission(2)))
                        .then(Commands.literal("reset").executes(context -> {
                           ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                           CompoundTag persist = player.getPersistentData();
                           persist.remove("CigaretteSmokeTimes");
                           persist.remove("CigarettesSmokedToday");
                           persist.remove("CigaretteLastSmokeDay");
                           persist.remove("CigaretteHeavyDays");
                           persist.remove("CigaretteWithdrawalTicks");
                           persist.remove("CigaretteProneTicks");
                           persist.remove("TicksSinceLastSmoke");
                           player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder((MobEffect)DorpMod.SMOKING_ADDICTION.get()));
                           player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                           player.removeEffect(MobEffects.DIG_SLOWDOWN);
                           player.removeEffect(MobEffects.POISON);
                           player.removeEffect(MobEffects.BLINDNESS);
                           player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder((MobEffect)DorpMod.NICOTINE_RUSH.get()));
                           player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder((MobEffect)DorpMod.OVER_SMOKING.get()));
                           player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder((MobEffect)DorpMod.LUNG_COLLAPSE.get()));
                           player.getCooldowns().removeCooldown((Item)DorpMod.CIGARETTE.get());
                           player.sendSystemMessage(Component.literal("Reset all cigarette / addiction data!").withStyle(ChatFormatting.GREEN));
                           return 1;
                        }))
                  ))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("device").requires(source -> source.hasPermission(2)))
                     .then(Commands.literal("charge").executes(context -> {
                        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                        int chargedCount = 0;

                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                           ItemStack s = player.getInventory().getItem(i);
                           if (s.getItem() instanceof WalkieTalkieItem) {
                              WalkieTalkieItem.setEnergy(s, 50000);
                              chargedCount++;
                           } else if (s.is((Item)DorpMod.HEADSET.get())) {
                              HeadsetItem.setEnergy(s, 50000);
                              chargedCount++;
                           } else if (s.is((Item)DorpMod.HEAD_TORCH.get())) {
                              HeadTorchItem.setEnergy(s, 7200);
                              chargedCount++;
                           }
                        }

                        Optional<ICuriosItemHandler> curioInvOpt = CuriosApi.getCuriosInventory(player);
                        if (curioInvOpt.isPresent()) {
                           for (SlotResult slotResult : curioInvOpt.get().findCurios((Item)DorpMod.HEADSET.get())) {
                              HeadsetItem.setEnergy(slotResult.stack(), 50000);
                              chargedCount++;
                           }

                           for (SlotResult slotResult : curioInvOpt.get().findCurios((Item)DorpMod.HEAD_TORCH.get())) {
                              HeadTorchItem.setEnergy(slotResult.stack(), 7200);
                              chargedCount++;
                           }
                        }

                        player.sendSystemMessage(Component.literal("Recharged " + chargedCount + " devices in your inventory!").withStyle(ChatFormatting.GREEN));
                        return 1;
                     }))
               ))
            .then(
                ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tools").requires(source -> source.hasPermission(2)))
                   .then(Commands.literal("testplayer").executes(context -> {
                     ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
                     ItemStack testPlayerItem = new ItemStack((ItemLike)DorpMod.TEST_PLAYER.get());
                     if (!player.getInventory().add(testPlayerItem)) {
                        player.drop(testPlayerItem, false);
                     }

                     player.sendSystemMessage(Component.literal("Given Test Player tool!").withStyle(ChatFormatting.GREEN));
                     return 1;
                  }))
            )))
         .then(WarModeManager.registerSubcommand());
   }

   private static void handleShoutCommand(ServerPlayer sender, String message) {
      long now = System.currentTimeMillis();
      long lastShout = SHOUT_COOLDOWNS.getOrDefault(sender.getUUID(), 0L);
      long diff = now - lastShout;
      long cooldownMs = DorpConfig.SHOUT_COOLDOWN_MS.get();
      if (diff < cooldownMs) {
         long remainingMs = cooldownMs - diff;
         long minutes = remainingMs / 60000L;
         long seconds = remainingMs % 60000L / 1000L;
         sender.sendSystemMessage(
            Component.literal(
                  String.format("You can only shout once every %d minutes! Cooldown remaining: %d minutes, %d seconds.", cooldownMs / 60000L, minutes, seconds)
               )
               .withStyle(ChatFormatting.RED)
         );
      } else {
         SHOUT_COOLDOWNS.put(sender.getUUID(), now);
         Component shoutMsg = Component.empty()
            .append(Component.literal("[Yell] ").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}))
            .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
            .append(sender.getDisplayName())
            .append(Component.literal("]: " + message).withStyle(ChatFormatting.WHITE));
         Level level = sender.level();
         double shoutConfigRange = DorpConfig.SHOUT_RANGE.get();
         double shoutRangeSq = shoutConfigRange * shoutConfigRange;
         sender.sendSystemMessage(shoutMsg);

         for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
            if (player != sender && player.level() == level && DistanceHelper.distanceSq(level, sender.position(), player.position()) <= shoutRangeSq) {
               player.sendSystemMessage(shoutMsg);
            }
         }
      }
   }

   private static void handleWhisperCommand(ServerPlayer sender, String message) {
      Level level = sender.level();
      double whisperRange = DorpConfig.PROXIMITY_RANGE_WHISPER.get();
      double whisperRangeSq = whisperRange * whisperRange;
      sender.sendSystemMessage(buildProximityMessage(sender, message, 0.0, whisperRangeSq, true));

      for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
         if (player != sender && player.level() == level) {
            double distanceSq = DistanceHelper.distanceSq(level, sender.position(), player.position());
            if (distanceSq <= whisperRangeSq) {
               player.sendSystemMessage(buildProximityMessage(sender, message, distanceSq, whisperRangeSq, true));
            }
         }
      }
   }

   private static int handleSetName(CommandSourceStack source, ServerPlayer target, String newName) {
      boolean isOp = source.hasPermission(2);
      boolean isSelf = source.getEntity() == target;
      if (!isOp && target.getPersistentData().getBoolean("DorpHasCustomName")) {
         source.sendFailure(Component.literal("You can only change your display name once!"));
         return 0;
      } else {
         target.getPersistentData().putString("DorpCustomDisplayName", newName);
         if (!isOp) {
            target.getPersistentData().putBoolean("DorpHasCustomName", true);
         }

         target.refreshDisplayName();
         if (target.getServer() != null) {
            target.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(Action.UPDATE_DISPLAY_NAME, target));
         }

         if (isSelf) {
            source.sendSuccess(() -> Component.literal("Your display name has been set to: " + newName).withStyle(ChatFormatting.GREEN), true);
         } else {
            source.sendSuccess(
               () -> Component.literal(target.getScoreboardName() + "'s display name has been set to: " + newName).withStyle(ChatFormatting.GREEN), true
            );
            target.sendSystemMessage(
               Component.literal("Your display name has been set to: " + newName + " by an administrator.").withStyle(ChatFormatting.GREEN)
            );
         }

         return 1;
      }
   }

   private static int handleClearName(CommandSourceStack source, ServerPlayer target) {
      target.getPersistentData().remove("DorpCustomDisplayName");
      target.refreshDisplayName();
      if (target.getServer() != null) {
         target.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(Action.UPDATE_DISPLAY_NAME, target));
      }

      boolean isSelf = source.getEntity() == target;
      if (isSelf) {
         source.sendSuccess(() -> Component.literal("Your display name has been cleared.").withStyle(ChatFormatting.GREEN), true);
      } else {
         source.sendSuccess(() -> Component.literal("Cleared display name for " + target.getScoreboardName()).withStyle(ChatFormatting.GREEN), true);
         target.sendSystemMessage(Component.literal("Your display name has been cleared by an administrator.").withStyle(ChatFormatting.GREEN));
      }

      return 1;
   }
}
