package com.dorp.mixin;

import com.dorp.Events;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
   public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
      super(level, pos, yRot, gameProfile);
   }

   @ModifyVariable(method = "die", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
   private Component modifyDeathMessage(Component original) {
      DamageSource damageSource = this.getLastDamageSource();
      return (Component)(damageSource != null && damageSource.getEntity() instanceof Player
         ? Component.literal(Events.getCustomDisplayNameString(this) + " died.")
         : original);
   }
}
