package com.dorp;

import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class CrimsonThreadItem extends Item implements ICurioItem {
   public static final int MAX_USES = 10;
   static final Random RING_RANDOM = new Random();

   public CrimsonThreadItem(Properties properties) {
      super(properties);
   }

   public static int getUses(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? 0 : data.copyTag().getInt("uses");
   }

   public static void setUses(ItemStack stack, int uses) {
      CompoundTag tag = new CompoundTag();
      CustomData existing = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (existing != null) {
         tag = existing.copyTag();
      }

      tag.putInt("uses", uses);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static boolean getJumpscareUsed(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? false : data.copyTag().getBoolean("jumpscareUsed");
   }

   public static void setJumpscareUsed(ItemStack stack, boolean used) {
      CompoundTag tag = new CompoundTag();
      CustomData existing = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (existing != null) {
         tag = existing.copyTag();
      }

      tag.putBoolean("jumpscareUsed", used);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static int getStage(ItemStack stack) {
      int uses = getUses(stack);
      if (uses < 3) {
         return 1;
      } else if (uses < 6) {
         return 2;
      } else {
         return uses < 9 ? 3 : 4;
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
      tip.add(Component.literal("A ring of deep crimson, hot to the touch.").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}));
      tip.add(Component.literal(""));
      tip.add(Component.literal("Don't use it too often...").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
      tip.add(Component.literal(""));
      tip.add(
         Component.literal("Triggers passively in inventory at ≤4 hearts ❤.").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.ITALIC})
      );
      tip.add(
         Component.literal("Only one Crimson Ring may exist in your inventory.")
            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tip.add(Component.literal(""));
      tip.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, ctx, tip, flag);
   }

   public static void activateRing(ServerPlayer player, ItemStack stack, ServerLevel level) {
      int stage = getStage(stack);
      int newUses = getUses(stack) + 1;
      level.broadcastEntityEvent(player, (byte)35);
      PacketDistributor.sendToPlayer(player, new RingTotemPayload(), new CustomPacketPayload[0]);
      spawnDepartureBeam(level, player.getX(), player.getY(), player.getZ(), stage);
      double[] dest = findTeleportDestination(level, player);
      applyTeleportEffects(player, stage);
      if (stage == 1) {
         player.heal(5.0F);
      }

      player.teleportTo(dest[0], dest[1], dest[2]);
      spawnArrivalBeam(level, dest[0], dest[1], dest[2], stage);
      level.playSound(null, dest[0], dest[1], dest[2], (SoundEvent)DorpMod.RING_TELEPORT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      player.getCooldowns().addCooldown(stack.getItem(), 2400);
      if (newUses >= 10) {
         player.displayClientMessage(
            Component.translatable("message.dorp.ring.stage4_vanish").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}), true
         );
         PacketDistributor.sendToPlayer(player, new RingAmbientPayload(6), new CustomPacketPayload[0]);
         player.getPersistentData().putBoolean("CrimsonSleepDeprived", true);
         player.getPersistentData().putBoolean("CrimsonPendingJumpscare", true);
         player.getPersistentData().putBoolean("CrimsonJumpscareSpawned", false);
         int delayTicks = 1200 + RING_RANDOM.nextInt(1201);
         player.getPersistentData().putLong("CrimsonJumpscareTime", level.getGameTime() + delayTicks);
         stack.shrink(1);
      } else {
         setUses(stack, newUses);
         int newStage = getStage(stack);
         if (newStage > stage) {
            sendStageMessage(player, newStage);
         }
      }
   }

   private static void applyTeleportEffects(ServerPlayer player, int stage) {
      player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
      switch (stage) {
         case 1:
         case 2:
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2, false, false));
            break;
         case 3:
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1, false, false));
            break;
         case 4:
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 1, false, false));
      }
   }

   private static void sendStageMessage(ServerPlayer player, int newStage) {
      Component msg = switch (newStage) {
         case 2 -> Component.translatable("message.dorp.ring.stage2").withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.ITALIC});
         case 3 -> Component.translatable("message.dorp.ring.stage3").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC});
         default -> Component.literal("The ring pulses.").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.ITALIC});
      };
      player.displayClientMessage(msg, true);
   }

   public static void spawnDepartureBeam(ServerLevel level, double x, double y, double z, int stage) {
      switch (stage) {
         case 1:
            beamStage1(level, x, y, z);
            break;
         case 2:
            beamStage2(level, x, y, z);
            break;
         case 3:
            beamStage3(level, x, y, z);
            break;
         default:
            beamStage4(level, x, y, z);
      }
   }

   public static void spawnArrivalBeam(ServerLevel level, double x, double y, double z, int stage) {
      switch (stage) {
         case 1:
            beamArrival1(level, x, y, z);
            break;
         case 2:
            beamArrival2(level, x, y, z);
            break;
         case 3:
            beamArrival3(level, x, y, z);
            break;
         default:
            beamArrival4(level, x, y, z);
      }
   }

   private static void beamStage1(ServerLevel lvl, double x, double y, double z) {
      DustParticleOptions crimson = new DustParticleOptions(new Vector3f(1.0F, 0.08F, 0.08F), 1.8F);
      DustParticleOptions amber = new DustParticleOptions(new Vector3f(1.0F, 0.45F, 0.0F), 1.2F);

      for (int i = 0; i < 20; i++) {
         double yo = i * 0.75;
         lvl.sendParticles(crimson, x, y + yo, z, 4, 0.12, 0.0, 0.12, 0.01);
      }

      for (int i = 0; i < 14; i++) {
         double yo = i * 0.75;
         lvl.sendParticles(amber, x, y + yo, z, 2, 0.04, 0.0, 0.04, 0.03);
      }

      lvl.sendParticles(ParticleTypes.FLAME, x, y, z, 35, 0.25, 0.0, 0.25, 0.07);
      lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 12, 0.18, 0.0, 0.18, 0.05);
      lvl.sendParticles(ParticleTypes.CRIT, x, y + 0.3, z, 22, 0.55, 0.15, 0.55, 0.15);
      lvl.sendParticles(ParticleTypes.ENCHANT, x, y + 0.5, z, 35, 0.8, 1.2, 0.8, 1.0);
   }

   private static void beamArrival1(ServerLevel lvl, double x, double y, double z) {
      DustParticleOptions crimson = new DustParticleOptions(new Vector3f(1.0F, 0.08F, 0.08F), 1.5F);

      for (int i = 0; i < 14; i++) {
         lvl.sendParticles(crimson, x, y + i * 0.75, z, 3, 0.1, 0.0, 0.1, 0.01);
      }

      lvl.sendParticles(ParticleTypes.FLAME, x, y, z, 20, 0.25, 0.0, 0.25, 0.06);
      lvl.sendParticles(ParticleTypes.ENCHANT, x, y + 0.5, z, 25, 0.7, 1.0, 0.7, 0.8);
   }

   private static void beamStage2(ServerLevel lvl, double x, double y, double z) {
      DustParticleOptions darkRed = new DustParticleOptions(new Vector3f(0.72F, 0.0F, 0.1F), 2.0F);
      DustParticleOptions purple = new DustParticleOptions(new Vector3f(0.52F, 0.0F, 0.52F), 1.6F);

      for (int i = 0; i < 25; i++) {
         double yo = i * 0.65;
         double jx = (RING_RANDOM.nextDouble() - 0.5) * 0.45;
         double jz = (RING_RANDOM.nextDouble() - 0.5) * 0.45;
         lvl.sendParticles(darkRed, x + jx, y + yo, z + jz, 3, 0.08, 0.0, 0.08, 0.02);
      }

      for (int i = 0; i < 16; i++) {
         lvl.sendParticles(purple, x, y + i * 0.85, z, 2, 0.2, 0.0, 0.2, 0.03);
      }

      lvl.sendParticles(ParticleTypes.WITCH, x, y + 2.5, z, 22, 0.55, 1.1, 0.55, 0.12);
      lvl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 16, 0.35, 0.15, 0.35, 0.02);
      lvl.sendParticles(ParticleTypes.SOUL, x, y + 0.5, z, 18, 0.6, 0.6, 0.6, 0.06);
      lvl.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 1.0, z, 22, 0.5, 1.6, 0.5, 0.06);
   }

   private static void beamArrival2(ServerLevel lvl, double x, double y, double z) {
      DustParticleOptions darkRed = new DustParticleOptions(new Vector3f(0.72F, 0.0F, 0.1F), 1.8F);

      for (int i = 0; i < 18; i++) {
         double jx = (RING_RANDOM.nextDouble() - 0.5) * 0.35;
         double jz = (RING_RANDOM.nextDouble() - 0.5) * 0.35;
         lvl.sendParticles(darkRed, x + jx, y + i * 0.65, z + jz, 2, 0.08, 0.0, 0.08, 0.02);
      }

      lvl.sendParticles(ParticleTypes.SOUL, x, y + 0.5, z, 14, 0.55, 0.55, 0.55, 0.05);
      lvl.sendParticles(ParticleTypes.WITCH, x, y + 2.0, z, 14, 0.5, 0.8, 0.5, 0.1);
      lvl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 10, 0.3, 0.15, 0.3, 0.02);
   }

   private static void beamStage3(ServerLevel lvl, double x, double y, double z) {
      DustParticleOptions voidBlack = new DustParticleOptions(new Vector3f(0.12F, 0.0F, 0.0F), 2.5F);
      DustParticleOptions corruptPur = new DustParticleOptions(new Vector3f(0.3F, 0.0F, 0.3F), 2.0F);

      for (int i = 0; i < 30; i++) {
         double yo = i * 0.55;
         double jx = (RING_RANDOM.nextDouble() - 0.5) * 0.85;
         double jz = (RING_RANDOM.nextDouble() - 0.5) * 0.85;
         lvl.sendParticles(voidBlack, x + jx, y + yo, z + jz, 3, 0.14, 0.0, 0.14, 0.02);
      }

      for (int i = 0; i < 20; i++) {
         lvl.sendParticles(corruptPur, x, y + i * 0.7, z, 3, 0.25, 0.0, 0.25, 0.04);
      }

      lvl.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 55, 0.55, 1.6, 0.55, 0.12);
      lvl.sendParticles(ParticleTypes.SOUL, x, y + 1.0, z, 28, 0.8, 1.1, 0.8, 0.06);
      lvl.sendParticles(ParticleTypes.POOF, x, y, z, 22, 0.4, 0.2, 0.4, 0.12);
      lvl.sendParticles(ParticleTypes.OMINOUS_SPAWNING, x, y + 1.0, z, 12, 0.3, 0.6, 0.3, 0.06);
      lvl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.OBSIDIAN.defaultBlockState()), x, y + 16.0, z, 20, 0.35, 0.0, 0.35, 0.0);
   }

   private static void beamArrival3(ServerLevel lvl, double x, double y, double z) {
      DustParticleOptions voidBlack = new DustParticleOptions(new Vector3f(0.12F, 0.0F, 0.0F), 2.2F);

      for (int i = 0; i < 22; i++) {
         double jx = (RING_RANDOM.nextDouble() - 0.5) * 0.7;
         double jz = (RING_RANDOM.nextDouble() - 0.5) * 0.7;
         lvl.sendParticles(voidBlack, x + jx, y + i * 0.55, z + jz, 2, 0.1, 0.0, 0.1, 0.02);
      }

      lvl.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 35, 0.5, 1.4, 0.5, 0.1);
      lvl.sendParticles(ParticleTypes.SOUL, x, y + 1.0, z, 20, 0.7, 1.0, 0.7, 0.05);
      lvl.sendParticles(ParticleTypes.OMINOUS_SPAWNING, x, y + 1.0, z, 8, 0.25, 0.5, 0.25, 0.05);
   }

   private static void beamStage4(ServerLevel lvl, double x, double y, double z) {
      DustParticleOptions voidRed = new DustParticleOptions(new Vector3f(0.6F, 0.0F, 0.0F), 3.0F);
      DustParticleOptions ashGray = new DustParticleOptions(new Vector3f(0.2F, 0.18F, 0.18F), 2.0F);

      for (int i = 0; i < 38; i++) {
         double yo = i * 0.5;
         double chaos = (RING_RANDOM.nextDouble() - 0.5) * 1.25;
         double chaz = (RING_RANDOM.nextDouble() - 0.5) * 1.25;
         lvl.sendParticles(voidRed, x + chaos, y + yo, z + chaz, 4, 0.18, 0.0, 0.18, 0.04);
      }

      for (int i = 0; i < 20; i++) {
         double yo = i * 0.85;
         lvl.sendParticles(ashGray, x, y + yo, z, 3, 0.3, 0.0, 0.3, 0.03);
      }

      lvl.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 90, 1.1, 2.2, 1.1, 0.22);
      lvl.sendParticles(ParticleTypes.SOUL, x, y + 1.0, z, 45, 1.1, 1.8, 1.1, 0.12);
      lvl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 30, 0.5, 0.3, 0.5, 0.05);
      lvl.sendParticles(ParticleTypes.ASH, x, y + 1.0, z, 65, 1.1, 2.2, 1.1, 0.06);
      lvl.sendParticles(ParticleTypes.OMINOUS_SPAWNING, x, y + 1.0, z, 25, 0.6, 1.2, 0.6, 0.12);
      lvl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 3, 0.6, 0.0, 0.6, 0.0);
   }

   private static void beamArrival4(ServerLevel lvl, double x, double y, double z) {
      lvl.sendParticles(ParticleTypes.ASH, x, y + 1.0, z, 40, 0.8, 1.8, 0.8, 0.05);
      lvl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 15, 0.4, 0.2, 0.4, 0.03);
      lvl.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 20, 0.4, 1.0, 0.4, 0.08);
   }

   static double[] findTeleportDestination(ServerLevel level, Player player) {
      double px = player.getX();
      double py = player.getY();
      double pz = player.getZ();

      for (int attempt = 0; attempt < 50; attempt++) {
         double angle = RING_RANDOM.nextDouble() * Math.PI * 2.0;
         double dist = 35.0 + RING_RANDOM.nextDouble() * 10.0;
         double tx = px + Math.cos(angle) * dist;
         double tz = pz + Math.sin(angle) * dist;
         int startY = Math.min(level.getMaxBuildHeight() - 2, (int)py + 16);
         int endY = Math.max(level.getMinBuildHeight() + 1, (int)py - 16);

         for (int iy = startY; iy >= endY; iy--) {
            BlockPos base = new BlockPos((int)tx, iy, (int)tz);
            BlockPos above1 = base.above();
            BlockPos above2 = base.above(2);
            if (level.getBlockState(base).isSolidRender(level, base)
               && level.getBlockState(above1).getCollisionShape(level, above1).isEmpty()
               && level.getBlockState(above2).getCollisionShape(level, above2).isEmpty()
               && level.getFluidState(above1).isEmpty()
               && level.getFluidState(above2).isEmpty()) {
               return new double[]{tx, iy + 1.0, tz};
            }
         }
      }

      return new double[]{px, py, pz};
   }

   public static void spawnPhantom(ServerLevel level, ServerPlayer player) {
      double angle = RING_RANDOM.nextDouble() * Math.PI * 2.0;
      double dist = 5.0 + RING_RANDOM.nextDouble() * 8.0;
      double ex = player.getX() + Math.cos(angle) * dist;
      double ez = player.getZ() + Math.sin(angle) * dist;
      double ey = player.getY();

      for (int dy = 0; dy < 6; dy++) {
         BlockPos bp = BlockPos.containing(ex, ey - dy, ez);
         if (level.getBlockState(bp).isSolidRender(level, bp)) {
            ey = ey - dy + 1.0;
            break;
         }
      }

      CrimsonPhantomEntity phantom = (CrimsonPhantomEntity)((EntityType)DorpMod.CRIMSON_PHANTOM.get()).create(level);
      if (phantom != null) {
         phantom.setTargetPlayerUUID(player.getUUID());
         phantom.moveTo(ex, ey, ez, 0.0F, 0.0F);
         level.addFreshEntity(phantom);
         level.sendParticles(ParticleTypes.LARGE_SMOKE, ex, ey + 1.0, ez, 10, 0.3, 0.4, 0.3, 0.02);
      }
   }

   private static int countThreadsInInventory(Player player) {
      int count = 0;

      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         if (player.getInventory().getItem(i).is((Item)DorpMod.CRIMSON_THREAD.get())) {
            count++;
         }
      }

      return count;
   }
}
