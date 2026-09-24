package com.dorp;

import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Builder;
import net.minecraft.world.level.Level;

public class SunlightHelmetItem extends Item implements Equipable {
   public SunlightHelmetItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      return this.swapWithEquipmentSlot(this, level, player, hand);
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }

   public ItemAttributeModifiers getDefaultAttributeModifiers() {
      Builder builder = ItemAttributeModifiers.builder()
         .add(
            Attributes.ARMOR,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "helmet_armor"), 2.0, Operation.ADD_VALUE),
            EquipmentSlotGroup.HEAD
         );

      try {
         builder.add(
            PerkAttributes.MAX_MANA,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "sunlight_helmet_max_mana"), 30.0, Operation.ADD_VALUE),
            EquipmentSlotGroup.HEAD
         );
      } catch (Exception | NoClassDefFoundError var4) {
         DorpMod.LOGGER.error("Failed to apply max mana attribute from Ars Nouveau", var4);
      }

      try {
         builder.add(
            PerkAttributes.MANA_REGEN_BONUS,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "sunlight_helmet_mana_regen"), 1.0, Operation.ADD_VALUE),
            EquipmentSlotGroup.HEAD
         );
      } catch (Exception | NoClassDefFoundError var3) {
         DorpMod.LOGGER.error("Failed to apply mana regen attribute from Ars Nouveau", var3);
      }

      return builder.build();
   }

   public int getEnchantmentValue() {
      return 9;
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A relic helmet wearable as a hat.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Right-Click to wear on your head").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
