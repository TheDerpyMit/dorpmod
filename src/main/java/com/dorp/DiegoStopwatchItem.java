package com.dorp;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "dorp")
public class DiegoStopwatchItem extends Item {
   private static final Map<UUID, Integer> ACTIVE_TIME_STOPS = new ConcurrentHashMap<>();

   public DiegoStopwatchItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (!level.isClientSide() && level instanceof ServerLevel serverLevel && !player.getCooldowns().isOnCooldown(this)) {
         player.getCooldowns().addCooldown(this, 72000);
         itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
         serverLevel.playSound(null, player.blockPosition(), (SoundEvent)DorpMod.PARADIXUS_PARADOXUM.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
         PacketDistributor.sendToPlayer((ServerPlayer)player, new StopwatchTotemPayload(), new CustomPacketPayload[0]);
         ACTIVE_TIME_STOPS.put(player.getUUID(), 0);
         return InteractionResultHolder.success(itemstack);
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   @SubscribeEvent
   public static void onServerTick(Post event) {
      MinecraftServer server = event.getServer();
      if (server != null) {
         for (Entry<UUID, Integer> entry : ACTIVE_TIME_STOPS.entrySet()) {
            UUID playerUuid = entry.getKey();
            int ticksElapsed = entry.getValue() + 1;
            entry.setValue(ticksElapsed);
            ServerPlayer caster = server.getPlayerList().getPlayer(playerUuid);
            if (caster == null) {
               if (server.tickRateManager().isFrozen()) {
                  server.tickRateManager().setFrozen(false);

                  for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                     PacketDistributor.sendToPlayer(sp, new CameraFreezePayload(false), new CustomPacketPayload[0]);
                  }
               }

               ACTIVE_TIME_STOPS.remove(playerUuid);
            } else {
               ServerLevel level = caster.serverLevel();
               if (ticksElapsed == 100) {
                  level.playSound(null, caster.blockPosition(), (SoundEvent)DorpMod.TIMESTOP_START.get(), SoundSource.PLAYERS, 1.4F, 1.0F);
                  server.tickRateManager().setFrozen(true);
                  caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 260, 1, false, false, true));
                  caster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 260, 0, false, false, true));
                  PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                     caster, new MonsterRoarPayload(caster.getX(), caster.getY(), caster.getZ()), new CustomPacketPayload[0]
                  );

                  for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                     PacketDistributor.sendToPlayer(sp, new TimeStopPayload(260), new CustomPacketPayload[0]);
                     if (!sp.getUUID().equals(playerUuid)) {
                        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 260, 3, false, false, true));
                        sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 260, 3, false, false, true));
                        PacketDistributor.sendToPlayer(sp, new CameraFreezePayload(true), new CustomPacketPayload[0]);
                     }
                  }
               }

               if (ticksElapsed > 100 && ticksElapsed < 360 && ticksElapsed % 20 == 0) {
                  for (ServerPlayer spx : server.getPlayerList().getPlayers()) {
                     if (!spx.getUUID().equals(playerUuid)) {
                        PacketDistributor.sendToPlayer(spx, new CameraFreezePayload(true), new CustomPacketPayload[0]);
                     }
                  }
               }

               if (ticksElapsed == 360) {
                  level.playSound(null, caster.blockPosition(), (SoundEvent)DorpMod.TIMESTOP_STOP.get(), SoundSource.PLAYERS, 1.4F, 1.0F);
                  server.tickRateManager().setFrozen(false);

                  for (ServerPlayer spxx : server.getPlayerList().getPlayers()) {
                     PacketDistributor.sendToPlayer(spxx, new CameraFreezePayload(false), new CustomPacketPayload[0]);
                  }
               }

               String lyric = null;
               switch (ticksElapsed) {
                  case 182:
                     lyric = "Now let me open the scar";
                     break;
                  case 233:
                     lyric = "Tokeatta Virus";
                     break;
                  case 299:
                     lyric = "Niji ni kuro o sashi tobitatsu";
                     break;
                  case 581:
                     lyric = "Akai hana no mitsu";
                     break;
                  case 658:
                     lyric = "Nurete kakureta noizu";
                     break;
                  case 773:
                     lyric = "Mune ni haiyoru no";
                     break;
                  case 845:
                     lyric = "Umarekawatari tai no deshou?";
                     break;
                  case 947:
                     lyric = "Eien nemutteita paradaimu";
                     break;
                  case 1038:
                     lyric = "Shin o kutte shinshoku shiteita";
                     break;
                  case 1182:
                     lyric = "Now let me open the scar";
                     break;
                  case 1234:
                     lyric = "Tokeatta Virus";
                     break;
                  case 1281:
                     lyric = "Furete arawani naru honnou";
                     break;
                  case 1375:
                     lyric = "Grew up in the loneliness";
                     break;
                  case 1422:
                     lyric = "Kowareta Reality";
                     break;
                  case 1488:
                     lyric = "Niji ni kuro o sashi tobitatsu";
                     break;
                  case 1663:
                     lyric = "Ima sugu nukedashite shouki no meiro";
                     break;
                  case 1718:
                     lyric = "Hanten shita kontorasuto e";
                     break;
                  case 1803:
                     lyric = "Aoi garasu ni";
                     break;
                  case 1872:
                     lyric = "Utsutta watashi wa";
                     break;
                  case 1989:
                     lyric = "Zankoku na bishou de";
                     break;
                  case 2061:
                     lyric = "Nodo o furuwaseru no";
                     break;
                  case 2190:
                     lyric = "I'm changing to a monster";
                     break;
                  case 2231:
                     lyric = "Uragiriai mo";
                     break;
                  case 2284:
                     lyric = "Fukaku ochiteyuku purosesu";
                     break;
                  case 2379:
                     lyric = "Is this my insanity?";
                     break;
                  case 2426:
                     lyric = "Sono toi sae ga";
                     break;
                  case 2491:
                     lyric = "Moumoku to yokubou no akashi";
                     break;
                  case 2666:
                     lyric = "Sono mama tobidashite shitta sekai wa";
                     break;
                  case 2724:
                     lyric = "Paradokusu no rakuen no you";
                     break;
                  case 2962:
                     lyric = "\"Live it up, up, up! Live it up! Live it up!\"";
                     break;
                  case 3013:
                     lyric = "\"Live it up, up, up! Live it up! Live it up!\"";
                     break;
                  case 3063:
                     lyric = "\"Live it up, up, up! Live it up! Live it up!\"";
                     break;
                  case 3117:
                     lyric = "Me o samashita";
                     break;
                  case 3155:
                     lyric = "\"Live it up, up, up! Live it up! Live it up!\"";
                     break;
                  case 3202:
                     lyric = "\"Live it up, up, up! Live it up! Live it up!\"";
                     break;
                  case 3257:
                     lyric = "Kanjou no fureru mama ni";
                     break;
                  case 3346:
                     lyric = "Now let me open the scar";
               }

               if (lyric != null) {
                  Component comp = Component.literal(lyric).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.ITALIC});

                  for (ServerPlayer spxx : server.getPlayerList().getPlayers()) {
                     spxx.displayClientMessage(comp, true);
                  }
               }

               if (ticksElapsed > 3420) {
                  ACTIVE_TIME_STOPS.remove(playerUuid);
               }
            }
         }
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(
         Component.literal("Pauses time for everyone, except you.").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("All projectiles are frozen, and the world comes to a stand still for 13 seconds.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by Niko").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
