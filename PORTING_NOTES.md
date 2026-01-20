# Splinecart NeoForge 1.21.1 Porting Notes

## Mod Inventory

### Basic Info
- **Mod ID**: `splinecart`
- **Version**: `0.3.1+1.21.1`
- **Maven Group**: `io.github.foundationgames`
- **Archive Name**: `splinecart`
- **Minecraft Version**: `1.21.1`
- **Java Version**: `21`

### Entrypoints (Fabric)
- **Main**: `io.github.foundationgames.splinecart.Splinecart` (implements `ModInitializer`)
- **Client**: `io.github.foundationgames.splinecart.SplinecartClient` (implements `ClientModInitializer`)

### Registry Types Used
| Registry Type | Items | Notes |
|--------------|-------|-------|
| Blocks | `track_ties` (TrackTiesBlock) | Rail-like block |
| Block Entity Types | `track_ties` (TrackTiesBlockEntity) | Uses FabricBlockEntityTypeBuilder |
| Items | `track`, `chain_drive_track`, `magnetic_track`, `track_ties` | TrackItem instances + BlockItem |
| Entity Types | `track_follower` (TrackFollowerEntity) | Custom entity for track following |
| Data Component Types | `origin` (OriginComponent) | Item component with BlockPos |
| Item Groups | `splinecart` (custom creative tab) | Uses FabricItemGroup.builder() |
| Tracked Data Handlers | `TrackProgress.DATA_HANDLER` | Custom data handler for entity syncing |
| Tags | `splinecart:carts` (entity_type) | Entity type tag |

### Fabric API Events/Callbacks Used
| Fabric API | Usage | NeoForge Equivalent |
|-----------|-------|---------------------|
| `ItemGroupEvents.modifyEntriesEvent()` | Add items to Redstone tab | `BuildCreativeModeTabContentsEvent` |
| `BlockRenderLayerMap.INSTANCE.putBlock()` | Set block render layer | `RegisterRenderLayersEvent` / `ItemBlockRenderTypes` |
| `BlockEntityRendererFactories.register()` | Register BE renderer | `EntityRenderersEvent.RegisterRenderers` |
| `EntityRendererRegistry.register()` | Register entity renderer | `EntityRenderersEvent.RegisterRenderers` |
| `ClientCommandRegistrationCallback.EVENT` | Register client commands | `RegisterClientCommandsEvent` |
| `HudRenderCallback.EVENT` | Render HUD overlay | `RenderGuiLayerEvent.Post` or custom layer |
| `FabricLoader.getInstance().getConfigDir()` | Get config directory | `FMLPaths.CONFIGDIR.get()` |

### Networking/Payloads
- No custom networking packets found
- Uses `TrackedDataHandler` for entity data syncing (built into Minecraft)

### Mixins
#### Common Mixins (`splinecart.mixins.json`)
| Mixin Class | Target | Injections | Purpose |
|------------|--------|------------|---------|
| `EntityMixin` | `Entity` | `@Inject(setPosition)`, `@ModifyReturnValue(getRotationVector)`, `@ModifyReturnValue(getCameraPosVec)`, `@Inject(getEyePos)` | Make minecarts mount TrackFollower, adjust rotation/camera |

#### Client Mixins (`splinecart.client.mixins.json`)
| Mixin Class | Target | Injections | Purpose |
|------------|--------|------------|---------|
| `CameraMixin` | `Camera` | `@Inject(update)`, `@Inject(setRotation)` | Adjust camera while riding track |
| `EntityRenderDispatcherMixin` | `EntityRenderDispatcher` | `@Inject(render)` x2 | Rotate entities on track follower |
| `GameRendererMixin` | `GameRenderer` | `@Inject(loadPrograms)` | Register custom shader |
| `InGameOverlayRendererMixin` | `InGameOverlayRenderer` | `@Inject(getInWallBlockState)` | Fix suffocation overlay |
| `MinecartInsideSoundInstanceMixin` | `MinecartInsideSoundInstance` | `@Inject(tick)` | Adjust sound when on track |
| `MovingMinecartSoundInstanceMixin` | `MovingMinecartSoundInstance` | `@Inject(tick)` | Adjust sound when on track |
| `WorldRendererMixin` | `WorldRenderer` | `@ModifyExpressionValue(setupTerrain)`, `@Inject(updateBlock)` | Chunk occlusion + VBO rebuild |

### Access Widener
- **No access widener file found** - No access transformers needed.

---

## NeoForge Porting Decisions

### Registry Approach
- Use `DeferredRegister` for all registry types
- Keep all registry names identical to Fabric version

### Event Mapping
| Fabric Event | NeoForge Event |
|--------------|----------------|
| `ItemGroupEvents.modifyEntriesEvent()` | `BuildCreativeModeTabContentsEvent` |
| `BlockRenderLayerMap.INSTANCE.putBlock()` | Client init or `ItemBlockRenderTypes.setRenderLayer()` |
| `BlockEntityRendererFactories.register()` | `EntityRenderersEvent.RegisterRenderers` |
| `EntityRendererRegistry.register()` | `EntityRenderersEvent.RegisterRenderers` |
| `ClientCommandRegistrationCallback.EVENT` | `RegisterClientCommandsEvent` |
| `HudRenderCallback.EVENT` | `RenderGuiLayerEvent.Post` / `RegisterGuiLayersEvent` |

