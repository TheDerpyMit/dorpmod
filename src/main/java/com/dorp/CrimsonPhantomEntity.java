package com.dorp;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class CrimsonPhantomEntity extends PathfinderMob {
   public static final int LIFESPAN_TICKS = 100;
   private static final EntityDataAccessor<Optional<UUID>> DATA_TARGET_UUID = SynchedEntityData.defineId(
      CrimsonPhantomEntity.class, EntityDataSerializers.OPTIONAL_UUID
   );
   private static final EntityDataAccessor<Boolean> DATA_IS_JUMPSCARE = SynchedEntityData.defineId(CrimsonPhantomEntity.class, EntityDataSerializers.BOOLEAN);
   int ticksAlive = 0;

   public CrimsonPhantomEntity(EntityType<? extends CrimsonPhantomEntity> type, Level level) {
      super(type, level);
      this.setNoAi(true);
      this.setSilent(true);
      this.setInvulnerable(true);
      this.setPersistenceRequired();
      this.setNoGravity(true);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_TARGET_UUID, Optional.empty());
      builder.define(DATA_IS_JUMPSCARE, false);
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.0);
   }

   public void setTargetPlayerUUID(UUID uuid) {
      this.entityData.set(DATA_TARGET_UUID, Optional.of(uuid));
   }

   public Optional<UUID> getTargetPlayerUUID() {
      return (Optional<UUID>)this.entityData.get(DATA_TARGET_UUID);
   }

   public boolean isJumpscare() {
      return (Boolean)this.entityData.get(DATA_IS_JUMPSCARE);
   }

   public void setJumpscare(boolean value) {
      this.entityData.set(DATA_IS_JUMPSCARE, value);
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         this.ticksAlive++;
         Optional<UUID> optUuid = this.getTargetPlayerUUID();
         if (optUuid.isEmpty()) {
            this.discard();
         } else {
            Player player = this.level().getPlayerByUUID(optUuid.get());
            if (player == null) {
               this.discard();
            } else if (this.isJumpscare()) {
               this.tickJumpscare(player);
            } else {
               this.facePlayer(player);
               this.checkStare(player);
            }
         }
      }
   }

   private void tickJumpscare(Player player) {
      if (this.ticksAlive < 40) {
         this.facePlayer(player);
      } else {
         if (this.ticksAlive == 40 && this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)DorpMod.BONESNAP.get(), SoundSource.HOSTILE, 3.0F, 1.0F);
         }

         if (this.ticksAlive < 80) {
            this.facePlayer(player);
         } else {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            this.facePlayer(player);
            if (dist < 1.8 && this.ticksAlive > 81) {
               if (this.level() instanceof ServerLevel sl && player instanceof ServerPlayer sp) {
                  PacketDistributor.sendToPlayer(sp, new BsodPayload(), new CustomPacketPayload[0]);
                  sp.getPersistentData().remove("CrimsonPendingJumpscare");
                  sp.getPersistentData().remove("CrimsonJumpscareSpawned");
               }

               this.spawnDeathParticles();
               this.discard();
            } else {
               if (dist > 0.1) {
                  double speed = 0.5;
                  this.setPos(this.getX() + dx / dist * speed, this.getY(), this.getZ() + dz / dist * speed);
               }

               if (this.ticksAlive >= 260) {
                  this.spawnDeathParticles();
                  this.discard();
               }
            }
         }
      }
   }

   private void facePlayer(Player player) {
      double dx = player.getX() - this.getX();
      double dz = player.getZ() - this.getZ();
      float yaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
      this.setYRot(yaw);
      this.setYHeadRot(yaw);
   }

   private void checkStare(Player player) {
      Vec3 lookVec = player.getLookAngle();
      double toDx = this.getX() - player.getX();
      double toDy = this.getEyeY() - player.getEyeY();
      double toDz = this.getZ() - player.getZ();
      double dist = Math.sqrt(toDx * toDx + toDy * toDy + toDz * toDz);
      if (dist > 0.01 && dist < 20.0) {
         double dot = (lookVec.x * toDx + lookVec.y * toDy + lookVec.z * toDz) / dist;
         if (dot > 0.97) {
            this.spawnDeathParticles();
            this.discard();
         }
      }
   }

   private void spawnDeathParticles() {
      if (this.level() instanceof ServerLevel sl) {
         sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getEyeY(), this.getZ(), 20, 0.3, 0.5, 0.3, 0.04);
         sl.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.2, 0.3, 0.2, 0.02);
      }
   }

   public boolean removeWhenFarAway(double d) {
      return false;
   }

   protected SoundEvent getDeathSound() {
      return null;
   }

   protected SoundEvent getHurtSound(DamageSource ds) {
      return null;
   }

   protected SoundEvent getAmbientSound() {
      return null;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      this.getTargetPlayerUUID().ifPresent(uid -> tag.putUUID("TargetUUID", uid));
      tag.putInt("TicksAlive", this.ticksAlive);
      tag.putBoolean("IsJumpscare", this.isJumpscare());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.hasUUID("TargetUUID")) {
         this.setTargetPlayerUUID(tag.getUUID("TargetUUID"));
      }

      this.ticksAlive = tag.getInt("TicksAlive");
      if (tag.getBoolean("IsJumpscare")) {
         this.setJumpscare(true);
      }
   }
}
