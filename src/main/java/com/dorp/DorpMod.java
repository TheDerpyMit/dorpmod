package com.dorp;

import com.dorp.client.ClientAuthHandler;
import com.dorp.config.DorpConfig;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.MapCodec;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.swing.JOptionPane;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Open;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mod("dorp")
public class DorpMod {
   public static final String MOD_ID = "dorp";
   public static final boolean CONFIDENTIAL_MODE = false;
   public static final Logger LOGGER = LoggerFactory.getLogger("dorp");
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("dorp");
   public static final Items ITEMS = DeferredRegister.createItems("dorp");
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "dorp");
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "dorp");
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "dorp");
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "dorp");
   public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "dorp");
   public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM_SERIALIZERS = DeferredRegister.create(
      Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "dorp"
   );
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "dorp");
   public static final Supplier<RecipeSerializer<LaptopCraftingRecipe>> LAPTOP_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
      "laptop", () -> new SimpleCraftingRecipeSerializer(LaptopCraftingRecipe::new)
   );
   public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<CrimsonKryaniteGLM>> CRIMSON_KRYANITE_GLM = GLM_SERIALIZERS.register(
      "crimson_kryanite_loot", () -> CrimsonKryaniteGLM.CODEC
   );
   public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<RemoveGearModifier>> REMOVE_GEAR_GLM = GLM_SERIALIZERS.register(
      "remove_gear_loot", () -> RemoveGearModifier.CODEC
   );
   public static final DeferredHolder<MobEffect, MobEffect> NICOTINE_RUSH = MOB_EFFECTS.register("nicotine_rush", NicotineRushEffect::new);
   public static final DeferredHolder<MobEffect, MobEffect> SMOKING_ADDICTION = MOB_EFFECTS.register("smoking_addiction", SmokingAddictionEffect::new);
   public static final DeferredHolder<MobEffect, MobEffect> OVER_SMOKING = MOB_EFFECTS.register("over_smoking", OverSmokingEffect::new);
   public static final DeferredHolder<MobEffect, MobEffect> LUNG_COLLAPSE = MOB_EFFECTS.register("lung_collapse", LungCollapseEffect::new);
   public static final DeferredHolder<MobEffect, MobEffect> CONCUSSION = MOB_EFFECTS.register("concussion", ConcussionEffect::new);
   public static final DeferredHolder<SoundEvent, SoundEvent> SCARE_SOUND = SOUNDS.register(
      "scare_sound", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "scare_sound"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> WALKIETALKIE_ON = SOUNDS.register(
      "walkietalkie_on", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "walkietalkie_on"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> WALKIETALKIE_OFF = SOUNDS.register(
      "walkietalkie_off", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "walkietalkie_off"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> VEST_SOUND = SOUNDS.register(
      "vest", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "vest"), 64.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> KRYANITE_MINING = SOUNDS.register(
      "kryanitemining", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "kryanitemining"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> TIMESTOP = SOUNDS.register(
      "timestop", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "timestop"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> TIMERESUME = SOUNDS.register(
      "timeresume", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "timeresume"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> TIMETICK = SOUNDS.register(
      "time_tick", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "time_tick"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_SCREAMS = SOUNDS.register(
      "ring_screams", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_screams"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_DISTANTSTEPS = SOUNDS.register(
      "ring_distantsteps", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_distantsteps"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_CANYOUHEARME = SOUNDS.register(
      "ring_canyouhearme", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_canyouhearme"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_HESDOWN = SOUNDS.register(
      "ring_hesdown", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_hesdown"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_DRIPSONG = SOUNDS.register(
      "ring_dripsong", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_dripsong"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_CALMSTEPS = SOUNDS.register(
      "ring_calmsteps", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_calmsteps"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_HEADSNAP = SOUNDS.register(
      "ring_headsnap", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_headsnap"), 48.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_TELEPORT = SOUNDS.register(
      "ring_teleport", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_teleport"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_GOODBYE = SOUNDS.register(
      "ring_goodbye", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_goodbye"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RING_SCREAM = SOUNDS.register(
      "ring_scream", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ring_scream"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> BONESNAP = SOUNDS.register(
      "bonesnap", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "bonesnap"), 32.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> FLASHON = SOUNDS.register(
      "flashon", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "flashon"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> FLASHOFF = SOUNDS.register(
      "flashoff", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "flashoff"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> COUGH = SOUNDS.register(
      "cough", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "cough"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> WW1_WHISTLE = SOUNDS.register(
      "ww1-whistle", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "ww1-whistle"), 16.0F)
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> PARADIXUS_PARADOXUM = SOUNDS.register(
      "paradixus_paradoxum", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "paradixus_paradoxum"))
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> WAR_START_MUSIC = SOUNDS.register(
      "sacredwar", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "sacredwar"))
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> WAR_END_MUSIC = SOUNDS.register(
      "kino", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "kino"))
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> TIMESTOP_START = SOUNDS.register(
      "timestop_start", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "timestop_start"))
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> TIMESTOP_STOP = SOUNDS.register(
      "timestop_stop", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dorp", "timestop_stop"))
   );
   public static final DeferredHolder<EntityType<?>, EntityType<CrimsonPhantomEntity>> CRIMSON_PHANTOM = ENTITY_TYPES.register(
      "crimson_phantom",
      () -> Builder.of(CrimsonPhantomEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(64).updateInterval(3).build("dorp:crimson_phantom")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<TestPlayerEntity>> TEST_PLAYER_ENTITY = ENTITY_TYPES.register(
      "test_player",
      () -> Builder.of(TestPlayerEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(64).updateInterval(3).build("dorp:test_player")
   );
   public static final SoundType CRIMSON_KRYANITE_SOUNDS = new DeferredSoundType(
      1.0F,
      1.0F,
      KRYANITE_MINING,
      () -> SoundEvents.AMETHYST_CLUSTER_STEP,
      () -> SoundEvents.AMETHYST_CLUSTER_PLACE,
      () -> SoundEvents.AMETHYST_CLUSTER_HIT,
      () -> SoundEvents.AMETHYST_CLUSTER_FALL
   );
   public static final DeferredBlock<Block> BANANA_BLOCK = BLOCKS.register(
      "banana", () -> new BananaBlock(Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> BANANA_BURGER_BLOCK = BLOCKS.register(
      "banana_burger", () -> new BananaBurgerBlock(Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> BLUE_CAP_BLOCK = BLOCKS.register(
      "blue_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_BLUE).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> POOP_CAP_BLOCK = BLOCKS.register(
      "poop_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> NAZI_CAP_BLOCK = BLOCKS.register(
      "nazi_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> CAPTAIN_CAP_BLOCK = BLOCKS.register(
      "captain_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_BLUE).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> SUOMI_FIELD_CAP_BLOCK = BLOCKS.register(
      "suomi_field_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_BLUE).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> COMMONWEALTH_FIELD_CAP_BLOCK = BLOCKS.register(
      "commonwealth_field_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> REICH_FIELD_CAP_BLOCK = BLOCKS.register(
      "reich_field_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> LIBERTE_CAP_BLOCK = BLOCKS.register(
      "liberte_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_RED).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> WEHRMACHT_OFFICER_CAP_BLOCK = BLOCKS.register(
      "wehrmacht_officer_cap", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> RADIO_BLOCK = BLOCKS.register("radio_block", () -> new RadioBlock(Properties.of().strength(2.0F, 6.0F)));
   public static final DeferredBlock<Block> LOCKED_CHEST = BLOCKS.register(
      "locked_chest",
      () -> new LockedChestBlock(
         Properties.of().strength(2.5F, 3600000.0F), () -> (BlockEntityType<? extends ChestBlockEntity>)DorpMod.LOCKED_CHEST_BLOCK_ENTITY.get()
      )
   );
   public static final DeferredBlock<Block> DEBUG_LOCKED_CHEST = BLOCKS.register(
      "debug_locked_chest",
      () -> new DebugLockedChestBlock(
         Properties.of().strength(2.5F, 3600000.0F), () -> (BlockEntityType<? extends ChestBlockEntity>)DorpMod.LOCKED_CHEST_BLOCK_ENTITY.get()
      )
   );
   public static final DeferredBlock<Block> CAP_BLOCK = BLOCKS.register(
      "cap_block", () -> new CapBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F).sound(SoundType.WOOL).noOcclusion())
   );
   public static final DeferredBlock<Block> CRIMSON_KRYANITE_BLOCK = BLOCKS.register(
      "crimson_kryanite",
      () -> new CrimsonKryaniteBlock(
         Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(3.0F, 3.0F)
            .sound(CRIMSON_KRYANITE_SOUNDS)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> 10)
            .emissiveRendering((state, level, pos) -> true)
      )
   );
   public static final DeferredBlock<Block> FORBIDDEN_WORKBENCH_BLOCK = BLOCKS.register(
      "forbidden_workbench", () -> new ForbiddenWorkbenchBlock(Properties.of().mapColor(MapColor.COLOR_RED).strength(2.5F).sound(SoundType.WOOD).noOcclusion())
   );
   public static final DeferredBlock<Block> INTERCEPTOR_BLOCK = BLOCKS.register(
      "interceptor", () -> new InterceptorBlock(Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL).noOcclusion())
   );
   public static final DeferredBlock<Block> DEBUG_RADIO_BLOCK = BLOCKS.register(
      "debug_radio", () -> new DebugRadioBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5F).sound(SoundType.METAL).noOcclusion())
   );
   public static final DeferredBlock<Block> DEBUG_INTERCEPTOR_BLOCK = BLOCKS.register(
      "debug_interceptor", () -> new DebugInterceptorBlock(Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL).noOcclusion())
   );
   public static final DeferredBlock<Block> INVENTORY_CHECKER_BLOCK = BLOCKS.register(
      "inventory_checker", () -> new InventoryCheckerBlock(Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL).noOcclusion())
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadioBlockEntity>> RADIO_BLOCK_ENTITY = BLOCK_ENTITIES.register(
      "radio_block",
      () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(RadioBlockEntity::new, new Block[]{(Block)RADIO_BLOCK.get()}).build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InterceptorBlockEntity>> INTERCEPTOR_BLOCK_ENTITY = BLOCK_ENTITIES.register(
      "interceptor",
      () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(InterceptorBlockEntity::new, new Block[]{(Block)INTERCEPTOR_BLOCK.get()})
         .build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DebugRadioBlockEntity>> DEBUG_RADIO_BLOCK_ENTITY = BLOCK_ENTITIES.register(
      "debug_radio",
      () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(DebugRadioBlockEntity::new, new Block[]{(Block)DEBUG_RADIO_BLOCK.get()})
         .build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DebugInterceptorBlockEntity>> DEBUG_INTERCEPTOR_BLOCK_ENTITY = BLOCK_ENTITIES.register(
      "debug_interceptor",
      () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
            DebugInterceptorBlockEntity::new, new Block[]{(Block)DEBUG_INTERCEPTOR_BLOCK.get()}
         )
         .build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CapBlockEntity>> CAP_BLOCK_ENTITY = BLOCK_ENTITIES.register(
      "cap",
      () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
            CapBlockEntity::new,
            new Block[]{
               (Block)BLUE_CAP_BLOCK.get(),
               (Block)POOP_CAP_BLOCK.get(),
               (Block)NAZI_CAP_BLOCK.get(),
               (Block)CAPTAIN_CAP_BLOCK.get(),
               (Block)SUOMI_FIELD_CAP_BLOCK.get(),
               (Block)COMMONWEALTH_FIELD_CAP_BLOCK.get(),
               (Block)REICH_FIELD_CAP_BLOCK.get(),
               (Block)LIBERTE_CAP_BLOCK.get(),
               (Block)WEHRMACHT_OFFICER_CAP_BLOCK.get(),
               (Block)CAP_BLOCK.get()
            }
         )
         .build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LockedChestBlockEntity>> LOCKED_CHEST_BLOCK_ENTITY = BLOCK_ENTITIES.register(
      "locked_chest_entity",
      () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
            LockedChestBlockEntity::new, new Block[]{(Block)LOCKED_CHEST.get(), (Block)DEBUG_LOCKED_CHEST.get()}
         )
         .build(null)
   );
   public static final DeferredItem<BananaBurgerItem> BANANA_BURGER = ITEMS.register(
      "banana_burger",
      () -> new BananaBurgerItem(
         (Block)BANANA_BURGER_BLOCK.get(),
         new net.minecraft.world.item.Item.Properties()
            .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(8).saturationModifier(2.0F).alwaysEdible().build())
      )
   );
   public static final DeferredItem<BananaItem> BANANA = ITEMS.register(
      "banana",
      () -> new BananaItem(
         new net.minecraft.world.item.Item.Properties()
            .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build())
      )
   );
   public static final DeferredItem<Item> PAPER_BAG = ITEMS.register(
      "paper_bag", () -> new PaperBagItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> DEBUG_RADIO = ITEMS.register(
      "debug_radio", () -> new BlockItem((Block)DEBUG_RADIO_BLOCK.get(), new net.minecraft.world.item.Item.Properties())
   );
   public static final DeferredItem<Item> DEBUG_INTERCEPTOR = ITEMS.register(
      "debug_interceptor", () -> new BlockItem((Block)DEBUG_INTERCEPTOR_BLOCK.get(), new net.minecraft.world.item.Item.Properties())
   );
   public static final DeferredItem<Item> INVENTORY_CHECKER = ITEMS.register(
      "inventory_checker", () -> new BlockItem((Block)INVENTORY_CHECKER_BLOCK.get(), new net.minecraft.world.item.Item.Properties()) {
         public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
            tooltipComponents.add(Component.literal("Scans player inventories within a 3x3 area behind it.").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal("Modelled by Emir").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
         }
      }
   );
   public static final DeferredItem<Item> CONCEALER_HEAD = ITEMS.register(
      "concealer_head", () -> new ConcealerHeadItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> WALKIE_TALKIE = ITEMS.register(
      "walkie_talkie", () -> new WalkieTalkieItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> BLUE_CAP = ITEMS.register(
      "blue_cap", () -> new CapBlockItem((Block)BLUE_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363))
   );
   public static final DeferredItem<Item> POOP_CAP = ITEMS.register(
      "poop_cap", () -> new CapBlockItem((Block)POOP_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363))
   );
   public static final DeferredItem<Item> NAZI_CAP = ITEMS.register(
      "nazi_cap", () -> new CapBlockItem((Block)NAZI_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363))
   );
   public static final DeferredItem<Item> CAPTAIN_CAP = ITEMS.register(
      "captain_cap", () -> new CapBlockItem((Block)CAPTAIN_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363))
   );
   public static final DeferredItem<Item> SUOMI_FIELD_CAP = ITEMS.register(
      "suomi_field_cap", () -> new CapBlockItem((Block)SUOMI_FIELD_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363), "Niko")
   );
   public static final DeferredItem<Item> COMMONWEALTH_FIELD_CAP = ITEMS.register(
      "commonwealth_field_cap",
      () -> new CapBlockItem((Block)COMMONWEALTH_FIELD_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363), "Niko")
   );
   public static final DeferredItem<Item> REICH_FIELD_CAP = ITEMS.register(
      "reich_field_cap", () -> new CapBlockItem((Block)REICH_FIELD_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363), "Niko")
   );
   public static final DeferredItem<Item> LIBERTE_CAP = ITEMS.register(
      "liberte_cap", () -> new CapBlockItem((Block)LIBERTE_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363), "Niko")
   );
   public static final DeferredItem<Item> WEHRMACHT_OFFICER_CAP = ITEMS.register(
      "wehrmacht_officer_cap",
      () -> new CapBlockItem((Block)WEHRMACHT_OFFICER_CAP_BLOCK.get(), new net.minecraft.world.item.Item.Properties().durability(363), "Stud")
   );
   public static final DeferredItem<Item> LOCKED_CHEST_ITEM = ITEMS.register(
      "locked_chest", () -> new LockedChestItem((Block)LOCKED_CHEST.get(), new net.minecraft.world.item.Item.Properties())
   );
   public static final DeferredItem<Item> DEBUG_LOCKED_CHEST_ITEM = ITEMS.register(
      "debug_locked_chest", () -> new LockedChestItem((Block)DEBUG_LOCKED_CHEST.get(), new net.minecraft.world.item.Item.Properties())
   );
   public static final DeferredItem<Item> BREACHER = ITEMS.register(
      "breacher", () -> new BreacherItem(new net.minecraft.world.item.Item.Properties().durability(5))
   );
   public static final DeferredItem<Item> BOMB_VEST = ITEMS.register(
      "bomb_vest", () -> new BombVestItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> HEADSET = ITEMS.register("headset", () -> new HeadsetItem(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
   public static final DeferredItem<RadioBlockItem> RADIO = ITEMS.register(
      "radio_block", () -> new RadioBlockItem((Block)RADIO_BLOCK.get(), new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<InterceptorBlockItem> INTERCEPTOR = ITEMS.register(
      "interceptor", () -> new InterceptorBlockItem((Block)INTERCEPTOR_BLOCK.get(), new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> CRIMSON_KRYANITE = ITEMS.register(
      "crimson_kryanite", () -> new CrimsonKryaniteItem((Block)CRIMSON_KRYANITE_BLOCK.get(), new net.minecraft.world.item.Item.Properties().stacksTo(64))
   );
   public static final DeferredItem<Item> IRON_RING = ITEMS.register(
      "iron_ring", () -> new IronRingItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> CRIMSON_THREAD = ITEMS.register(
      "crimson_thread", () -> new CrimsonThreadItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> CRIMSON_PLATE = ITEMS.register(
      "crimson_plate", () -> new CrimsonPlateItem(new net.minecraft.world.item.Item.Properties())
   );
   public static final DeferredItem<Item> TRACKER = ITEMS.register("tracker", () -> new TrackerItem(new net.minecraft.world.item.Item.Properties().stacksTo(1)));
   public static final DeferredItem<Item> TRACKER_VIEWER = ITEMS.register(
      "tracker_viewer", () -> new TrackerViewerItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> FORBIDDEN_WORKBENCH = ITEMS.register(
      "forbidden_workbench", () -> new ForbiddenWorkbenchBlockItem((Block)FORBIDDEN_WORKBENCH_BLOCK.get(), new net.minecraft.world.item.Item.Properties())
   );
   public static final DeferredItem<Item> CIGARETTE = ITEMS.register(
      "cigarette", () -> new CigaretteItem(new net.minecraft.world.item.Item.Properties().stacksTo(64))
   );
   public static final DeferredItem<Item> CIGAR = ITEMS.register(
      "cigar", () -> new CigarItem(new net.minecraft.world.item.Item.Properties().stacksTo(1).durability(3))
   );
   public static final DeferredItem<Item> CLOCK = ITEMS.register(
      "clock", () -> new ClockItem(new net.minecraft.world.item.Item.Properties().stacksTo(1).durability(10))
   );
   public static final DeferredItem<Item> TEST_PLAYER = ITEMS.register(
      "test_player", () -> new TestPlayerItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> DIEGO_STOPWATCH = ITEMS.register(
      "diego_stopwatch", () -> new DiegoStopwatchItem(new net.minecraft.world.item.Item.Properties().stacksTo(1).durability(3))
   );
   public static final DeferredItem<Item> HEAD_TORCH = ITEMS.register(
      "head_torch", () -> new HeadTorchItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> HIMARI_VISOR = ITEMS.register(
      "himari_visor", () -> new HimariVisorItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<Item> SUNLIGHT_HELMET = ITEMS.register(
      "sunlight_helmet", () -> new SunlightHelmetItem(new net.minecraft.world.item.Item.Properties().durability(165))
   );
   public static final DeferredItem<Item> OFFICER_WHISTLE = ITEMS.register(
      "officer_whistle", () -> new OfficerWhistleItem(new net.minecraft.world.item.Item.Properties().stacksTo(1).durability(8))
   );
   public static final DeferredItem<Item> ANOMALOUS_SLEDGEHAMMER = ITEMS.register(
      "anomalous_sledgehammer", () -> new AnomalousSledgehammerItem(new net.minecraft.world.item.Item.Properties().stacksTo(1).durability(250))
   );
   public static final DeferredItem<Item> DORP_GUIDE = ITEMS.register(
      "dorp_guide", () -> new DorpGuideItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DORP_ITEMS_TAB = CREATIVE_MODE_TABS.register(
      "dorp_items",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.dorp.dorp_items"))
         .icon(() -> new ItemStack((ItemLike)BANANA.get()))
         .displayItems((parameters, output) -> {
            output.accept((ItemLike)BANANA_BURGER.get());
            output.accept((ItemLike)BANANA.get());
            output.accept((ItemLike)PAPER_BAG.get());
            output.accept((ItemLike)CONCEALER_HEAD.get());
            output.accept((ItemLike)WALKIE_TALKIE.get());
            output.accept((ItemLike)BLUE_CAP.get());
            output.accept((ItemLike)POOP_CAP.get());
            output.accept((ItemLike)NAZI_CAP.get());
            output.accept((ItemLike)CAPTAIN_CAP.get());
            output.accept((ItemLike)SUOMI_FIELD_CAP.get());
            output.accept((ItemLike)COMMONWEALTH_FIELD_CAP.get());
            output.accept((ItemLike)REICH_FIELD_CAP.get());
            output.accept((ItemLike)LIBERTE_CAP.get());
            output.accept((ItemLike)WEHRMACHT_OFFICER_CAP.get());
            output.accept((ItemLike)SUNLIGHT_HELMET.get());
            ItemStack laptop = ImmersiveLaptopHelper.createLaptopStack();
            if (!laptop.isEmpty()) {
               output.accept(laptop);
            }

            output.accept((ItemLike)BOMB_VEST.get());
            output.accept((ItemLike)HEADSET.get());
            output.accept((ItemLike)RADIO.get());
            output.accept((ItemLike)INTERCEPTOR.get());
            output.accept((ItemLike)LOCKED_CHEST_ITEM.get());
            output.accept((ItemLike)CRIMSON_KRYANITE.get());
            output.accept((ItemLike)CRIMSON_PLATE.get());
            output.accept((ItemLike)TRACKER.get());
            output.accept((ItemLike)TRACKER_VIEWER.get());
            output.accept((ItemLike)FORBIDDEN_WORKBENCH.get());
            output.accept((ItemLike)CLOCK.get());
            output.accept((ItemLike)IRON_RING.get());
            output.accept((ItemLike)CRIMSON_THREAD.get());
            output.accept((ItemLike)TEST_PLAYER.get());
            output.accept((ItemLike)DIEGO_STOPWATCH.get());
            output.accept((ItemLike)HEAD_TORCH.get());
            output.accept((ItemLike)HIMARI_VISOR.get());
            output.accept((ItemLike)CIGARETTE.get());
            output.accept((ItemLike)OFFICER_WHISTLE.get());
            output.accept((ItemLike)ANOMALOUS_SLEDGEHAMMER.get());
         })
         .build()
   );
   private static final Map<UUID, Long> RING_PHANTOM_COOLDOWNS = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> RING_EVENT_COOLDOWNS = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> RING_CAVESOUND_COOLDOWNS = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> RING_LATE_EVENT_COOLDOWNS = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> RING_SLEEP_WARNING_COOLDOWNS = new ConcurrentHashMap<>();
   private static final Random RING_SERVER_RANDOM = new Random();
   private static final Set<UUID> ELYTRA_REBELS = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private static final Map<UUID, ItemStack> ELYTRA_PENDING = new ConcurrentHashMap<>();
   private static final Set<UUID> ELYTRA_LAUNCHED = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private static final Random SCARE_RANDOM = new Random();
   private static final Map<UUID, Long> SCAN_COOLDOWNS = new ConcurrentHashMap<>();

   public DorpMod(IEventBus modEventBus, ModContainer modContainer) {
      DorpConfig.load();
      this.checkDependencies();
      LOGGER.info("Dorp Mod loaded!");
      BLOCKS.register(modEventBus);
      ITEMS.register(modEventBus);
      MOB_EFFECTS.register(modEventBus);
      BLOCK_ENTITIES.register(modEventBus);
      CREATIVE_MODE_TABS.register(modEventBus);
      SOUNDS.register(modEventBus);
      ENTITY_TYPES.register(modEventBus);
      GLM_SERIALIZERS.register(modEventBus);
      RECIPE_SERIALIZERS.register(modEventBus);
      modEventBus.addListener(DorpMod::onRegisterPayloads);
      modEventBus.addListener(DorpMod::registerCapabilities);
      modEventBus.addListener(DorpMod::onEntityAttributeCreation);
      NeoForge.EVENT_BUS.addListener(DorpMod::onBlockDrops);
      NeoForge.EVENT_BUS.addListener(DorpMod::onItemFinishUsing);
      NeoForge.EVENT_BUS.addListener(DorpMod::onPlayerTick);
      NeoForge.EVENT_BUS.addListener(DorpMod::onLivingDeath);
      NeoForge.EVENT_BUS.addListener(DorpMod::onPlayerClone);
      NeoForge.EVENT_BUS.addListener(DorpMod::onItemEntityTick);
      NeoForge.EVENT_BUS.addListener(ChatHandler::onServerChat);
      NeoForge.EVENT_BUS.addListener(DorpMod::onAttackEntity);
      NeoForge.EVENT_BUS.addListener(DorpMod::onBlockBreak);
      NeoForge.EVENT_BUS.addListener(DorpMod::onBlockPlace);
      NeoForge.EVENT_BUS.addListener(DorpMod::onRightClickBlock);
      NeoForge.EVENT_BUS.addListener(DorpMod::onLeftClickBlock);
      NeoForge.EVENT_BUS.addListener(ChatHandler::onCommand);
      NeoForge.EVENT_BUS.addListener(ChatHandler::onRegisterCommands);
      NeoForge.EVENT_BUS.addListener(HeadTorchLightHandler::onPlayerTick);
      NeoForge.EVENT_BUS.addListener(HeadTorchLightHandler::onPlayerLogout);
      NeoForge.EVENT_BUS.addListener(DorpMod::onBreakSpeed);
      NeoForge.EVENT_BUS.addListener(DorpMod::onLivingDamage);
      NeoForge.EVENT_BUS.addListener(WarModeManager::onLivingDeath);
      NeoForge.EVENT_BUS.addListener(WarModeManager::onTabListNameFormat);
      NeoForge.EVENT_BUS.addListener(WarModeManager::onPlayerLoggedIn);
      NeoForge.EVENT_BUS.addListener(DorpMod::onContainerOpen);
      NeoForge.EVENT_BUS.addListener(DorpMod::onItemCrafted);
      NeoForge.EVENT_BUS.addListener(DorpMod::onLivingEquipmentChange);
      NeoForge.EVENT_BUS.addListener(DorpMod::onElytraLaunchedDeath);
      NeoForge.EVENT_BUS.addListener(DorpMod::onPlayerLoggedIn);
      if (FMLEnvironment.dist.isClient()) {
         DorpModClient.init(modEventBus);
      }
   }

   private void checkDependencies() {
      List<String> missing = new ArrayList<>();
      List<String> links = new ArrayList<>();
      if (!ModList.get().isLoaded("ritchiesprojectilelib")) {
         missing.add("Ritchie's Projectile Library (RPL)");
         links.add("- Ritchie's Projectile Library (RPL): https://modrinth.com/mod/rpl");
      }

      if (!ModList.get().isLoaded("create")) {
         missing.add("Create Mod");
         links.add("- Create Mod: https://modrinth.com/mod/create");
      }

      if (!missing.isEmpty()) {
         StringBuilder sb = new StringBuilder();
         sb.append("Your game is missing required dependencies for Dorp Mod:\n\n");

         for (String link : links) {
            sb.append(link).append("\n");
         }

         sb.append("\nPlease download and install them in your mods folder to continue.");
         showNativeWarning(sb.toString(), "Dorp Mod - Missing Dependencies");
         throw new RuntimeException("Missing required dependencies: " + String.join(", ", missing));
      }
   }

   private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("dorp").optional();
      registrar.playToServer(CraftConcealerHeadPayload.TYPE, CraftConcealerHeadPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer serverPlayer) {
            handleCraftConcealerHead(serverPlayer, payload.targetName(), payload.targetUuid(), payload.texValue(), payload.texSig());
         }
      }));
      registrar.playToServer(ToggleHeadTorchPayload.TYPE, ToggleHeadTorchPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer serverPlayer) {
            HeadTorchItem.handleTogglePayload(serverPlayer);
         }
      }));
      registrar.playToServer(ModifyAuthListPayload.TYPE, ModifyAuthListPayload.STREAM_CODEC, ServerAuthHandler::handleModify);
      registrar.playToServer(PasswordPayloads.AuthMenuClosed.TYPE, PasswordPayloads.AuthMenuClosed.STREAM_CODEC, ServerAuthHandler::handleAuthMenuClosed);
      registrar.playToServer(
         PasswordPayloads.SubmitSetupPassword.TYPE, PasswordPayloads.SubmitSetupPassword.STREAM_CODEC, ServerAuthHandler::handleSubmitSetupPassword
      );
      registrar.playToServer(
         PasswordPayloads.SubmitEnterPassword.TYPE, PasswordPayloads.SubmitEnterPassword.STREAM_CODEC, ServerAuthHandler::handleSubmitEnterPassword
      );
      registrar.playToServer(PasswordPayloads.ToggleAutoAuth.TYPE, PasswordPayloads.ToggleAutoAuth.STREAM_CODEC, ServerAuthHandler::handleToggleAutoAuth);
      registrar.playToServer(
         PasswordPayloads.RequestAuthManager.TYPE, PasswordPayloads.RequestAuthManager.STREAM_CODEC, ServerAuthHandler::handleRequestAuthManager
      );
      registrar.playToServer(BreachChestPayload.TYPE, BreachChestPayload.STREAM_CODEC, ServerAuthHandler::handleBreachChest);
      registrar.playToClient(
         PasswordPayloads.ActiveLockedChest.TYPE,
         PasswordPayloads.ActiveLockedChest.STREAM_CODEC,
         (payload, context) -> ClientAuthHandler.handleActiveLockedChest(payload, context)
      );
      registrar.playToClient(
         OpenAuthManagerPayload.TYPE, OpenAuthManagerPayload.STREAM_CODEC, (payload, context) -> ClientAuthHandler.handleOpenPrompt(payload, context)
      );
      registrar.playToClient(
         PasswordPayloads.OpenSetupPassword.TYPE,
         PasswordPayloads.OpenSetupPassword.STREAM_CODEC,
         (payload, context) -> ClientAuthHandler.handleOpenSetupPassword(payload, context)
      );
      registrar.playToClient(
         PasswordPayloads.OpenEnterPassword.TYPE,
         PasswordPayloads.OpenEnterPassword.STREAM_CODEC,
         (payload, context) -> ClientAuthHandler.handleOpenEnterPassword(payload, context)
      );
      registrar.playToServer(UpdateWalkieTalkiePayload.TYPE, UpdateWalkieTalkiePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer serverPlayer) {
            WalkieTalkieItem.handleUpdatePayload(serverPlayer, payload.frequency(), payload.toggleActive(), payload.sendSos(), payload.sosMessage());
         }
      }));
      registrar.playToServer(
         UpdateRadioPayload.TYPE,
         UpdateRadioPayload.STREAM_CODEC,
         (payload, context) -> context.enqueueWork(
            () -> {
               if (context.player() instanceof ServerPlayer serverPlayer) {
                  Level level = serverPlayer.level();
                  if (level.isLoaded(payload.pos()) && level.getBlockEntity(payload.pos()) instanceof RadioBlockEntity radio) {
                     if (payload.toggleActive()) {
                        boolean nextState = !radio.isActive();
                        radio.setActive(nextState);
                        SoundEvent sound = nextState ? (SoundEvent)WALKIETALKIE_ON.get() : (SoundEvent)WALKIETALKIE_OFF.get();
                        level.playSound(null, payload.pos(), sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        Component msg = nextState
                           ? Component.translatable("actionbar.dorp.radio.on", new Object[]{payload.frequency()})
                           : Component.translatable("actionbar.dorp.radio.off");
                        serverPlayer.displayClientMessage(msg, true);
                     } else {
                        radio.setFrequency(payload.frequency());
                        Component msg = Component.translatable("actionbar.dorp.radio.on", new Object[]{payload.frequency()});
                        serverPlayer.displayClientMessage(msg, true);
                     }
                  }
               }
            }
         )
      );
      registrar.playToServer(UpdateHeadsetPayload.TYPE, UpdateHeadsetPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer serverPlayer) {
            HeadsetItem.handleUpdatePayload(serverPlayer, payload.frequency(), payload.toggleActive());
         }
      }));
      registrar.playToServer(
         UpdateInterceptorPayload.TYPE,
         UpdateInterceptorPayload.STREAM_CODEC,
         (payload, context) -> context.enqueueWork(
            () -> {
               if (context.player() instanceof ServerPlayer serverPlayer) {
                  Level level = serverPlayer.level();
                  if (level.isLoaded(payload.pos()) && level.getBlockEntity(payload.pos()) instanceof InterceptorBlockEntity interceptor) {
                     interceptor.setActive(payload.active());
                     Component msg = payload.active()
                        ? Component.translatable("actionbar.dorp.interceptor.on")
                        : Component.translatable("actionbar.dorp.interceptor.off");
                     serverPlayer.displayClientMessage(msg, true);
                  }
               }
            }
         )
      );
      registrar.playToServer(ElytraDisposePayload.TYPE, ElytraDisposePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer serverPlayer) {
            handleElytraDispose(serverPlayer);
         }
      }));
      registrar.playToServer(ElytraAcceptPayload.TYPE, ElytraAcceptPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer serverPlayer) {
            handleElytraAccept(serverPlayer);
         }
      }));
      registrar.playToServer(ScanPlayerPayload.TYPE, ScanPlayerPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer serverPlayer) {
            handleScanPlayer(serverPlayer, payload.blockPos());
         }
      }));
      if (FMLEnvironment.dist.isClient()) {
         ClientPayloadHandler.registerClientPayloads(registrar);
      } else {
         registerDummyClientPayloads(registrar);
      }
   }

   private static void registerDummyClientPayloads(PayloadRegistrar registrar) {
      registrar.playToClient(ScarePayload.TYPE, ScarePayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(RingScarePayload.TYPE, RingScarePayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(RingAmbientPayload.TYPE, RingAmbientPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(CrimsonWarningPayload.TYPE, CrimsonWarningPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(RingTotemPayload.TYPE, RingTotemPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(BsodPayload.TYPE, BsodPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(TimeStopPayload.TYPE, TimeStopPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(MonsterRoarPayload.TYPE, MonsterRoarPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(CameraFreezePayload.TYPE, CameraFreezePayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(StopwatchTotemPayload.TYPE, StopwatchTotemPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(RadioChatPayload.TYPE, RadioChatPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(ElytraWarningPayload.TYPE, ElytraWarningPayload.STREAM_CODEC, (payload, context) -> {});
      registrar.playToClient(ScannedInventoryPayload.TYPE, ScannedInventoryPayload.STREAM_CODEC, (payload, context) -> {});
   }

   private static void onItemFinishUsing(Finish event) {
      if (event.getItem().is((Item)BANANA_BURGER.get())) {
         if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.level().isClientSide()) {
               float roll = SCARE_RANDOM.nextFloat();
               LOGGER.debug("[BananaBurger] Scare roll: {} (triggers if < 0.20)", roll);
               if (!(roll >= 0.2F)) {
                  player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140, 0, false, false));
                  PacketDistributor.sendToPlayer(player, new ScarePayload(), new CustomPacketPayload[0]);
               }
            }
         }
      }
   }

   private static void onBlockDrops(BlockDropsEvent event) {
      if ((event.getState().is(net.minecraft.world.level.block.Blocks.JUNGLE_LEAVES) || event.getState().is(net.minecraft.world.level.block.Blocks.OAK_LEAVES))
         && event.getLevel().getRandom().nextFloat() < 0.008F) {
         Vec3 pos = event.getPos().getCenter();
         ServerLevel entity = event.getLevel();
         if (entity instanceof Level) {
            ItemEntity entityx = new ItemEntity(entity, pos.x, pos.y, pos.z, new ItemStack((ItemLike)BANANA.get()));
            event.getDrops().add(entityx);
         }
      }
   }

   public static void handleCraftConcealerHead(ServerPlayer player, String targetName, String uuidStr, String texValue, String texSig) {
      ItemStack held = player.getMainHandItem();
      boolean isMainHand = true;
      if (!held.is((Item)CONCEALER_HEAD.get())) {
         held = player.getOffhandItem();
         isMainHand = false;
      }

      if (held.is((Item)CONCEALER_HEAD.get())) {
         try {
            ItemStack skull = new ItemStack(net.minecraft.world.item.Items.PLAYER_HEAD);

            UUID realUuid;
            try {
               realUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException var11) {
               realUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + targetName).getBytes(StandardCharsets.UTF_8));
            }

            GameProfile profile = new GameProfile(realUuid, targetName);
            if (!texValue.isEmpty()) {
               profile.getProperties().put("textures", new Property("textures", texValue, texSig.isEmpty() ? null : texSig));
            }

            skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("concealer", true);
            skull.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            skull.set(DataComponents.CUSTOM_NAME, Component.literal(targetName + "'s Concealer Head"));
            held.shrink(1);
            if (isMainHand) {
               player.setItemInHand(InteractionHand.MAIN_HAND, held);
            } else {
               player.setItemInHand(InteractionHand.OFF_HAND, held);
            }

            if (!player.getInventory().add(skull)) {
               player.drop(skull, false);
            }
         } catch (Exception var12) {
            LOGGER.error("Failed to create concealer head for name '{}': {}", targetName, var12.getMessage());
            player.sendSystemMessage(
               Component.literal("[Concealer Head] Error creating head for '" + targetName + "'. Item refunded.").withStyle(ChatFormatting.RED)
            );
         }
      }
   }

   private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
      event.put((EntityType)CRIMSON_PHANTOM.get(), CrimsonPhantomEntity.createAttributes().build());
      event.put((EntityType)TEST_PLAYER_ENTITY.get(), TestPlayerEntity.createAttributes().build());
   }

   private static void onPlayerTick(Post event) {
      Player entityPlayer = event.getEntity();
      Level level = entityPlayer.level();
      if (!level.isClientSide()) {
         int slownessDelay = entityPlayer.getPersistentData().getInt("SledgehammerSlownessDelay");
         if (slownessDelay > 0) {
            if (--slownessDelay == 0) {
               entityPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
            }

            entityPlayer.getPersistentData().putInt("SledgehammerSlownessDelay", slownessDelay);
         }
      }

      int chargeTicks = entityPlayer.getPersistentData().getInt("SledgehammerChargeTicks");
      if (entityPlayer.getPersistentData().getInt("TimeStopTicks") > 0) {
         if (chargeTicks > 0) {
            entityPlayer.getPersistentData().putInt("SledgehammerChargeTicks", 0);
         }
      } else if (chargeTicks > 0) {
         entityPlayer.getPersistentData().putInt("SledgehammerChargeTicks", --chargeTicks);
         Vec3 look = entityPlayer.getLookAngle();
         entityPlayer.setDeltaMovement(look.x * 1.6, look.y * 0.3 + 0.1, look.z * 1.6);
         entityPlayer.hurtMarked = true;
         AABB bounds = entityPlayer.getBoundingBox().inflate(1.2, 1.0, 1.2);
         List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, bounds, e -> e != entityPlayer && e.isAlive());
         boolean hitSomeone = false;
         if (!targets.isEmpty()) {
            for (LivingEntity target : targets) {
               if (!(target instanceof ArmorStand)) {
                  if (!level.isClientSide()) {
                     target.hurt(level.damageSources().mobAttack(entityPlayer), 18.0F);
                     target.knockback(1.5, -look.x, -look.z);
                     level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 1.2F);
                     level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
                  }

                  hitSomeone = true;
               }
            }
         }

         if (hitSomeone) {
            entityPlayer.getPersistentData().putInt("SledgehammerChargeTicks", 0);
         } else {
            boolean hitWall = false;
            BlockHitResult blockHit = null;
            if (!level.isClientSide()) {
               Vec3 start = entityPlayer.getEyePosition();
               Vec3 end = start.add(look.x * 1.1, look.y * 0.3, look.z * 1.1);
               blockHit = level.clip(new ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, entityPlayer));
               if (blockHit.getType() == Type.BLOCK) {
                  Direction face = blockHit.getDirection();
                  if (face == Direction.NORTH || face == Direction.SOUTH || face == Direction.EAST || face == Direction.WEST) {
                     hitWall = true;
                  }
               }

               if (entityPlayer.horizontalCollision) {
                  hitWall = true;
               }
            }

            if (hitWall) {
               entityPlayer.getPersistentData().putInt("SledgehammerChargeTicks", 0);
               if (!level.isClientSide()) {
                  BlockPos playerPos = entityPlayer.blockPosition();
                  BlockPos basePos = blockHit != null && blockHit.getType() == Type.BLOCK
                     ? blockHit.getBlockPos()
                     : playerPos.relative(entityPlayer.getDirection());
                  int playerY = playerPos.getY();

                  for (BlockPos p : BlockPos.betweenClosed(
                     new BlockPos(basePos.getX() - 1, playerY, basePos.getZ() - 1), new BlockPos(basePos.getX() + 1, playerY + 2, basePos.getZ() + 1)
                  )) {
                     BlockState state = level.getBlockState(p);
                     float hardness = state.getDestroySpeed(level, p);
                     if (hardness >= 0.0F && hardness < 50.0F && !state.isAir()) {
                        level.destroyBlock(p, true);
                     }
                  }
               }

               if (!entityPlayer.isCreative() && !entityPlayer.isSpectator()) {
                  float maxHealth = entityPlayer.getMaxHealth();
                  float damageAmount = maxHealth * 0.7F;
                  float healthBefore = entityPlayer.getHealth();
                  float targetHealth = healthBefore - damageAmount;
                  entityPlayer.hurt(level.damageSources().flyIntoWall(), damageAmount);
                  if (entityPlayer.isAlive()) {
                     if (targetHealth <= 0.0F) {
                        entityPlayer.hurt(level.damageSources().flyIntoWall(), 10000.0F);
                     } else {
                        entityPlayer.setHealth(targetHealth);
                     }
                  }
               }

               entityPlayer.addEffect(new MobEffectInstance(CONCUSSION, 600, 0, false, false, true));
               level.playSound(null, entityPlayer.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.6F);
               level.playSound(null, entityPlayer.blockPosition(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 1.5F);
               if (level instanceof ServerLevel serverLevel) {
                  serverLevel.sendParticles(
                     ParticleTypes.EXPLOSION, entityPlayer.getX(), entityPlayer.getY() + 1.0, entityPlayer.getZ(), 20, 0.5, 0.5, 0.5, 0.1
                  );
                  serverLevel.sendParticles(ParticleTypes.CRIT, entityPlayer.getX(), entityPlayer.getY() + 1.0, entityPlayer.getZ(), 40, 1.0, 1.0, 1.0, 0.2);
               }
            } else if (chargeTicks == 0 && !level.isClientSide()) {
               entityPlayer.hurt(level.damageSources().flyIntoWall(), 6.0F);
               level.playSound(null, entityPlayer.blockPosition(), SoundEvents.PLAYER_BIG_FALL, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
         }
      }

      int timeStopTicks = entityPlayer.getPersistentData().getInt("TimeStopTicks");
      if (timeStopTicks > 0) {
         entityPlayer.getPersistentData().putInt("TimeStopTicks", --timeStopTicks);
         if (entityPlayer instanceof ServerPlayer serverPlayer) {
            if (timeStopTicks > 0) {
               int seconds = (int)Math.ceil(timeStopTicks / 20.0F);
               serverPlayer.displayClientMessage(Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.GOLD), true);
            } else {
               serverPlayer.setSilent(false);
               level.playSound(null, entityPlayer.blockPosition(), (SoundEvent)TIMERESUME.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
         }
      }

      if (entityPlayer instanceof ServerPlayer player) {
         if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel)level;
            long gameTime = serverLevel.getGameTime();
            UUID uid = player.getUUID();
            if (ELYTRA_REBELS.contains(player.getUUID()) && player.isFallFlying()) {
               onElytraPunishment(player);
            }

            int proneTicks = player.getPersistentData().getInt("CigaretteProneTicks");
            if (proneTicks > 0) {
               player.getPersistentData().putInt("CigaretteProneTicks", --proneTicks);
               player.setPose(Pose.SWIMMING);
            }

            boolean hasNicotineRush = player.hasEffect(NICOTINE_RUSH);
            boolean wasNicotineRushActive = player.getPersistentData().getBoolean("NicotineRushActive");
            if (hasNicotineRush) {
               if (!wasNicotineRushActive) {
                  player.getPersistentData().putBoolean("NicotineRushActive", true);
               }
            } else if (wasNicotineRushActive) {
               player.getPersistentData().putBoolean("NicotineRushActive", false);
               player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
               player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
               player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
               player.sendSystemMessage(
                  Component.literal("The nicotine rush fades, leaving you feeling sluggish and disoriented.").withStyle(ChatFormatting.DARK_GRAY)
               );
            }

            if (player.hasEffect(SMOKING_ADDICTION)) {
               int withdrawalTicks = player.getPersistentData().getInt("CigaretteWithdrawalTicks");
               if (withdrawalTicks > 0) {
                  player.getPersistentData().putInt("CigaretteWithdrawalTicks", --withdrawalTicks);
                  player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, true));
                  player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 0, false, false, true));
                  if (withdrawalTicks == 0) {
                     player.sendSystemMessage(Component.literal("The withdrawal symptoms finally fade.").withStyle(ChatFormatting.GREEN));
                  }
               } else {
                  int ticksSinceLastSmoke = player.getPersistentData().getInt("TicksSinceLastSmoke");
                  player.getPersistentData().putInt("TicksSinceLastSmoke", ++ticksSinceLastSmoke);
                  if (ticksSinceLastSmoke >= 24000) {
                     player.getPersistentData().putInt("CigaretteWithdrawalTicks", 72000);
                     player.getPersistentData().putInt("TicksSinceLastSmoke", 0);
                     player.sendSystemMessage(
                        Component.literal("You are experiencing nicotine withdrawal! You feel slow and weak.")
                           .withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD})
                     );
                  }
               }
            } else {
               player.getPersistentData().putInt("CigaretteWithdrawalTicks", 0);
               player.getPersistentData().putInt("TicksSinceLastSmoke", 0);
            }

            if (gameTime % 20L == 0L) {
               CuriosApi.getCuriosInventory(player)
                  .ifPresent(
                     handler -> handler.findFirstCurio((Item)HEAD_TORCH.get())
                        .ifPresent(
                           slotResult -> {
                              ItemStack stack = slotResult.stack();
                              if (HeadTorchItem.isActive(stack)) {
                                 int energy = HeadTorchItem.getEnergy(stack);
                                 if (energy > 0) {
                                    HeadTorchItem.setEnergy(stack, energy - 1);
                                    if (HeadTorchItem.getEnergy(stack) <= 0) {
                                       HeadTorchItem.setActive(stack, false);
                                       player.level()
                                          .playSound(
                                             null, player.getX(), player.getY(), player.getZ(), (SoundEvent)FLASHOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F
                                          );
                                       player.displayClientMessage(Component.literal("Head Torch has run out of energy!").withStyle(ChatFormatting.RED), true);
                                    }
                                 } else {
                                    HeadTorchItem.setActive(stack, false);
                                 }
                              }
                           }
                        )
                  );
            }

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            ItemStack viewerStack = ItemStack.EMPTY;
            if (mainHand.is((Item)TRACKER_VIEWER.get())) {
               viewerStack = mainHand;
            } else if (offHand.is((Item)TRACKER_VIEWER.get())) {
               viewerStack = offHand;
            }

            if (!viewerStack.isEmpty()) {
               CustomData data = (CustomData)viewerStack.get(DataComponents.CUSTOM_DATA);
               if (data != null) {
                  CompoundTag tag = data.copyTag();
                  if (tag.getBoolean("bricked")) {
                     player.displayClientMessage(
                        Component.literal("BRICKED - SIGNAL LOST").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}), true
                     );
                  } else if (tag.contains("TrackedPlayerName")) {
                     String name = tag.getString("TrackedPlayerName");
                     int ticksRemaining = 300 - (int)(gameTime % 300L);
                     double secondsRemaining = ticksRemaining / 20.0;
                     String timerStr = String.format("%.1fs", secondsRemaining);
                     if (tag.contains("LastX")) {
                        int x = tag.getInt("LastX");
                        int y = tag.getInt("LastY");
                        int z = tag.getInt("LastZ");
                        String arrow = "";
                        String tagArrow = tag.getString("Arrow");
                        if (tagArrow.equals("\ud83c\udf00")) {
                           arrow = "\ud83c\udf00";
                        } else if (tagArrow.equals("\ud83d\udcf6")) {
                           arrow = "\ud83d\udcf6";
                        } else {
                           double dx = x - player.getX();
                           double dz = z - player.getZ();
                           double angleToTarget = Math.toDegrees(Math.atan2(dz, dx));
                           double normalizedTargetAngle = (angleToTarget + 360.0) % 360.0;
                           double playerYaw = (player.getYRot() + 360.0F) % 360.0F;
                           double playerMathAngle = (playerYaw + 90.0) % 360.0;
                           double relativeAngle = (normalizedTargetAngle - playerMathAngle + 360.0) % 360.0;
                           arrow = "⬆";
                           if (relativeAngle >= 22.5 && relativeAngle < 67.5) {
                              arrow = "↗";
                           } else if (relativeAngle >= 67.5 && relativeAngle < 112.5) {
                              arrow = "➡";
                           } else if (relativeAngle >= 112.5 && relativeAngle < 157.5) {
                              arrow = "↘";
                           } else if (relativeAngle >= 157.5 && relativeAngle < 202.5) {
                              arrow = "⬇";
                           } else if (relativeAngle >= 202.5 && relativeAngle < 247.5) {
                              arrow = "↙";
                           } else if (relativeAngle >= 247.5 && relativeAngle < 292.5) {
                              arrow = "⬅";
                           } else if (relativeAngle >= 292.5 && relativeAngle < 337.5) {
                              arrow = "↖";
                           }
                        }

                        Component actionMessage = Component.literal("Tracking: ")
                           .withStyle(ChatFormatting.GRAY)
                           .append(Component.literal(name).withStyle(ChatFormatting.GREEN))
                           .append(Component.literal(" | Location: ").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal(String.format("X: %d Y: %d Z: %d", x, y, z)).withStyle(ChatFormatting.YELLOW))
                           .append(Component.literal(" | Direction: ").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal(arrow).withStyle(new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.BOLD}))
                           .append(Component.literal(" | Next Ping: ").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal(timerStr).withStyle(ChatFormatting.AQUA));
                        player.displayClientMessage(actionMessage, true);
                     } else {
                        Component actionMessage = Component.literal("Awaiting signal...")
                           .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.ITALIC})
                           .append(Component.literal(" | ID: " + tag.getInt("TrackerID")).withStyle(ChatFormatting.YELLOW))
                           .append(Component.literal(" | Next Ping: ").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal(timerStr).withStyle(ChatFormatting.AQUA));
                        player.displayClientMessage(actionMessage, true);
                     }
                  } else {
                     player.displayClientMessage(Component.literal("Unlinked Tracker Viewer").withStyle(ChatFormatting.GRAY), true);
                  }
               }
            }

            if (gameTime % 300L == 0L) {
               for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                  ItemStack stack = player.getInventory().getItem(i);
                  if (stack.is((Item)TRACKER_VIEWER.get())) {
                     CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
                     if (data != null) {
                        CompoundTag cachedTag = data.copyTag();
                        if (!cachedTag.getBoolean("bricked") && cachedTag.contains("TrackedPlayerUUID")) {
                           UUID targetUuid = cachedTag.getUUID("TrackedPlayerUUID");
                           int viewerId = cachedTag.getInt("TrackerID");
                           boolean isMob = cachedTag.getBoolean("IsMob");
                           boolean stillValid = false;
                           if (isMob) {
                              Entity targetx = null;

                              for (ServerLevel sl : serverLevel.getServer().getAllLevels()) {
                                 Entity e = sl.getEntity(targetUuid);
                                 if (e != null) {
                                    targetx = e;
                                    break;
                                 }
                              }

                              if (targetx != null) {
                                 if (targetx.isAlive() && targetx instanceof LivingEntity livingTarget) {
                                    int backId = livingTarget.getPersistentData().getInt("MobTrackerID");
                                    boolean bricked = livingTarget.getPersistentData().getBoolean("MobTrackerBricked");
                                    if (backId == viewerId && !bricked) {
                                       stillValid = true;
                                       CompoundTag tag = data.copyTag();
                                       tag.putInt("LastX", targetx.getBlockX());
                                       tag.putInt("LastY", targetx.getBlockY());
                                       tag.putInt("LastZ", targetx.getBlockZ());
                                       if (targetx.level().dimension() != player.level().dimension()) {
                                          tag.putString("Arrow", "\ud83c\udf00");
                                       } else {
                                          double dx = targetx.getX() - player.getX();
                                          double dz = targetx.getZ() - player.getZ();
                                          double angleToTarget = Math.toDegrees(Math.atan2(dz, dx));
                                          double normalizedTargetAngle = (angleToTarget + 360.0) % 360.0;
                                          double playerYaw = (player.getYRot() + 360.0F) % 360.0F;
                                          double playerMathAngle = (playerYaw + 90.0) % 360.0;
                                          double relativeAngle = (normalizedTargetAngle - playerMathAngle + 360.0) % 360.0;
                                          String arrow = "⬆";
                                          if (relativeAngle >= 22.5 && relativeAngle < 67.5) {
                                             arrow = "↗";
                                          } else if (relativeAngle >= 67.5 && relativeAngle < 112.5) {
                                             arrow = "➡";
                                          } else if (relativeAngle >= 112.5 && relativeAngle < 157.5) {
                                             arrow = "↘";
                                          } else if (relativeAngle >= 157.5 && relativeAngle < 202.5) {
                                             arrow = "⬇";
                                          } else if (relativeAngle >= 202.5 && relativeAngle < 247.5) {
                                             arrow = "↙";
                                          } else if (relativeAngle >= 247.5 && relativeAngle < 292.5) {
                                             arrow = "⬅";
                                          } else if (relativeAngle >= 292.5 && relativeAngle < 337.5) {
                                             arrow = "↖";
                                          }

                                          tag.putString("Arrow", arrow);
                                       }

                                       stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                    }
                                 }
                              } else {
                                 stillValid = true;
                                 CompoundTag tag = data.copyTag();
                                 tag.putString("Arrow", "\ud83d\udcf6");
                                 stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                              }
                           } else {
                              ServerPlayer targetx = serverLevel.getServer().getPlayerList().getPlayer(targetUuid);
                              if (targetx != null && targetx.isAlive()) {
                                 Optional<ICuriosItemHandler> curioInvOpt = CuriosApi.getCuriosInventory(targetx);
                                 if (curioInvOpt.isPresent()) {
                                    ICuriosItemHandler handler = curioInvOpt.get();
                                    Optional<ICurioStacksHandler> stacksHandlerOpt = handler.getStacksHandler("back");
                                    if (stacksHandlerOpt.isPresent()) {
                                       IDynamicStackHandler inventory = stacksHandlerOpt.get().getStacks();

                                       for (int j = 0; j < inventory.getSlots(); j++) {
                                          ItemStack backItem = inventory.getStackInSlot(j);
                                          if (backItem.is((Item)TRACKER.get()) && !TrackerItem.isBricked(backItem)) {
                                             CustomData backData = (CustomData)backItem.get(DataComponents.CUSTOM_DATA);
                                             int backId = backData != null ? backData.copyTag().getInt("TrackerID") : -1;
                                             if (backId == viewerId) {
                                                stillValid = true;
                                                CompoundTag tag = data.copyTag();
                                                tag.putInt("LastX", targetx.getBlockX());
                                                tag.putInt("LastY", targetx.getBlockY());
                                                tag.putInt("LastZ", targetx.getBlockZ());
                                                if (targetx.level().dimension() != player.level().dimension()) {
                                                   tag.putString("Arrow", "\ud83c\udf00");
                                                } else {
                                                   double dx = targetx.getX() - player.getX();
                                                   double dz = targetx.getZ() - player.getZ();
                                                   double angleToTarget = Math.toDegrees(Math.atan2(dz, dx));
                                                   double normalizedTargetAngle = (angleToTarget + 360.0) % 360.0;
                                                   double playerYaw = (player.getYRot() + 360.0F) % 360.0F;
                                                   double playerMathAngle = (playerYaw + 90.0) % 360.0;
                                                   double relativeAngle = (normalizedTargetAngle - playerMathAngle + 360.0) % 360.0;
                                                   String arrow = "⬆";
                                                   if (relativeAngle >= 22.5 && relativeAngle < 67.5) {
                                                      arrow = "↗";
                                                   } else if (relativeAngle >= 67.5 && relativeAngle < 112.5) {
                                                      arrow = "➡";
                                                   } else if (relativeAngle >= 112.5 && relativeAngle < 157.5) {
                                                      arrow = "↘";
                                                   } else if (relativeAngle >= 157.5 && relativeAngle < 202.5) {
                                                      arrow = "⬇";
                                                   } else if (relativeAngle >= 202.5 && relativeAngle < 247.5) {
                                                      arrow = "↙";
                                                   } else if (relativeAngle >= 247.5 && relativeAngle < 292.5) {
                                                      arrow = "⬅";
                                                   } else if (relativeAngle >= 292.5 && relativeAngle < 337.5) {
                                                      arrow = "↖";
                                                   }

                                                   tag.putString("Arrow", arrow);
                                                }

                                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                                break;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           if (!stillValid) {
                              cachedTag.putBoolean("bricked", true);
                              stack.set(DataComponents.CUSTOM_DATA, CustomData.of(cachedTag));
                           }
                        }
                     }
                  }
               }
            }

            boolean hasActiveTalkie = false;
            ItemStack walkieMainHand = player.getMainHandItem();
            ItemStack walkieOffHand = player.getOffhandItem();
            if (walkieMainHand.getItem() instanceof WalkieTalkieItem && WalkieTalkieItem.isActive(walkieMainHand)
               || walkieOffHand.getItem() instanceof WalkieTalkieItem && WalkieTalkieItem.isActive(walkieOffHand)) {
               hasActiveTalkie = true;
            }

            boolean nearActiveRadio = false;
            if (!hasActiveTalkie) {
               for (RadioBlockEntity radio : ChatHandler.ACTIVE_RADIOS) {
                  if (radio.getLevel() == level
                     && !radio.isRemoved()
                     && radio.isActive()
                     && DistanceHelper.distanceSq(level, radio.getBlockPos().getCenter(), player.position()) <= 49.0) {
                     nearActiveRadio = true;
                     break;
                  }
               }
            }

            if ((hasActiveTalkie || nearActiveRadio) && level.getMaxLocalRawBrightness(player.blockPosition()) <= 4) {
               int whisperTimer = player.getPersistentData().getInt("WhisperRadioTimer");
               if (++whisperTimer >= 12000) {
                  whisperTimer = 0;
                  if (player.getRandom().nextInt(50) == 0) {
                     triggerCrypticRadioMessage(player, hasActiveTalkie);
                  }
               }

               player.getPersistentData().putInt("WhisperRadioTimer", whisperTimer);
            }

            boolean sleepDeprived = player.getPersistentData().getBoolean("CrimsonSleepDeprived");
            boolean hasRing = false;
            int stage = 0;
            long lastRingScan = player.getPersistentData().getLong("LastRingScanTime");
            if (gameTime - lastRingScan < 20L && player.getPersistentData().contains("LastRingScanTime")) {
               hasRing = player.getPersistentData().getBoolean("CachedHasRing");
               stage = player.getPersistentData().getInt("CachedRingStage");
            } else {
               boolean found = false;

               for (int ix = 0; ix < player.getInventory().getContainerSize(); ix++) {
                  ItemStack s = player.getInventory().getItem(ix);
                  if (s.is((Item)CRIMSON_THREAD.get())) {
                     stage = CrimsonThreadItem.getStage(s);
                     found = true;
                     break;
                  }
               }

               if (!found) {
                  Optional<SlotResult> curioOpt = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio((Item)CRIMSON_THREAD.get()));
                  if (curioOpt.isPresent()) {
                     stage = CrimsonThreadItem.getStage(curioOpt.get().stack());
                     found = true;
                  }
               }

               hasRing = found;
               player.getPersistentData().putLong("LastRingScanTime", gameTime);
               player.getPersistentData().putBoolean("CachedHasRing", found);
               player.getPersistentData().putInt("CachedRingStage", stage);
            }

            ItemStack ring = ItemStack.EMPTY;
            if (hasRing) {
               for (int ixx = 0; ixx < player.getInventory().getContainerSize(); ixx++) {
                  ItemStack s = player.getInventory().getItem(ixx);
                  if (s.is((Item)CRIMSON_THREAD.get())) {
                     ring = s;
                     break;
                  }
               }

               if (ring.isEmpty()) {
                  Optional<SlotResult> curioOpt = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio((Item)CRIMSON_THREAD.get()));
                  if (curioOpt.isPresent()) {
                     ring = curioOpt.get().stack();
                  }
               }

               if (ring.isEmpty()) {
                  hasRing = false;
                  stage = 0;
                  player.getPersistentData().putBoolean("CachedHasRing", false);
                  player.getPersistentData().putInt("CachedRingStage", 0);
               }
            }

            if (hasRing && !sleepDeprived && player.getHealth() <= 8.0F && !player.getCooldowns().isOnCooldown(ring.getItem())) {
               boolean pendingJumpscare = player.getPersistentData().getBoolean("CrimsonPendingJumpscare");
               if (!pendingJumpscare && CrimsonThreadItem.getUses(ring) < 10) {
                  int count = 0;

                  for (int ixxx = 0; ixxx < player.getInventory().getContainerSize(); ixxx++) {
                     if (player.getInventory().getItem(ixxx).is((Item)CRIMSON_THREAD.get())) {
                        count++;
                     }
                  }

                  Optional<ICuriosItemHandler> curioInvOpt = CuriosApi.getCuriosInventory(player);
                  if (curioInvOpt.isPresent()) {
                     count += curioInvOpt.get().findCurios((Item)CRIMSON_THREAD.get()).size();
                  }

                  if (count <= 1) {
                     CrimsonThreadItem.activateRing(player, ring, serverLevel);
                     ring = ItemStack.EMPTY;

                     for (int ixxxx = 0; ixxxx < player.getInventory().getContainerSize(); ixxxx++) {
                        ItemStack s = player.getInventory().getItem(ixxxx);
                        if (s.is((Item)CRIMSON_THREAD.get())) {
                           ring = s;
                           break;
                        }
                     }

                     if (ring.isEmpty()) {
                        Optional<SlotResult> curioOpt = CuriosApi.getCuriosInventory(player)
                           .flatMap(handler -> handler.findFirstCurio((Item)CRIMSON_THREAD.get()));
                        if (curioOpt.isPresent()) {
                           ring = curioOpt.get().stack();
                        }
                     }

                     hasRing = !ring.isEmpty();
                     stage = ring.isEmpty() ? 0 : CrimsonThreadItem.getStage(ring);
                     player.getPersistentData().putBoolean("CachedHasRing", hasRing);
                     player.getPersistentData().putInt("CachedRingStage", stage);
                  } else {
                     player.displayClientMessage(Component.translatable("message.dorp.ring.reject_greed").withStyle(ChatFormatting.DARK_RED), true);
                  }
               }
            }

            boolean pendingJumpscare = player.getPersistentData().getBoolean("CrimsonPendingJumpscare");
            boolean jumpscareSpawned = player.getPersistentData().getBoolean("CrimsonJumpscareSpawned");
            if (pendingJumpscare && !jumpscareSpawned && gameTime >= player.getPersistentData().getLong("CrimsonJumpscareTime")) {
               player.getPersistentData().putBoolean("CrimsonJumpscareSpawned", true);
               spawnJumpscarePhantom(serverLevel, player);
            }

            if ((stage >= 3 || sleepDeprived) && player.isSleeping()) {
               player.stopSleeping();
               player.displayClientMessage(
                  Component.translatable("message.dorp.ring.cant_sleep").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.ITALIC}), true
               );
               Long lastSleepWarn = RING_SLEEP_WARNING_COOLDOWNS.get(uid);
               if (lastSleepWarn == null || gameTime - lastSleepWarn > 100L) {
                  RING_SLEEP_WARNING_COOLDOWNS.put(uid, gameTime);
                  if (hasRing) {
                     PacketDistributor.sendToPlayer(player, new CrimsonWarningPayload(), new CustomPacketPayload[0]);
                  }
               }
            }

            if (hasRing && (stage >= 3 || sleepDeprived)) {
               Long lastLateEvent = RING_LATE_EVENT_COOLDOWNS.get(uid);
               boolean lateEventReady = lastLateEvent == null || gameTime - lastLateEvent > 900L;
               if (lateEventReady && RING_SERVER_RANDOM.nextFloat() < 0.001F) {
                  RING_LATE_EVENT_COOLDOWNS.put(uid, gameTime);
                  int eventType = RING_SERVER_RANDOM.nextInt(2);
                  if (eventType == 0) {
                     String[] whisperMessages = new String[]{
                        "You hear a voice whispering in the dark...",
                        "A cold shiver runs down your spine.",
                        "Something is breathing behind you.",
                        "The shadows seem to stretch."
                     };
                     String chosenWhisper = whisperMessages[RING_SERVER_RANDOM.nextInt(whisperMessages.length)];
                     player.displayClientMessage(
                        Component.literal(chosenWhisper).withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.ITALIC}), true
                     );
                     SoundEvent[] whisperSounds = new SoundEvent[]{
                        (SoundEvent)RING_CANYOUHEARME.get(), (SoundEvent)RING_HESDOWN.get(), (SoundEvent)RING_CALMSTEPS.get()
                     };
                     SoundEvent chosenSound = whisperSounds[RING_SERVER_RANDOM.nextInt(whisperSounds.length)];
                     serverLevel.playSound(
                        null, player.getX(), player.getY(), player.getZ(), chosenSound, SoundSource.AMBIENT, 0.6F, 0.9F + RING_SERVER_RANDOM.nextFloat() * 0.2F
                     );
                     player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
                     if (hasRing) {
                        PacketDistributor.sendToPlayer(player, new CrimsonWarningPayload(), new CustomPacketPayload[0]);
                     }
                  } else {
                     player.displayClientMessage(
                        Component.literal("An icy grip freezes your bones...").withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.ITALIC}),
                        true
                     );
                     serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)RING_CALMSTEPS.get(), SoundSource.AMBIENT, 0.5F, 0.5F);
                     DustParticleOptions redDust = new DustParticleOptions(new Vector3f(0.8F, 0.0F, 0.0F), 1.5F);
                     serverLevel.sendParticles(redDust, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.5, 0.5, 0.01);
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
                     player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false));
                     if (hasRing) {
                        PacketDistributor.sendToPlayer(player, new CrimsonWarningPayload(), new CustomPacketPayload[0]);
                     }
                  }
               }
            }

            if (hasRing) {
               if (stage >= 2) {
                  long lastCaveSound = RING_CAVESOUND_COOLDOWNS.computeIfAbsent(uid, k -> 0L);
                  boolean isLowLight = serverLevel.getBrightness(LightLayer.BLOCK, player.blockPosition()) <= 4;
                  boolean isOutsideAtNight = !serverLevel.isDay() && serverLevel.canSeeSky(player.blockPosition());
                  if ((isLowLight || isOutsideAtNight) && gameTime - lastCaveSound >= 6000L) {
                     RING_CAVESOUND_COOLDOWNS.put(uid, gameTime);
                     SoundEvent[] cavePool = new SoundEvent[]{
                        (SoundEvent)RING_DISTANTSTEPS.get(),
                        (SoundEvent)RING_CANYOUHEARME.get(),
                        (SoundEvent)RING_HESDOWN.get(),
                        (SoundEvent)RING_DRIPSONG.get(),
                        (SoundEvent)RING_CALMSTEPS.get()
                     };
                     SoundEvent chosenSound = cavePool[RING_SERVER_RANDOM.nextInt(cavePool.length)];
                     serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), chosenSound, SoundSource.AMBIENT, 3.0F, 1.0F);
                  }

                  if (stage == 2) {
                     Long lastPhantom = RING_PHANTOM_COOLDOWNS.get(uid);
                     boolean phantomReady = lastPhantom == null || gameTime - lastPhantom > 600L;
                     if (phantomReady && RING_SERVER_RANDOM.nextFloat() < 0.002F) {
                        CrimsonThreadItem.spawnPhantom(serverLevel, player);
                        RING_PHANTOM_COOLDOWNS.put(uid, gameTime);
                     }
                  }

                  if (stage >= 3) {
                     Long lastEvent = RING_EVENT_COOLDOWNS.get(uid);
                     boolean eventReady = lastEvent == null || gameTime - lastEvent > 1200L;
                     boolean alone = serverLevel.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(20.0), px -> px != player).isEmpty();
                     if (alone && eventReady && RING_SERVER_RANDOM.nextFloat() < 0.001F) {
                        RING_EVENT_COOLDOWNS.put(uid, gameTime);
                        double angle = RING_SERVER_RANDOM.nextDouble() * Math.PI * 2.0;
                        double dist = 4.0 + RING_SERVER_RANDOM.nextDouble() * 4.0;
                        double ex = player.getX() + Math.cos(angle) * dist;
                        double ez = player.getZ() + Math.sin(angle) * dist;
                        CrimsonPhantomEntity phantom = (CrimsonPhantomEntity)((EntityType)CRIMSON_PHANTOM.get()).create(serverLevel);
                        if (phantom != null) {
                           phantom.setTargetPlayerUUID(uid);
                           phantom.moveTo(ex, player.getY(), ez, 0.0F, 0.0F);
                           serverLevel.addFreshEntity(phantom);
                           PacketDistributor.sendToPlayer(player, new RingScarePayload(ex, player.getEyeY(), ez), new CustomPacketPayload[0]);
                           PacketDistributor.sendToPlayer(player, new RingAmbientPayload(5), new CustomPacketPayload[0]);
                        }

                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
                        CrimsonThreadItem.spawnDepartureBeam(serverLevel, player.getX(), player.getY(), player.getZ(), 3);
                        double[] dest = CrimsonThreadItem.findTeleportDestination(serverLevel, player);
                        player.teleportTo(dest[0], dest[1], dest[2]);
                        CrimsonThreadItem.spawnArrivalBeam(serverLevel, dest[0], dest[1], dest[2], 3);
                        player.displayClientMessage(
                           Component.translatable("message.dorp.ring.ellipsis")
                              .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}),
                           true
                        );
                        if (hasRing) {
                           PacketDistributor.sendToPlayer(player, new CrimsonWarningPayload(), new CustomPacketPayload[0]);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void spawnJumpscarePhantom(ServerLevel level, ServerPlayer player) {
      double angle = RING_SERVER_RANDOM.nextDouble() * Math.PI * 2.0;
      double dist = 10.0 + RING_SERVER_RANDOM.nextDouble() * 5.0;
      double ex = player.getX() + Math.cos(angle) * dist;
      double ez = player.getZ() + Math.sin(angle) * dist;
      CrimsonPhantomEntity phantom = (CrimsonPhantomEntity)((EntityType)CRIMSON_PHANTOM.get()).create(level);
      if (phantom != null) {
         phantom.setTargetPlayerUUID(player.getUUID());
         phantom.moveTo(ex, player.getY(), ez, 0.0F, 0.0F);
         phantom.setJumpscare(true);
         level.addFreshEntity(phantom);
         PacketDistributor.sendToPlayer(player, new RingAmbientPayload(3), new CustomPacketPayload[0]);
      }
   }

   private static void triggerCrypticRadioMessage(ServerPlayer player, boolean isWalkieTalkie) {
      String[] messages = new String[]{
         "help us",
         "they are in the walls",
         "turn it off. it can hear you",
         "is anyone there?",
         "subject detected at depth " + player.getBlockY(),
         "coordinates: " + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ() + " ... no signs of life",
         "the dark... it's moving",
         "do not look back",
         "it is so cold down here",
         "alert: containment breach at sector 4"
      };
      String randomMsg = messages[player.getRandom().nextInt(messages.length)];
      Component prefix = isWalkieTalkie
         ? Component.literal("[Walkie-Talkie] ").withStyle(ChatFormatting.DARK_GRAY)
         : Component.literal("[Radio] ").withStyle(ChatFormatting.DARK_GRAY);
      Component fullMsg = Component.empty()
         .append(prefix)
         .append(Component.literal("... *static* ... \"" + randomMsg + "\" ...").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
      player.sendSystemMessage(fullMsg);
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)WALKIETALKIE_ON.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
   }

   private static void onPlayerClone(Clone event) {
      CompoundTag oldData = event.getOriginal().getPersistentData();
      CompoundTag newData = event.getEntity().getPersistentData();
      if (oldData.contains("DorpCustomDisplayName")) {
         newData.putString("DorpCustomDisplayName", oldData.getString("DorpCustomDisplayName"));
         event.getEntity().refreshDisplayName();
      }

      if (oldData.contains("DorpHasCustomName")) {
         newData.putBoolean("DorpHasCustomName", oldData.getBoolean("DorpHasCustomName"));
      }

      if (oldData.contains("CigaretteSmokeTimes")) {
         newData.putLongArray("CigaretteSmokeTimes", oldData.getLongArray("CigaretteSmokeTimes"));
      }

      if (oldData.contains("CigaretteHeavyDays")) {
         newData.putLongArray("CigaretteHeavyDays", oldData.getLongArray("CigaretteHeavyDays"));
      }

      if (oldData.contains("CigarettesSmokedToday")) {
         newData.putInt("CigarettesSmokedToday", oldData.getInt("CigarettesSmokedToday"));
      }

      if (oldData.contains("CigaretteLastSmokeDay")) {
         newData.putLong("CigaretteLastSmokeDay", oldData.getLong("CigaretteLastSmokeDay"));
      }

      if (oldData.contains("NicotineRushActive")) {
         newData.putBoolean("NicotineRushActive", oldData.getBoolean("NicotineRushActive"));
      }

      if (oldData.contains("CigaretteProneTicks")) {
         newData.putInt("CigaretteProneTicks", oldData.getInt("CigaretteProneTicks"));
      }

      if (oldData.contains("CigaretteWithdrawalTicks")) {
         newData.putInt("CigaretteWithdrawalTicks", oldData.getInt("CigaretteWithdrawalTicks"));
      }

      if (oldData.contains("TicksSinceLastSmoke")) {
         newData.putInt("TicksSinceLastSmoke", oldData.getInt("TicksSinceLastSmoke"));
      }

      if (event.getOriginal().hasEffect(SMOKING_ADDICTION)) {
         event.getEntity().addEffect(new MobEffectInstance(SMOKING_ADDICTION, -1, 0, false, false, true));
      }

      if (!event.isWasDeath()) {
         boolean sleepDeprived = event.getOriginal().getPersistentData().getBoolean("CrimsonSleepDeprived");
         if (sleepDeprived) {
            event.getEntity().getPersistentData().putBoolean("CrimsonSleepDeprived", true);
         }

         boolean pendingJumpscare = event.getOriginal().getPersistentData().getBoolean("CrimsonPendingJumpscare");
         if (pendingJumpscare) {
            event.getEntity().getPersistentData().putBoolean("CrimsonPendingJumpscare", true);
            event.getEntity().getPersistentData().putLong("CrimsonJumpscareTime", event.getOriginal().getPersistentData().getLong("CrimsonJumpscareTime"));
         }

         boolean jumpscareSpawned = event.getOriginal().getPersistentData().getBoolean("CrimsonJumpscareSpawned");
         if (jumpscareSpawned) {
            event.getEntity().getPersistentData().putBoolean("CrimsonJumpscareSpawned", true);
         }
      } else {
         UUID uid = event.getEntity().getUUID();
         event.getEntity().getPersistentData().remove("CrimsonSleepDeprived");
         event.getEntity().getPersistentData().remove("CrimsonPendingJumpscare");
         event.getEntity().getPersistentData().remove("CrimsonJumpscareSpawned");
         event.getEntity().getPersistentData().remove("CrimsonJumpscareTime");
         RING_PHANTOM_COOLDOWNS.remove(uid);
         RING_EVENT_COOLDOWNS.remove(uid);
         RING_CAVESOUND_COOLDOWNS.remove(uid);
         RING_LATE_EVENT_COOLDOWNS.remove(uid);
         RING_SLEEP_WARNING_COOLDOWNS.remove(uid);
      }
   }

   private static void onBreakSpeed(BreakSpeed event) {
      if (event.getEntity().hasEffect(NICOTINE_RUSH)) {
         int amp = event.getEntity().getEffect(NICOTINE_RUSH).getAmplifier();
         event.setNewSpeed(event.getNewSpeed() * (1.0F + 0.2F * (amp + 1)));
      }
   }

   private static void onLivingDamage(Pre event) {
      if (event.getEntity().hasEffect(NICOTINE_RUSH)) {
         int amp = event.getEntity().getEffect(NICOTINE_RUSH).getAmplifier();
         float factor = Math.max(0.0F, 1.0F - 0.2F * (amp + 1));
         event.setNewDamage(event.getNewDamage() * factor);
      }

      if (event.getEntity() instanceof Player player) {
         ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
         ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
         if (main.is((Item)ANOMALOUS_SLEDGEHAMMER.get()) || off.is((Item)ANOMALOUS_SLEDGEHAMMER.get())) {
            DamageSource source = event.getSource();
            if (source.getDirectEntity() instanceof Projectile && player.getRandom().nextFloat() < 0.5F) {
               event.setNewDamage(0.0F);
               player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
               if (player.level() instanceof ServerLevel serverLevel) {
                  serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
               }
            }
         }
      }
   }

   public static void showNativeWarning(String message, String title) {
      if (!FMLEnvironment.dist.isClient()) {
         LOGGER.error("[{}] {}", title, message);
      } else {
         try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
               File tempFile = File.createTempFile("dorp_warning", ".vbs");
               tempFile.deleteOnExit();

               try (FileWriter writer = new FileWriter(tempFile)) {
                  writer.write(
                     "MsgBox \"" + message.replace("\"", "\"\"").replace("\n", "\" & vbCrLf & \"") + "\", 16, \"" + title.replace("\"", "\"\"") + "\""
                  );
               }

               Runtime.getRuntime().exec(new String[]{"wscript.exe", tempFile.getAbsolutePath()});
            } else {
               new Thread(() -> JOptionPane.showMessageDialog(null, message, title, 0)).start();
            }
         } catch (Exception var9) {
            LOGGER.error("[{}] Failed to show native dialog: {}", title, var9.getMessage());
         }
      }
   }

   private static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(ItemHandler.BLOCK, (BlockEntityType)LOCKED_CHEST_BLOCK_ENTITY.get(), (blockEntity, side) -> null);
      event.registerItem(
         EnergyStorage.ITEM,
         (stack, context) -> new ItemEnergyStorage(stack, DorpConfig.WALKIE_TALKIE_MAX_ENERGY.get()),
         new ItemLike[]{(ItemLike)WALKIE_TALKIE.get()}
      );
      event.registerItem(
         EnergyStorage.ITEM, (stack, context) -> new ItemEnergyStorage(stack, DorpConfig.HEADSET_MAX_ENERGY.get()), new ItemLike[]{(ItemLike)HEADSET.get()}
      );
      event.registerItem(EnergyStorage.ITEM, (stack, context) -> new ItemEnergyStorage(stack, 7200), new ItemLike[]{(ItemLike)HEAD_TORCH.get()});
       event.registerBlockEntity(EnergyStorage.BLOCK, INTERCEPTOR_BLOCK_ENTITY.get(), (blockEntity, context) -> {
         if (context == null) {
            return blockEntity;
         } else {
            if (blockEntity.getBlockState().hasProperty(InterceptorBlock.FACING)) {
               Direction facing = (Direction)blockEntity.getBlockState().getValue(InterceptorBlock.FACING);
               if (context == facing.getClockWise()) {
                  return blockEntity;
               }
            }

            return null;
         }
      });
   }

   private static void onAttackEntity(AttackEntityEvent event) {
      if (event.getEntity().getPersistentData().getInt("TimeStopTicks") > 0 && event.getTarget() instanceof Player) {
         event.setCanceled(true);
      }
   }

   private static void onBlockBreak(BreakEvent event) {
      if (event.getPlayer().getPersistentData().getInt("TimeStopTicks") > 0) {
         event.setCanceled(true);
      }
   }

   private static void onBlockPlace(EntityPlaceEvent event) {
      if (event.getEntity() instanceof Player player && player.getPersistentData().getInt("TimeStopTicks") > 0) {
         event.setCanceled(true);
      }
   }

   private static void onRightClickBlock(RightClickBlock event) {
      if (event.getEntity().getPersistentData().getInt("TimeStopTicks") > 0) {
         event.setCanceled(true);
      }
   }

   private static void onLeftClickBlock(LeftClickBlock event) {
      if (event.getEntity().getPersistentData().getInt("TimeStopTicks") > 0) {
         event.setCanceled(true);
      }
   }

   private static void onLivingDeath(LivingDeathEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         LivingEntity entity = event.getEntity();
         MinecraftServer server = entity.getServer();
         if (server != null) {
            if (entity instanceof ServerPlayer player) {
               for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                  ItemStack stack = player.getInventory().getItem(i);
                  if (stack.is((Item)TRACKER.get())) {
                     TrackerItem.setBricked(stack, true);
                  }
               }

               Optional<ICuriosItemHandler> curioInvOpt = CuriosApi.getCuriosInventory(player);
               if (curioInvOpt.isPresent()) {
                  ICuriosItemHandler handler = curioInvOpt.get();
                  Optional<ICurioStacksHandler> stacksHandlerOpt = handler.getStacksHandler("back");
                  if (stacksHandlerOpt.isPresent()) {
                     IDynamicStackHandler inventory = stacksHandlerOpt.get().getStacks();

                     for (int j = 0; j < inventory.getSlots(); j++) {
                        ItemStack backItem = inventory.getStackInSlot(j);
                        if (backItem.is((Item)TRACKER.get())) {
                           TrackerItem.setBricked(backItem, true);
                        }
                     }
                  }
               }

               brickViewersForTarget(server, player.getUUID());
            } else {
               CompoundTag persist = entity.getPersistentData();
               if (persist.contains("MobTrackerID")) {
                  persist.putBoolean("MobTrackerBricked", true);
                  brickViewersForTarget(server, entity.getUUID());
               }
            }
         }
      }
   }

   public static void brickViewersForTarget(MinecraftServer server, UUID targetUuid) {
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         boolean updated = false;

         for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is((Item)TRACKER_VIEWER.get())) {
               CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
               if (data != null && !data.copyTag().getBoolean("bricked")) {
                  CompoundTag tag = data.copyTag();
                  if (tag.contains("TrackedPlayerUUID") && tag.getUUID("TrackedPlayerUUID").equals(targetUuid)) {
                     tag.putBoolean("bricked", true);
                     stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                     updated = true;
                  }
               }
            }
         }

         if (updated) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal("A tracked target has died! Your tracker viewer has been bricked.").withStyle(ChatFormatting.RED));
         }
      }
   }

   public static boolean isForbiddenOnly(Item item) {
      return item == INTERCEPTOR.get()
         || item == DIEGO_STOPWATCH.get()
         || item == CRIMSON_THREAD.get()
         || item == ANOMALOUS_SLEDGEHAMMER.get()
         || item == BREACHER.get();
   }

   private static void onContainerOpen(Open event) {
      AbstractContainerMenu menu = event.getContainer();
      if (menu instanceof RecipeBookMenu && !(menu instanceof ForbiddenWorkbenchMenu)) {
         menu.addSlotListener(new ContainerListener() {
            public void slotChanged(AbstractContainerMenu menu, int slotNum, ItemStack stack) {
               if (slotNum == 0 && !stack.isEmpty() && DorpMod.isForbiddenOnly(stack.getItem())) {
                  menu.getSlot(0).set(ItemStack.EMPTY);
               }
            }

            public void dataChanged(AbstractContainerMenu menu, int id, int value) {
            }
         });
      }
   }

   private static void onItemEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
      if (event.getEntity() instanceof ItemEntity itemEntity) {
         Level level = itemEntity.level();
         if (!level.isClientSide()) {
            ItemStack stack = itemEntity.getItem();
            if (stack.is((Item)CIGARETTE.get())) {
               if (itemEntity.onGround()) {
                  CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
                  if (data != null) {
                     CompoundTag tag = data.copyTag();
                     if (tag.getBoolean("used")) {
                        BlockPos pos = itemEntity.blockPosition();
                        if (level.getBlockState(pos).isAir()) {
                           level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
                        } else if (level.getBlockState(pos.above()).isAir()) {
                           level.setBlockAndUpdate(pos.above(), net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
                        }

                        itemEntity.discard();
                     }
                  }
               }
            }
         }
      }
   }

   private static void onItemCrafted(ItemCraftedEvent event) {
      Player player = event.getEntity();
      if (player.containerMenu instanceof ForbiddenWorkbenchMenu menu) {
         menu.access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
               serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 15, 0.5, 0.2, 0.5, 0.1);
               serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 10, 0.5, 0.2, 0.5, 0.1);
               serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
         });
      }
   }

   private static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (!player.level().isClientSide()) {
            EquipmentSlot slot = event.getSlot();
            if (slot == EquipmentSlot.CHEST) {
               ItemStack newStack = event.getTo();
               if (newStack.is(net.minecraft.world.item.Items.ELYTRA)) {
                  if (!ELYTRA_REBELS.contains(player.getUUID())) {
                     ItemStack held = newStack.copy();
                     player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                     ELYTRA_PENDING.put(player.getUUID(), held);
                     PacketDistributor.sendToPlayer(player, new ElytraWarningPayload(), new CustomPacketPayload[0]);
                  }
               }
            }
         }
      }
   }

   private static void handleElytraDispose(ServerPlayer player) {
      ELYTRA_PENDING.remove(player.getUUID());

      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         if (player.getInventory().getItem(i).is(net.minecraft.world.item.Items.ELYTRA)) {
            player.getInventory().removeItemNoUpdate(i);
            break;
         }
      }

      player.addItem(new ItemStack(net.minecraft.world.item.Items.DIAMOND, 2));
      player.displayClientMessage(Component.literal("smart choice. here's 2 diamonds for your trouble.").withStyle(ChatFormatting.GREEN), false);
   }

   private static void handleElytraAccept(ServerPlayer player) {
      ItemStack pending = ELYTRA_PENDING.remove(player.getUUID());
      if (pending != null && !pending.isEmpty()) {
         ELYTRA_REBELS.add(player.getUUID());
         player.setItemSlot(EquipmentSlot.CHEST, pending);
         player.displayClientMessage(
            Component.literal("suit yourself...").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}), false
         );
      }
   }

   private static void handleScanPlayer(ServerPlayer serverPlayer, BlockPos blockPos) {
      Level level = serverPlayer.level();
      long gameTime = level.getGameTime();
      if (gameTime >= SCAN_COOLDOWNS.getOrDefault(serverPlayer.getUUID(), 0L)) {
         SCAN_COOLDOWNS.put(serverPlayer.getUUID(), gameTime + 20L);
         if (level.isLoaded(blockPos)) {
            BlockState state = level.getBlockState(blockPos);
            if (state.is((Block)INVENTORY_CHECKER_BLOCK.get())) {
               Direction facing = (Direction)state.getValue(InventoryCheckerBlock.FACING);
               Direction behind = facing.getOpposite();
               BlockPos centerBehind = blockPos.relative(behind);
               AABB aabb = new AABB(centerBehind).inflate(1.5, 1.5, 1.5);
               List<Player> players = level.getEntitiesOfClass(Player.class, aabb);
               CompoundTag resultData = new CompoundTag();

               for (Player scanned : players) {
                  CompoundTag invTag = new CompoundTag();
                  ListTag itemList = new ListTag();

                  for (int i = 0; i < scanned.getInventory().getContainerSize(); i++) {
                     ItemStack stack = scanned.getInventory().getItem(i);
                     if (!stack.isEmpty()) {
                        CompoundTag itemWrapper = new CompoundTag();
                        itemWrapper.putInt("Slot", i);
                        itemWrapper.put("Item", stack.saveOptional(serverPlayer.registryAccess()));
                        itemList.add(itemWrapper);
                     }
                  }

                  invTag.put("Items", itemList);
                  resultData.put(scanned.getName().getString(), invTag);
               }

               String singleName = players.size() == 1 ? players.get(0).getName().getString() : "";
               PacketDistributor.sendToPlayer(serverPlayer, new ScannedInventoryPayload(singleName, resultData), new CustomPacketPayload[0]);
            }
         }
      }
   }

   private static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         ServerPlayer player = (ServerPlayer)event.getEntity();
         CompoundTag data = player.getPersistentData();
         if (!data.getBoolean("dorp_guide_given")) {
            data.putBoolean("dorp_guide_given", true);
            player.getInventory().add(new ItemStack((ItemLike)DORP_GUIDE.get()));
         }
      }
   }

   private static void onElytraPunishment(ServerPlayer player) {
      ELYTRA_REBELS.remove(player.getUUID());
      ELYTRA_LAUNCHED.add(player.getUUID());
      int buildLimit = player.level().getMaxBuildHeight() - 2;
      player.teleportTo(player.getX(), buildLimit, player.getZ());
      player.setDeltaMovement(0.0, 3.0, 0.0);
      player.hurtMarked = true;
      ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
      if (chestStack.is(net.minecraft.world.item.Items.ELYTRA) && chestStack.isDamageableItem()) {
         chestStack.setDamageValue(chestStack.getMaxDamage());
         player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      }

      player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 500, 0, false, false));
      player.displayClientMessage(Component.literal("i warned you").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}), false);
      Component announcement = Component.literal(player.getName().getString() + " tried to use elytra even when it wasn't allowed")
         .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD});
      if (player.getServer() != null) {
         player.getServer().getPlayerList().broadcastSystemMessage(announcement, false);
      }
   }

   private static void onElytraLaunchedDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (ELYTRA_LAUNCHED.contains(player.getUUID())) {
            DamageSource src = event.getSource();
            boolean isFallOrVoid = src.is(DamageTypes.FALL) || src.is(DamageTypes.FELL_OUT_OF_WORLD) || src.is(DamageTypes.OUTSIDE_BORDER);
            if (isFallOrVoid) {
               ELYTRA_LAUNCHED.remove(player.getUUID());
               Component deathMsg = Component.literal(
                     player.getName().getString() + " died because they thought they could use the elytra even when it was banned from the SMP"
                  )
                  .withStyle(ChatFormatting.RED);
               if (player.getServer() != null) {
                  player.getServer().getPlayerList().broadcastSystemMessage(deathMsg, false);
               }
            }
         }
      }
   }
}
