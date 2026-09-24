package com.dorp;

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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CapBlockItem extends BlockItem implements Equipable {
   private final String modeler;

   public CapBlockItem(Block block, Properties properties) {
      this(block, properties, "Emir");
   }

   public CapBlockItem(Block block, Properties properties, String modeler) {
      super(block, properties);
      this.modeler = modeler;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      return this.swapWithEquipmentSlot(this, level, player, hand);
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }

   public ItemAttributeModifiers getDefaultAttributeModifiers() {
      return ItemAttributeModifiers.builder()
         .add(
            Attributes.ARMOR,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "helmet_armor"), 3.0, Operation.ADD_VALUE),
            EquipmentSlotGroup.HEAD
         )
         .add(
            Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("dorp", "helmet_toughness"), 2.0, Operation.ADD_VALUE),
            EquipmentSlotGroup.HEAD
         )
         .build();
   }

   public int getEnchantmentValue() {
      return 10;
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A decorative cap wearable as a helmet.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("Can also be placed as a decorative block.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(
         Component.literal("Right-Click to wear on your head").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(
         Component.literal("Crouch+Right-Click on surface to place").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      tooltipComponents.add(Component.literal(""));
      tooltipComponents.add(Component.literal("Modelled by " + this.modeler).withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
