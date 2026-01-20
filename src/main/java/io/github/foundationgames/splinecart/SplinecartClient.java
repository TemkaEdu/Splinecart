package io.github.foundationgames.splinecart;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.splinecart.block.TrackGeometry;
import io.github.foundationgames.splinecart.block.entity.ClientTrackGeometry;
import io.github.foundationgames.splinecart.block.entity.TrackTiesBlockEntityRenderer;
import io.github.foundationgames.splinecart.config.Config;
import io.github.foundationgames.splinecart.config.ConfigOption;
import io.github.foundationgames.splinecart.util.SUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.io.IOException;

@EventBusSubscriber(modid = Splinecart.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SplinecartClient {
    public static final Config CONFIG = new Config("splinecart_client",
            () -> FMLPaths.CONFIGDIR.get()
                    .resolve("splinecart").resolve("splinecart_client.properties"));

    public static final ConfigOption.BooleanOption CFG_ROTATE_CAMERA = CONFIG.optBool("rotate_camera", true);
    public static final ConfigOption.BooleanOption CFG_VBOS = CONFIG.optBool("vbos", false);
    public static final ConfigOption.IntOption CFG_TRACK_RESOLUTION = CONFIG.optInt("track_resolution", 3, 1, 16);
    public static final ConfigOption.IntOption CFG_TRACK_RENDER_DISTANCE = CONFIG.optInt("track_render_distance", 8, 4, 32);

    public static ShaderInstance entityCutoutNoCullUvTransformProgram;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        SUtil.TICK_DELTA = () -> Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

        try {
            CONFIG.load();
        } catch (IOException e) {
            Splinecart.LOGGER.error("Error loading client config on mod init", e);
        }

        // Set track geometry constructor for client
        TrackGeometry.CONSTRUCTOR = ClientTrackGeometry::new;
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register block entity renderer
        event.registerBlockEntityRenderer(Splinecart.TRACK_TIES_BE.get(), TrackTiesBlockEntityRenderer::new);
        // Register entity renderer (empty/invisible)
        event.registerEntityRenderer(Splinecart.TRACK_FOLLOWER.get(), NoopRenderer::new);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        Splinecart.id("splinecart_rendertype_entity_cutout_no_cull_uv_transform"),
                        com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> entityCutoutNoCullUvTransformProgram = shader
        );
    }

    public static ShaderInstance getProgramEntityCutoutNoCullUvTransform() {
        return entityCutoutNoCullUvTransformProgram;
    }

    public static RenderType renderLayerEntityCutoutNoCullUvTransform(ResourceLocation texture, float x, float y) {
        return RenderType.create(
                "splinecart_entity_cutout_no_cull_uv_transform",
                com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                1536,
                true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderType.ShaderStateShard(SplinecartClient::getProgramEntityCutoutNoCullUvTransform))
                        .setTextureState(new RenderType.TextureStateShard(texture, false, false))
                        .setTexturingState(new RenderType.OffsetTexturingStateShard(x, y))
                        .setTransparencyState(RenderType.NO_TRANSPARENCY)
                        .setCullState(RenderType.NO_CULL)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setOverlayState(RenderType.OVERLAY)
                        .createCompositeState(false)
        );
    }

    // NeoForge Game Events - registered separately on NeoForge.EVENT_BUS
    @EventBusSubscriber(modid = Splinecart.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("splinecartc")
                            .then(CONFIG.command(LiteralArgumentBuilder.literal("config"),
                                    (source, text) -> source.sendSuccess(() -> text, false)))
            );
        }

        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Splinecart.id("hud"), new SplinecartHud());
        }
    }
}