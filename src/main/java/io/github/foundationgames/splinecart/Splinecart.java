package io.github.foundationgames.splinecart;

import io.github.foundationgames.splinecart.block.TrackTiesBlock;
import io.github.foundationgames.splinecart.block.TrackTiesBlockEntity;
import io.github.foundationgames.splinecart.component.OriginComponent;
import io.github.foundationgames.splinecart.entity.TrackFollowerEntity;
import io.github.foundationgames.splinecart.item.TrackItem;
import io.github.foundationgames.splinecart.util.TrackProgress;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

@Mod(Splinecart.MOD_ID)
public class Splinecart {
    public static final String MOD_ID = "splinecart";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Deferred Registers
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredRegister<net.minecraft.network.syncher.EntityDataSerializer<?>> DATA_SERIALIZERS = 
            DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, MOD_ID);

    // Blocks
    public static final DeferredBlock<TrackTiesBlock> TRACK_TIES = BLOCKS.register("track_ties",
            () -> new TrackTiesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));

    // Block Entity Types
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrackTiesBlockEntity>> TRACK_TIES_BE = 
            BLOCK_ENTITY_TYPES.register("track_ties",
                    () -> BlockEntityType.Builder.of(TrackTiesBlockEntity::new, TRACK_TIES.get()).build(null));

    // Items
    public static final DeferredItem<TrackItem> TRACK = ITEMS.register("track",
            () -> new TrackItem(TrackType.DEFAULT, new Item.Properties()
                    .component(DataComponents.LORE, lore(Component.translatable("item.splinecart.track.desc").withStyle(net.minecraft.ChatFormatting.GRAY)))));
    
    public static final DeferredItem<TrackItem> CHAIN_DRIVE_TRACK = ITEMS.register("chain_drive_track",
            () -> new TrackItem(TrackType.CHAIN_DRIVE, new Item.Properties()
                    .component(DataComponents.LORE, lore(Component.translatable("item.splinecart.chain_drive_track.desc").withStyle(net.minecraft.ChatFormatting.GRAY)))));
    
    public static final DeferredItem<TrackItem> MAGNETIC_TRACK = ITEMS.register("magnetic_track",
            () -> new TrackItem(TrackType.MAGNETIC, new Item.Properties()
                    .component(DataComponents.LORE, lore(Component.translatable("item.splinecart.magnetic_track.desc").withStyle(net.minecraft.ChatFormatting.GRAY)))));
    
    public static final DeferredItem<BlockItem> TRACK_TIES_ITEM = ITEMS.register("track_ties",
            () -> new BlockItem(TRACK_TIES.get(), new Item.Properties()
                    .component(DataComponents.LORE, lore(Component.translatable("item.splinecart.track_ties.desc").withStyle(net.minecraft.ChatFormatting.GRAY)))));

    // Data Component Types
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<OriginComponent>> ORIGIN_POS = 
            DATA_COMPONENT_TYPES.register("origin",
                    () -> DataComponentType.<OriginComponent>builder().persistent(OriginComponent.CODEC).build());

    // Entity Types
    public static final DeferredHolder<EntityType<?>, EntityType<TrackFollowerEntity>> TRACK_FOLLOWER = 
            ENTITY_TYPES.register("track_follower",
                    () -> EntityType.Builder.<TrackFollowerEntity>of(TrackFollowerEntity::new, MobCategory.MISC)
                            .updateInterval(2)
                            .sized(0.25f, 0.25f)
                            .build(id("track_follower").toString()));

    // Entity Data Serializers
    public static final DeferredHolder<net.minecraft.network.syncher.EntityDataSerializer<?>, net.minecraft.network.syncher.EntityDataSerializer<TrackProgress>> TRACK_PROGRESS_SERIALIZER =
            DATA_SERIALIZERS.register("track_progress", () -> TrackProgress.DATA_HANDLER);

    // Tags
    public static final TagKey<EntityType<?>> CARTS = TagKey.create(Registries.ENTITY_TYPE, id("carts"));

    // Creative Mode Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPLINECART_TAB = CREATIVE_MODE_TABS.register("splinecart",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.splinecart"))
                    .icon(() -> CHAIN_DRIVE_TRACK.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(TRACK_TIES_ITEM.get().getDefaultInstance());
                        output.accept(TRACK.get().getDefaultInstance());
                        output.accept(CHAIN_DRIVE_TRACK.get().getDefaultInstance());
                        output.accept(MAGNETIC_TRACK.get().getDefaultInstance());
                    })
                    .build());

    public Splinecart(IEventBus modEventBus) {
        // Register all deferred registers
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        DATA_COMPONENT_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_SERIALIZERS.register(modEventBus);

        // Register event handlers
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(TRACK_TIES_ITEM);
            event.accept(TRACK);
            event.accept(CHAIN_DRIVE_TRACK);
            event.accept(MAGNETIC_TRACK);
        }
    }

    public static ItemLore lore(Component loreText) {
        return new ItemLore(List.of(loreText));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}