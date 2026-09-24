package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HimariVisorItem extends ArmorItem implements GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public static volatile String currentMood = "normal";
   private static String lastMood = "";
   private static int lastHurtTick = -100000;

   public HimariVisorItem(Properties properties) {
      super(ArmorMaterials.DIAMOND, Type.HELMET, properties);
   }

   @Override
   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "mood", 5, this::moodPredicate));
   }

   private PlayState moodPredicate(AnimationState<HimariVisorItem> state) {
      String mood = currentMood;
      if (!mood.equals(lastMood)) {
         lastMood = mood;
         return state.setAndContinue(RawAnimation.begin().thenLoop(mood));
      }
      return PlayState.CONTINUE;
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return cache;
   }

   public static String moodFor(LivingEntity entity) {
      if (entity.hurtTime > 0) {
         lastHurtTick = entity.tickCount;
      }
      if (entity.tickCount - lastHurtTick < 600) {
         return "angry";
      }
      if (entity.getHealth() <= entity.getMaxHealth() * 0.35F) {
         return "sad";
      }
      if (isCreeperAboutToExplode(entity)) {
         return "surprised";
      }
      if (entity.isSprinting()) {
         return "happy";
      }
      if (entity.isCrouching()) {
         return "tired";
      }
      if (entity.isInWater()) {
         return "confusing";
      }
      if ((entity.tickCount % 100) < 6) {
         return "blink";
      }
      return "normal";
   }

   private static boolean isCreeperAboutToExplode(LivingEntity entity) {
      try {
         if (entity.level() == null) {
            return false;
         }
         java.util.List<net.minecraft.world.entity.monster.Creeper> creepers =
               entity.level().getEntitiesOfClass(
                     net.minecraft.world.entity.monster.Creeper.class,
                     entity.getBoundingBox().inflate(6.0),
                     c -> c.isAlive() && (c.getSwellDir() > 0 || c.isIgnited()));
         return !creepers.isEmpty();
      } catch (Throwable t) {
         return false;
      }
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(
         Component.literal("You need this for driving 'wheelchair'.")
            .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Modelled & Animated by Otaku")
            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