### Mixin Compatibility
- All mixins should work with NeoForge as they target vanilla classes
- Client mixins registered with `environment: "client"` in mods.toml to prevent server loading
- MixinExtras annotations (`@ModifyReturnValue`, `@ModifyExpressionValue`) are supported in NeoForge

### TrackedDataHandler Registration
- Fabric uses `TrackedDataHandlerRegistry.register()` directly
- NeoForge uses `EntityDataSerializers` registered via `RegisterEvent` for `ENTITY_DATA_SERIALIZERS`

---

## Manual Test Checklist

### Blocks/Items
- [ ] Track Ties block appears in creative menu under Redstone tab
- [ ] Track Ties block appears in Splinecart creative tab
- [ ] Track, Chain Drive Track, Magnetic Track items appear
- [ ] All items have correct textures and lore

### Crafting
- [ ] Track Ties crafting recipe works (wooden slabs + stick)
- [ ] Track crafting recipe works (rail + iron blocks)
- [ ] Chain Drive Track crafting recipe works (activator rail + iron blocks + chains)
- [ ] Magnetic Track crafting recipe works (powered rail + iron blocks + copper blocks)

### Block Functionality
- [ ] Track Ties can be placed on any surface
- [ ] Right-clicking Track Ties rotates pointing direction
- [ ] Track Ties drop themselves when broken

### Track Linking
- [ ] Using Track item on Track Ties sets first selection
- [ ] Using Track item on second Track Ties creates track connection
- [ ] Track renders correctly between connected ties
- [ ] Using Track on non-tie block clears selection

### Minecart Behavior
- [ ] Minecart entering Track Ties mounts the TrackFollower
- [ ] Minecart follows spline track path
- [ ] Chain Drive track pulls minecarts at constant speed
- [ ] Magnetic track accelerates/decelerates based on redstone power

### Rendering
- [ ] Track renders with correct texture
- [ ] Track overlay (chain/magnetic) renders correctly
- [ ] Debug rendering (F3) shows pose markers
- [ ] Camera rotates with track when `rotate_camera` is true

### Client Commands
- [ ] `/splinecartc config rotate_camera <true/false>` works
- [ ] `/splinecartc config vbos <true/false>` works
- [ ] `/splinecartc config track_resolution <1-16>` works
- [ ] `/splinecartc config track_render_distance <4-32>` works

### Sound
- [ ] Minecart sounds adjust volume when on track

---

## TODO / Known Issues

### Build Environment
- Build command: `./gradlew build`
- Client run command: `./gradlew runClient`
- Requires Gradle 8.14 (configured in gradle-wrapper.properties)

### Mixin Target Changes (NeoForge vs Fabric)
The following mixin targets have been updated for NeoForge/Mojmap naming:
- `Entity.setPosition` → `Entity.setPos`
- `Entity.getRotationVector` → `Entity.getViewVector`  
- `Entity.getCameraPosVec` → `Entity.getEyePosition(F)`
- `Camera.update` → `Camera.setup`
- `Camera.setPos` → `Camera.setPosition`
- `Camera.setRotation(FF)V` → `Camera.setRotation(FFF)V` (NeoForge 1.21.1 added a `roll` parameter)
- `WorldRenderer` → `LevelRenderer`
- `BlockView` → `BlockGetter`
- `VertexConsumerProvider` → `MultiBufferSource`
- `MatrixStack` → `PoseStack`
- `Identifier` → `ResourceLocation`
- `NbtCompound` → `CompoundTag`
- `Vec3d` → `Vec3`
- `AbstractMinecartEntity` → `AbstractMinecart`
- `MinecartInsideSoundInstance` → `MinecartSoundInstance`
- `MovingMinecartSoundInstance` → `RidingMinecartSoundInstance`
- `InGameOverlayRenderer.getInWallBlockState` → `ScreenEffectRenderer.getViewBlockingState`

### Shader Registration
In NeoForge, shader registration is done via `RegisterShadersEvent` instead of a mixin into `GameRenderer.loadPrograms`. The GameRendererMixin has been removed and shader registration moved to SplinecartClient.

### HUD Overlay Registration
In NeoForge 1.21.1, HUD overlays are registered via `RegisterGuiLayersEvent` using `LayeredDraw.Layer` interface instead of Fabric's `HudRenderCallback`.

### Client Commands
NeoForge uses `RegisterClientCommandsEvent` with `CommandSourceStack` instead of Fabric's `ClientCommandRegistrationCallback` with `FabricClientCommandSource`.

### Build Status
- ✅ Build successful with `./gradlew build`
- ✅ Produces `splinecart-0.3.1+1.21.1.jar`
- Run with `./gradlew runClient`
